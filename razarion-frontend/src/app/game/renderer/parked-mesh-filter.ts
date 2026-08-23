import {AbstractMesh, ISmartArrayLike, RenderTargetTexture, Scene, SmartArray} from "@babylonjs/core";

/**
 * Keeps meshes that are in the scene but deliberately invisible out of Babylon's two per-frame
 * walks over every mesh.
 *
 * Why this exists. The terrain tile cache (TerrainUiService.MAX_CACHED_TILES) parks up to 48
 * scrolled-away tiles with {@code container.setEnabled(false)} so scrolling back is instant. Those
 * tiles stay in {@code scene.meshes}, and PROD telemetry showed what that costs:
 *
 *   renderP50 = 15.6 ms + 0.00125 ms x scene.meshes   (R^2 = 0.81, 258 periods, two sessions)
 *   renderP50 ~ activeMeshes                          (R^2 = 0.00)
 *
 * i.e. the frame time follows the size of the mesh array and ignores what is actually drawn. In one
 * measured session 5808 of 7975 meshes (73%) were parked terrain scenery, costing ~7.3 ms of a
 * 28.4 ms frame; in another the array reached 30 653 and the game ran at 15 fps until reload.
 *
 * The cost is not the drawing — it is the bookkeeping Babylon does before it finds out the mesh is
 * off. {@code Scene._evaluateActiveMeshes} touches the LOD map, reads {@code isBlocked}, sums
 * {@code getTotalVertices()} and calls {@code isReady()} *before* it ever asks {@code isEnabled()};
 * the shadow map's render list is walked a second time in {@code ObjectRenderer}.
 *
 * So this does not change what is rendered — it makes Babylon reach the same verdict sooner. Both
 * loops skip a disabled mesh anyway; this filter drops it one step earlier, at the price of a single
 * boolean read. Anything that is parked is by definition also {@code setEnabled(false)}, so removing
 * the filter (F10) must produce a pixel-identical frame and only a slower one — which is exactly
 * what makes the toggle a valid A/B against the telemetry line.
 *
 * Chosen over {@code scene.removeMesh()} on purpose: removing 450 meshes per tile would run
 * {@code _resyncLightSources()} and mark every submesh light-dirty on each scroll, trading a steady
 * per-frame cost for a stutter exactly when the player is moving.
 */
export class ParkedMeshFilter {
  /**
   * Written straight onto the mesh rather than held in a Set/WeakSet: this is read once per mesh
   * per frame in the hottest loop the renderer has, and a property read beats a hash lookup.
   */
  private static readonly FLAG = "__razParked";

  /** Reused across frames — a fresh array per frame would hand the GC 30 000 entries per second. */
  private readonly candidates = new SmartArray<AbstractMesh>(2048);
  private readonly shadowCasters: AbstractMesh[] = [];
  private enabled = true;
  private parkedCount = 0;

  /**
   * Marks a mesh as in-the-scene-but-never-drawn. The caller stays responsible for actually
   * disabling it; this flag only says "do not bother looking at it".
   */
  static park(mesh: AbstractMesh, parked: boolean): void {
    (mesh as any)[ParkedMeshFilter.FLAG] = parked;
  }

  static isParked(mesh: AbstractMesh): boolean {
    return (mesh as any)[ParkedMeshFilter.FLAG] === true;
  }

  /** Parks or unparks a whole subtree, e.g. everything a terrain tile owns. */
  static parkAll(meshes: AbstractMesh[], parked: boolean): void {
    for (let i = 0; i < meshes.length; i++) {
      (meshes[i] as any)[ParkedMeshFilter.FLAG] = parked;
    }
  }

  /**
   * Hooks the two documented Babylon extension points. Both are called once per frame and must
   * return synchronously-consumed data, which is why the buffers below can be reused.
   *
   * @param shadowMap the shadow generator's render target, or null if shadows are off
   */
  install(scene: Scene, shadowMap: RenderTargetTexture | null): void {
    const defaultCandidates = scene.getActiveMeshCandidates;
    scene.getActiveMeshCandidates = (): ISmartArrayLike<AbstractMesh> => {
      if (!this.enabled) {
        return defaultCandidates();
      }
      const meshes = scene.meshes;
      const candidates = this.candidates;
      candidates.reset();
      let parked = 0;
      for (let i = 0; i < meshes.length; i++) {
        const mesh = meshes[i];
        if ((mesh as any)[ParkedMeshFilter.FLAG] === true) {
          parked++;
        } else {
          candidates.push(mesh);
        }
      }
      this.parkedCount = parked;
      return candidates;
    };

    if (shadowMap) {
      // The shadow pass keeps its own list of casters, and a parked tile's terrain objects are all
      // on it — registered by BabylonModelService and never taken off again while hidden.
      shadowMap.getCustomRenderList = (_layerOrFace, renderList, renderListLength) => {
        if (!this.enabled || !renderList) {
          return null;   // null means "use the list you already have"
        }
        const casters = this.shadowCasters;
        casters.length = 0;
        for (let i = 0; i < renderListLength; i++) {
          const mesh = renderList[i];
          if ((mesh as any)[ParkedMeshFilter.FLAG] !== true) {
            casters.push(mesh);
          }
        }
        return casters;
      };
    }
  }

  /** F10: bypass the filter without unparking anything, so both halves of an A/B are one keypress. */
  setEnabled(enabled: boolean): void {
    this.enabled = enabled;
  }

  isEnabled(): boolean {
    return this.enabled;
  }

  /** Parked meshes counted in the last filtered frame — reported by the render telemetry. */
  getParkedCount(): number {
    return this.enabled ? this.parkedCount : -1;
  }
}
