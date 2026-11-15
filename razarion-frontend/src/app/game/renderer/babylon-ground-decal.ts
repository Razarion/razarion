import {Ray} from "@babylonjs/core/Culling/ray.core";
import {NodeMaterial} from "@babylonjs/core/Materials/Node/nodeMaterial";
import {RawTexture} from "@babylonjs/core/Materials/Textures/rawTexture";
import {DynamicTexture} from "@babylonjs/core/Materials/Textures/dynamicTexture";
import {Texture} from "@babylonjs/core/Materials/Textures/texture";
import {Vector3} from "@babylonjs/core/Maths/math.vector";
import {AbstractMesh} from "@babylonjs/core/Meshes/abstractMesh";
import {Mesh} from "@babylonjs/core/Meshes/mesh";
import {MeshBuilder} from "@babylonjs/core/Meshes/meshBuilder";
import {Scene} from "@babylonjs/core/scene";
import {VertexBuffer} from "@babylonjs/core/Buffers/buffer";
import {VertexData} from "@babylonjs/core/Meshes/mesh.vertexData";
import {Observer} from "@babylonjs/core/Misc/observable";
import {BabylonRenderServiceAccessImpl, RazarionMetadataType} from "./babylon-render-service-access-impl.service";
import {BabylonTerrainTileImpl} from "./babylon-terrain-tile.impl";
import {buildBotGroundTopMaterial} from "./bot-ground-top-material";

// The opaque plate covers this share of the decal; the rest is the frayed gravel edge
const PLATE_SHARE = 0.78;
const MASK_SIZE = 256;
const RETRY_MS = 500;
const MAX_RETRY_MS = 8000;
const DOWN = new Vector3(0, -1, 0);

/**
 * The ground under a building whose type has a ground decal radius: an asphalt plate with a frayed
 * gravel edge, projected onto the terrain tiles. Unlike a bot's ground box it is not part of the
 * terrain - it comes and goes with the item, has no height and no walls.
 *
 * The decal is cut per terrain mesh, so a building on a tile border gets one decal on each tile.
 * When the tile is not loaded yet the decal is retried until it is. A building standing on a bot's
 * ground box gets none: the plate is its ground.
 */
export class BabylonGroundDecal {
  private static maskTexture: DynamicTexture | null = null;
  private static material: NodeMaterial | null = null;
  private decals: Mesh[] = [];
  private retryTimer: ReturnType<typeof setTimeout> | null = null;
  // a newer place() or dispose() makes a pending attempt stale
  private generation = 0;
  private placedAt: Vector3 | null = null;
  private disposed = false;

  constructor(private scene: Scene, private radius: number, private rendererService: BabylonRenderServiceAccessImpl) {
  }

  /** x/z are world X/Z, angle the item's rotation around Y. Cheap when nothing moved. */
  place(x: number, z: number, angle: number): void {
    if (this.disposed) {
      return;
    }
    const at = new Vector3(x, angle, z);
    if (this.placedAt && Vector3.DistanceSquared(this.placedAt, at) < 0.0025) {
      return;
    }
    this.placedAt = at;
    this.clearDecals();
    this.schedule(x, z, angle, 0, 0);
  }

  /**
   * A tile's bot grounds stand one build phase after its ground mesh. Picking in between would
   * see bare terrain where a plate is about to go up, so the decal waits until the tiles are built.
   */
  private schedule(x: number, z: number, angle: number, attempt: number, delay: number): void {
    const generation = ++this.generation;
    const run = () => {
      this.retryTimer = null;
      BabylonTerrainTileImpl.whenBuildsSettled().then(() => BabylonGroundDecal.enqueue(this.scene, () => {
        if (!this.disposed && generation === this.generation) {
          this.tryCreate(x, z, angle, attempt);
        }
      }));
    };
    if (delay > 0) {
      this.retryTimer = setTimeout(run, delay);
    } else {
      run();
    }
  }

  private tryCreate(x: number, z: number, angle: number, attempt: number): void {
    if (this.onBotGround(x, z)) {
      // Standing on a bot's ground box: the plate is its ground already, a decal would be hidden under it
      return;
    }
    const size = this.radius * 2 / PLATE_SHARE;
    // The decal must cover the square turned by any angle: half its diagonal plus a little
    const half = size * 0.75;
    const grounds = this.findGroundMeshes(x, z, half);
    if (grounds.length === 0) {
      // Tile not there (yet), e.g. out of view: back off, a look at the bounding boxes costs nothing
      this.schedule(x, z, angle, attempt + 1, Math.min(RETRY_MS * 2 ** attempt, MAX_RETRY_MS));
      return;
    }
    const material = this.getMaterial();
    for (const mesh of grounds) {
      let piece: { mesh: Mesh, minY: number, maxY: number } | null = null;
      try {
        piece = BabylonGroundDecal.cutOut(mesh, x, z, half, this.scene);
        if (!piece) {
          continue;
        }
        const decal = MeshBuilder.CreateDecal("groundDecal", piece.mesh, {
          // centred on the terrain under it and deep enough for all of it
          position: new Vector3(x, (piece.minY + piece.maxY) / 2, z),
          normal: new Vector3(0, 1, 0),
          size: new Vector3(size, size, Math.max(4, piece.maxY - piece.minY + 2)),
          angle: angle,
        });
        if (!decal || decal.getTotalVertices() === 0) {
          decal?.dispose();
          continue;
        }
        decal.isPickable = false;
        // the building and its neighbours cast onto it like onto the plates (LightBlock reads the shadow map)
        decal.receiveShadows = true;
        decal.material = material;
        this.rendererService.directionalLight.includedOnlyMeshes.push(decal);
        // like the plates: their ambient term stands in for the sky
        this.scene.getLightByName(BabylonRenderServiceAccessImpl.SKY_LIGHT_NAME)?.excludedMeshes.push(decal);
        this.decals.push(decal);
      } catch (_) {
        // a failing decal leaves the ground bare, nothing more
      } finally {
        piece?.mesh.dispose();
      }
    }
  }

  /**
   * The triangles of a ground mesh under the square around x/z, as a temporary mesh in the same
   * place, with the world height span of them. CreateDecal walks every triangle of its source: on a
   * whole tile (51 200) that took 35 ms on a phone (Pixel, 15.11.2026), on the cut-out 1.3 ms.
   * The cut itself is two flat passes: an outcode per vertex, then one AND per triangle.
   */
  private static cutOut(mesh: Mesh, x: number, z: number, half: number, scene: Scene): { mesh: Mesh, minY: number, maxY: number } | null {
    const positions = mesh.getVerticesData(VertexBuffer.PositionKind);
    const normals = mesh.getVerticesData(VertexBuffer.NormalKind);
    const indices = mesh.getIndices();
    if (!positions || !normals || !indices) {
      return null;
    }
    const worldMatrix = mesh.getWorldMatrix();
    const local = Vector3.TransformCoordinates(new Vector3(x, 0, z), worldMatrix.clone().invert());
    const minX = local.x - half, maxX = local.x + half, minZ = local.z - half, maxZ = local.z + half;
    const vertexCount = positions.length / 3;
    // bit 1: left of the square, 2: right, 4: below, 8: above. A triangle whose three vertices share
    // a bit lies wholly on that side.
    const outcode = new Uint8Array(vertexCount);
    for (let v = 0, p = 0; v < vertexCount; v++, p += 3) {
      const px = positions[p], pz = positions[p + 2];
      outcode[v] = (px < minX ? 1 : px > maxX ? 2 : 0) | (pz < minZ ? 4 : pz > maxZ ? 8 : 0);
    }
    const remap = new Int32Array(vertexCount).fill(-1);
    const piecePositions: number[] = [];
    const pieceNormals: number[] = [];
    const pieceIndices: number[] = [];
    let minY = Infinity, maxY = -Infinity;
    for (let i = 0; i < indices.length; i += 3) {
      if (outcode[indices[i]] & outcode[indices[i + 1]] & outcode[indices[i + 2]]) {
        continue;
      }
      for (let k = 0; k < 3; k++) {
        const v = indices[i + k];
        if (remap[v] < 0) {
          remap[v] = piecePositions.length / 3;
          const p = v * 3;
          piecePositions.push(positions[p], positions[p + 1], positions[p + 2]);
          pieceNormals.push(normals[p], normals[p + 1], normals[p + 2]);
          minY = Math.min(minY, positions[p + 1]);
          maxY = Math.max(maxY, positions[p + 1]);
        }
        pieceIndices.push(remap[v]);
      }
    }
    if (pieceIndices.length === 0) {
      return null;
    }
    const piece = new Mesh("groundDecalSource", scene);
    const vertexData = new VertexData();
    vertexData.positions = piecePositions;
    vertexData.normals = pieceNormals;
    vertexData.indices = pieceIndices;
    vertexData.applyToMesh(piece);
    piece.isVisible = false;
    piece.isPickable = false;
    piece.freezeWorldMatrix(worldMatrix.clone());
    // the tile meshes are only moved, never scaled or turned: the world height is the local one plus the offset
    const offsetY = worldMatrix.getTranslation().y;
    return {mesh: piece, minY: minY + offsetY, maxY: maxY + offsetY};
  }

  private static queue: (() => void)[] = [];
  private static queueObserver: Observer<Scene> | null = null;
  private static queueScene: Scene | null = null;
  // Per frame. Many decals come at once - all extractors in view right after the start - and on a
  // phone a few of them would make one long frame.
  private static readonly FRAME_BUDGET_MS = 2;

  private static enqueue(scene: Scene, job: () => void): void {
    if (BabylonGroundDecal.queueScene !== scene || scene.isDisposed) {
      // a new scene (studio, reload): the old observer went with the old one
      BabylonGroundDecal.queue = [];
      BabylonGroundDecal.queueObserver = null;
      BabylonGroundDecal.queueScene = scene;
    }
    BabylonGroundDecal.queue.push(job);
    if (BabylonGroundDecal.queueObserver) {
      return;
    }
    BabylonGroundDecal.queueObserver = scene.onBeforeRenderObservable.add(() => {
      const start = performance.now();
      // at least one per frame, then as many as fit
      do {
        try {
          BabylonGroundDecal.queue.shift()!();
        } catch (error) {
          console.warn("Ground decal failed", error);
        }
      } while (BabylonGroundDecal.queue.length > 0 && performance.now() - start < BabylonGroundDecal.FRAME_BUDGET_MS);
      if (BabylonGroundDecal.queue.length === 0) {
        scene.onBeforeRenderObservable.remove(BabylonGroundDecal.queueObserver);
        BabylonGroundDecal.queueObserver = null;
      }
    });
  }

  /**
   * The ground meshes whose bounds overlap the square around x/z. Only the bounding boxes: a ray
   * against a tile tests its 51 200 triangles, 2.6 ms each on a phone (15.11.2026).
   */
  private findGroundMeshes(x: number, z: number, half: number): Mesh[] {
    const result: Mesh[] = [];
    for (const mesh of this.scene.meshes) {
      if (BabylonRenderServiceAccessImpl.getRazarionMetadata(mesh)?.type !== RazarionMetadataType.GROUND || !mesh.isEnabled()) {
        continue;
      }
      const box = mesh.getBoundingInfo().boundingBox;
      if (box.maximumWorld.x >= x - half && box.minimumWorld.x <= x + half
        && box.maximumWorld.z >= z - half && box.minimumWorld.z <= z + half) {
        result.push(mesh as Mesh);
      }
    }
    return result;
  }

  /** A ray against the bot ground boxes only: they have a handful of triangles each. */
  private onBotGround(x: number, z: number): boolean {
    const ray = new Ray(new Vector3(x, 200, z), DOWN, 400);
    return !!this.scene.pickWithRay(ray, (mesh: AbstractMesh) =>
      BabylonRenderServiceAccessImpl.getRazarionMetadata(mesh)?.type === RazarionMetadataType.BOT_GROUND, true)?.hit;
  }

  /**
   * The bot ground's own asphalt material (bot-ground-top-material.ts), so a decal is as bright as
   * a plate and lit the same way (sun + its ambient, clouds, no sky light). Its UV is the world
   * grid, not the decal's, so one material serves every decal of every size.
   */
  private getMaterial(): NodeMaterial {
    const cached = BabylonGroundDecal.material;
    if (cached && cached.getScene() === this.scene && !this.scene.isDisposed) {
      return cached;
    }
    // No painted markings: a fully transparent 1 x 1 texture
    const noMarkings = RawTexture.CreateRGBATexture(new Uint8Array(4), 1, 1, this.scene, false);
    const material = buildBotGroundTopMaterial(this.scene, {
      texture: noMarkings,
      gravel: noMarkings,
      originX: 0,
      originZ: 0,
      sizeX: 1,
      sizeZ: 1,
    }, BabylonGroundDecal.getMask(this.scene));
    material.name = "groundDecalMat";
    material.backFaceCulling = false;
    material.zOffset = -2;
    BabylonGroundDecal.material = material;
    return material;
  }

  /**
   * Alpha mask shared by all decals: an opaque rounded square, and outside it gravel that thins out
   * towards the edge. Drawn once; the noise is seeded so every plate frays the same way.
   */
  private static getMask(scene: Scene): DynamicTexture {
    if (BabylonGroundDecal.maskTexture && !BabylonGroundDecal.maskTexture.getScene()?.isDisposed) {
      return BabylonGroundDecal.maskTexture;
    }
    const texture = new DynamicTexture("groundDecalMask", MASK_SIZE, scene, true);
    const ctx = texture.getContext() as CanvasRenderingContext2D;
    const image = ctx.createImageData(MASK_SIZE, MASK_SIZE);
    // one value per 3x3 pixel cell, so the edge reads as small stones rather than as pixel noise
    const cellNoise = (cx: number, cy: number, salt: number) => {
      let h = (cx * 374761393 + cy * 668265263 + salt * 2147483647) | 0;
      h = Math.imul(h ^ (h >>> 13), 1274126177);
      return ((h ^ (h >>> 16)) >>> 0) / 4294967295;
    };
    const plateHalf = PLATE_SHARE / 2;
    const corner = 0.06;
    for (let py = 0; py < MASK_SIZE; py++) {
      for (let px = 0; px < MASK_SIZE; px++) {
        const u = Math.abs((px + 0.5) / MASK_SIZE - 0.5);
        const v = Math.abs((py + 0.5) / MASK_SIZE - 0.5);
        // distance outside the rounded plate, 0 inside
        const qx = Math.max(u - (plateHalf - corner), 0);
        const qy = Math.max(v - (plateHalf - corner), 0);
        const outside = Math.max(Math.hypot(qx, qy) - corner, 0);
        const edge = 0.5 - plateHalf;
        let alpha: number;
        if (outside <= 0) {
          alpha = 1;
        } else {
          const t = Math.min(outside / edge, 1);
          // gravel: dense next to the plate, sparse at the edge of the decal
          const cx = Math.floor(px / 3), cy = Math.floor(py / 3);
          alpha = cellNoise(cx, cy, 1) > t * 1.1 ? (0.55 + 0.45 * cellNoise(cx, cy, 2)) * (1 - t * t) : 0;
        }
        const i = (py * MASK_SIZE + px) * 4;
        const a = Math.round(alpha * 255);
        image.data[i] = image.data[i + 1] = image.data[i + 2] = a;
        image.data[i + 3] = 255;
      }
    }
    ctx.putImageData(image, 0, 0);
    texture.update();
    texture.getAlphaFromRGB = true;
    texture.wrapU = texture.wrapV = Texture.CLAMP_ADDRESSMODE;
    BabylonGroundDecal.maskTexture = texture;
    return texture;
  }

  private clearDecals(): void {
    if (this.retryTimer) {
      clearTimeout(this.retryTimer);
      this.retryTimer = null;
    }
    const lit = this.rendererService.directionalLight.includedOnlyMeshes;
    const sky = this.scene.getLightByName(BabylonRenderServiceAccessImpl.SKY_LIGHT_NAME)?.excludedMeshes;
    for (const decal of this.decals) {
      const index = lit.indexOf(decal);
      if (index !== -1) {
        lit.splice(index, 1);
      }
      const skyIndex = sky ? sky.indexOf(decal) : -1;
      if (skyIndex !== -1) {
        sky!.splice(skyIndex, 1);
      }
      decal.dispose();
    }
    this.decals = [];
  }

  /** The material and the mask are shared and stay. */
  dispose(): void {
    this.disposed = true;
    this.generation++;
    this.clearDecals();
  }
}
