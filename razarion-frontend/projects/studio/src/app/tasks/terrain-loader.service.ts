import {Injectable, inject} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {firstValueFrom} from 'rxjs';
import {BabylonRenderServiceAccessImpl} from '../../../../../src/app/game/renderer/babylon-render-service-access-impl.service';
import {BabylonTerrainTileImpl} from '../../../../../src/app/game/renderer/babylon-terrain-tile.impl';
import {GwtInstance} from '../../../../../src/app/gwtangular/GwtInstance';
import {GwtAngularService} from '../../../../../src/app/gwtangular/GwtAngularService';
import {
  BabylonDecal,
  BabylonTerrainTile,
  BotGround,
  GroundConfig as RendererGroundConfig,
  Index,
  TerrainObjectConfig as RendererTerrainObjectConfig,
  TerrainTile,
  TerrainTileObjectList,
  TerrainTypeService
} from '../../../../../src/app/gwtangular/GwtAngularFacade';
import {
  GroundEditorControllerClient,
  GroundConfig,
  PlanetEditorControllerClient,
  PlanetConfig,
  TerrainObjectConfig,
  TerrainObjectEditorControllerClient
} from '../../../../../src/app/generated/razarion-share';
import {TypescriptGenerator} from '../../../../../src/app/backend/typescript-generator';

/**
 * Mirrors NativeTerrainShape from razarion-share. The server returns this
 * shape via /rest/terrainshape/{planetId} — a 2D matrix of per-tile data.
 */
interface NativeTerrainShape {
  nativeTerrainShapeTiles: NativeTerrainShapeTile[][];
}
interface NativeTerrainShapeTile {
  nativeTerrainShapeObjectLists?: NativeTerrainShapeObjectList[];
  nativeBabylonDecals?: NativeBabylonDecal[];
  nativeBotGrounds?: NativeBotGround[];
}
interface NativeTerrainShapeObjectList {
  terrainObjectConfigId: number;
  terrainShapeObjectPositions: NativeTerrainShapeObjectPosition[];
}
interface NativeTerrainShapeObjectPosition {
  terrainObjectId: number;
  x: number;
  y: number;
  scale: NativeVertex | null;
  rotation: NativeVertex | null;
  offset: NativeVertex | null;
}
interface NativeVertex { x: number; y: number; z: number; }
interface NativeBabylonDecal {
  babylonMaterialId: number;
  xPos: number;
  yPos: number;
  xSize: number;
  ySize: number;
}
interface NativeBotGround {
  height: number;
  positions: {x: number; y: number}[];
  botGroundSlopeBoxes: {xPos: number; yPos: number; height: number; yRot: number; zRot: number}[];
}

/**
 * Builds Babylon terrain tiles directly from razarion-server data without
 * running the game engine. The game-runtime path takes the same inputs
 * (heightmap binary + NativeTerrainShape + groundConfigId) and feeds them
 * into `BabylonRenderServiceAccessImpl.createTerrainTile`, so we do the same
 * here — just from REST calls instead of from a TeaVM worker.
 *
 * Phase 7a keeps tile decoration empty (objectLists/decals/botGrounds all
 * `[]`) so terrain renders quickly; trees/decals come in 7b.
 */
@Injectable({providedIn: 'root'})
export class TerrainLoaderService {
  private readonly httpClient = inject(HttpClient);
  private readonly babylonRender = inject(BabylonRenderServiceAccessImpl);
  private readonly gwtAngularService = inject(GwtAngularService);
  private readonly planetClient = new PlanetEditorControllerClient(
    TypescriptGenerator.generateHttpClientAdapter(this.httpClient)
  );
  private readonly terrainObjectClient = new TerrainObjectEditorControllerClient(
    TypescriptGenerator.generateHttpClientAdapter(this.httpClient)
  );
  private readonly groundClient = new GroundEditorControllerClient(
    TypescriptGenerator.generateHttpClientAdapter(this.httpClient)
  );

  private currentTiles: BabylonTerrainTile[] = [];
  private terrainTypeServiceReady = false;

  async listPlanets() {
    return this.planetClient.getObjectNameIds();
  }

  /**
   * Dispose any previously-loaded terrain and build the tiles for `planetId`.
   * No-op if planetId is null/0.
   */
  async loadTerrain(planetId: number | null): Promise<void> {
    this.disposeTerrain();
    if (!planetId) return;

    // The terrain renderer asks gwtAngularFacade.terrainTypeService for each
    // tile's TerrainObjectConfig and GroundConfig — without this the cloned
    // tile silently skips trees/decals/etc.
    await this.ensureTerrainTypeService();

    const [planet, heightmapBuf, terrainShape] = await Promise.all([
      this.fetchPlanetConfig(planetId),
      this.fetchHeightmap(planetId),
      this.fetchTerrainShape(planetId)
    ]);

    const heightMap = new Uint16Array(heightmapBuf);
    // Server stores nativeTerrainShapeTiles[x][y] (X-major). JSON preserves the
    // nesting order, so the outer index is x and inner is y — same as Java.
    const tileMatrix = terrainShape.nativeTerrainShapeTiles;
    const tileXCount = tileMatrix.length;
    const tileYCount = tileMatrix[0]?.length ?? 0;
    const groundConfigId = planet.groundConfigId ?? 0;

    // z-sampler closes over the planet-wide heightmap; terrainObjects only
    // know their (x, y), the renderer wants Vertex (x, y, z).
    const sampleZ = (worldX: number, worldY: number) =>
      sampleHeightAt(worldX, worldY, heightMap, tileXCount, tileYCount);

    for (let x = 0; x < tileXCount; x++) {
      for (let y = 0; y < tileYCount; y++) {
        const tileData = tileMatrix[x][y];
        if (!tileData) continue;
        const tileHeightMap = sliceTileHeightMap(heightMap, x, y, tileXCount, tileYCount);
        const tile = buildTerrainTile(x, y, tileHeightMap, groundConfigId, tileData, sampleZ);
        const babylonTile = this.babylonRender.createTerrainTile(tile);
        babylonTile.addToScene();
        this.currentTiles.push(babylonTile);
      }
    }
  }

  disposeTerrain(): void {
    for (const tile of this.currentTiles) {
      try { tile.removeFromScene(); } catch { /* defensive */ }
    }
    this.currentTiles = [];
  }

  /**
   * Build a lookup-only TerrainTypeService backed by the editor REST endpoints
   * and attach it to gwtAngularFacade. Called once per session — the configs
   * are essentially static and the caches survive scene switches.
   */
  private async ensureTerrainTypeService(): Promise<void> {
    if (this.terrainTypeServiceReady) return;
    const [terrainObjectConfigs, groundConfigs] = await Promise.all([
      this.terrainObjectClient.readAll(),
      this.groundClient.readAll()
    ]);
    const toMap = new Map<number, TerrainObjectConfig>(terrainObjectConfigs.map(c => [c.id, c]));
    const gMap = new Map<number, GroundConfig>(groundConfigs.map(c => [c.id, c]));

    const service: TerrainTypeService = {
      getTerrainObjectConfig(id: number): RendererTerrainObjectConfig {
        const c = toMap.get(id);
        if (!c) throw new Error(`No TerrainObjectConfig for id ${id}`);
        return wrapTerrainObjectConfig(c);
      },
      getGroundConfig(groundConfigId: number): RendererGroundConfig {
        const c = gMap.get(groundConfigId);
        if (!c) throw new Error(`No GroundConfig for id ${groundConfigId}`);
        return wrapGroundConfig(c);
      }
    };
    this.gwtAngularService.gwtAngularFacade.terrainTypeService = service;
    this.terrainTypeServiceReady = true;
  }

  // ===== Fetch helpers =====

  private async fetchPlanetConfig(planetId: number): Promise<PlanetConfig> {
    return this.planetClient.read(planetId);
  }

  private async fetchHeightmap(planetId: number): Promise<ArrayBuffer> {
    return await firstValueFrom(
      this.httpClient.get(`/rest/terrainHeightMap/${planetId}`, {responseType: 'arraybuffer'})
    );
  }

  private async fetchTerrainShape(planetId: number): Promise<NativeTerrainShape> {
    return await firstValueFrom(
      this.httpClient.get<NativeTerrainShape>(`/rest/terrainshape/${planetId}`)
    );
  }
}

/**
 * Reconstructs the per-tile (NODE_X+1)×(NODE_Y+1) heightmap slice from the
 * flat planet-wide heightmap, including the east/north edge values that the
 * renderer needs for seamless tile boundaries.
 *
 * Logic ported from GameMockService.setupHeightMap — origin comment:
 * "Copied from com.btxtech.common.ClientNativeTerrainShapeAccess
 * .createTileGroundHeightMap()".
 */
function sliceTileHeightMap(
  heightMap: Uint16Array,
  tileX: number,
  tileY: number,
  tileXCount: number,
  tileYCount: number
): Uint16Array {
  const NX = BabylonTerrainTileImpl.NODE_X_COUNT;
  const NY = BabylonTerrainTileImpl.NODE_Y_COUNT;
  const tileNodeSize = BabylonTerrainTileImpl.TILE_NODE_SIZE;
  const tileStart = (tx: number, ty: number) => ty * (tileXCount * tileNodeSize) + tx * tileNodeSize;

  const t = tileStart(tileX, tileY);
  const tNextX = tileStart(tileX + 1, tileY);
  const tNextY = tileStart(tileX, tileY + 1);
  const tNextXY = tileStart(tileX + 1, tileY + 1);

  const out = new Uint16Array((NX + 1) * (NY + 1));
  for (let i = 0; i < NY; i++) {
    const srcYOff = i * NX;
    const srcStart = t + srcYOff;
    const srcEnd = srcStart + NX;
    const dstStart = i * (NX + 1);
    try {
      const row = heightMap.slice(srcStart, srcEnd);
      out.set(row, dstStart);

      const eastStart = tileX + 1 < tileXCount ? tNextX + srcYOff : srcEnd + 1;
      out.set(heightMap.slice(eastStart, eastStart + 1), dstStart + NX);

      if (i === NY - 1) {
        if (tileY + 1 < tileYCount) {
          out.set(heightMap.slice(tNextY, tNextY + NX), dstStart + NX + 1);
          const cornerStart = tileX + 1 < tileXCount ? tNextXY : tNextY + NX + 1;
          out.set(heightMap.slice(cornerStart, cornerStart + 1), dstStart + NX + 1 + NX);
        } else {
          out.set(row, dstStart + NX + 1);
          const cornerStart = tileX + 1 < tileXCount ? tNextX + srcYOff : srcEnd;
          out.set(heightMap.slice(cornerStart, cornerStart + 1), dstStart + NX + 1 + NX);
        }
      }
    } catch (e) {
      console.warn('[Studio] terrain slice failed', e);
    }
  }
  return out;
}

/**
 * Builds the renderer-facing TerrainTile from raw inputs. Per-tile decoration
 * (trees, decals, bot plateaus) is mapped from NativeTerrainShapeTile fields;
 * the (x, y) coords for terrain objects gain their z from the heightmap.
 */
function buildTerrainTile(
  tileX: number,
  tileY: number,
  heightMap: Uint16Array,
  groundConfigId: number,
  tileData: NativeTerrainShapeTile,
  sampleZ: (worldX: number, worldY: number) => number
): TerrainTile {
  const index: Index = GwtInstance.newIndex(tileX, tileY);

  const objectLists: TerrainTileObjectList[] = (tileData.nativeTerrainShapeObjectLists ?? []).map(list => ({
    terrainObjectConfigId: list.terrainObjectConfigId,
    terrainObjectModels: (list.terrainShapeObjectPositions ?? []).map(p => ({
      terrainObjectId: p.terrainObjectId,
      position: GwtInstance.newVertex(p.x, p.y, sampleZ(p.x, p.y) + (p.offset?.z ?? 0)),
      scale: p.scale ? GwtInstance.newVertex(p.scale.x, p.scale.y, p.scale.z) : null,
      rotation: p.rotation ? GwtInstance.newVertex(p.rotation.x, p.rotation.y, p.rotation.z) : null
    }))
  }));

  // NativeBabylonDecal and BabylonDecal already line up field-for-field.
  const decals: BabylonDecal[] = (tileData.nativeBabylonDecals ?? []).map(d => ({
    babylonMaterialId: d.babylonMaterialId,
    xPos: d.xPos,
    yPos: d.yPos,
    xSize: d.xSize,
    ySize: d.ySize
  }));

  const botGrounds: BotGround[] = (tileData.nativeBotGrounds ?? []).map(g => ({
    height: g.height,
    positions: (g.positions ?? []).map(pos => GwtInstance.newDecimalPosition(pos.x, pos.y)),
    botGroundSlopeBoxes: (g.botGroundSlopeBoxes ?? []).map(b => ({
      xPos: b.xPos, yPos: b.yPos, height: b.height, yRot: b.yRot, zRot: b.zRot
    }))
  }));

  return {
    getGroundHeightMap: () => heightMap,
    getGroundConfigId: () => groundConfigId,
    getTerrainTileObjectLists: () => objectLists,
    getBabylonDecals: () => decals,
    getBotGrounds: () => botGrounds,
    getIndex: () => index
  };
}

/**
 * Convert generated DTO TerrainObjectConfig (field-style) to the renderer's
 * interface (Java-getter style).
 */
function wrapTerrainObjectConfig(dto: TerrainObjectConfig): RendererTerrainObjectConfig {
  return {
    getId: () => dto.id,
    getInternalName: () => dto.internalName,
    getRadius: () => dto.radius,
    setRadius: (_r: number) => { /* studio is read-only against the registry */ },
    getModel3DId: () => dto.model3DId,
    toString: () => `${dto.internalName} '${dto.id}'`
  };
}

function wrapGroundConfig(dto: GroundConfig): RendererGroundConfig {
  return {
    getId: () => dto.id,
    getInternalName: () => dto.internalName,
    getGroundBabylonMaterialId: () => dto.groundBabylonMaterialId ?? 0,
    getUnderWaterBabylonMaterialId: () => dto.underWaterBabylonMaterialId,
    getAsphaltBabylonMaterialId: () => dto.asphaltBabylonMaterialId,
    getWaterBabylonMaterialId: () => dto.waterBabylonMaterialId ?? 0
  };
}

/**
 * Nearest-neighbour heightmap sample. World units map 1:1 to heightmap
 * nodes because NODE_SIZE=1. Returns 0 for out-of-bounds.
 */
function sampleHeightAt(
  worldX: number,
  worldY: number,
  heightMap: Uint16Array,
  tileXCount: number,
  tileYCount: number
): number {
  const NX = BabylonTerrainTileImpl.NODE_X_COUNT;
  const NY = BabylonTerrainTileImpl.NODE_Y_COUNT;
  const tileNodeSize = BabylonTerrainTileImpl.TILE_NODE_SIZE;
  const HP = BabylonTerrainTileImpl.HEIGHT_PRECISION;
  const HMIN = BabylonTerrainTileImpl.HEIGHT_MIN;
  const NODE = BabylonTerrainTileImpl.NODE_SIZE;

  let nx = Math.floor(worldX / NODE);
  let ny = Math.floor(worldY / NODE);
  const maxX = tileXCount * NX - 1;
  const maxY = tileYCount * NY - 1;
  if (nx < 0 || ny < 0 || nx > maxX || ny > maxY) return BabylonTerrainTileImpl.HEIGHT_DEFAULT;

  const tx = Math.floor(nx / NX);
  const ty = Math.floor(ny / NY);
  const localX = nx % NX;
  const localY = ny % NY;
  const idx = ty * (tileXCount * tileNodeSize) + tx * tileNodeSize + localY * NX + localX;
  const v = heightMap[idx];
  return v === undefined ? BabylonTerrainTileImpl.HEIGHT_DEFAULT : v * HP + HMIN;
}
