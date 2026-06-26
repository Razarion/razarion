import {TransformNode, Vector3} from "@babylonjs/core";

/**
 * Everything an object brush needs from the editor it runs in. The editor (ObjectTerrainEditorComponent)
 * implements this so the brushes stay free of the renderer/REST wiring and only deal with placement
 * geometry. All coordinates are Babylon world coordinates (x, z = ground plane; y = height).
 */
export interface ObjectBrushContext {
  /** Ground height at world (x, z), or null if there is no ground there (e.g. water / off-map). */
  groundHeightAt(x: number, z: number): number | null;

  /** All placed terrain-object root nodes currently in the scene (persisted + pending-new). */
  terrainObjectNodes(): TransformNode[];

  /** Config id of a terrain-object node (undefined if it carries none). */
  configIdOf(node: TransformNode): number | undefined;

  /** Currently selected terrain-object config id (from the Object dropdown), or null if none. */
  selectedConfigId(): number | null;

  /**
   * Config ids the scatter/mountain brush draws from; each placement picks one at random so a stroke
   * can mix several object types. Empty means nothing is selected (the brush places nothing).
   */
  scatterConfigIds(): number[];

  /**
   * Create one terrain object at world (x, z) with the given ground height, yaw (rad, around the
   * vertical axis) and uniform scale. If tiltNormal is given the object is additionally tilted so its
   * up-axis follows the terrain normal. Handles mesh creation and pending-list bookkeeping.
   */
  createObject(configId: number, x: number, z: number, height: number, yaw: number, scale: number, tiltNormal?: Vector3): void;

  /** Remove one terrain-object node, handling mesh dispose and pending-list bookkeeping. */
  deleteObjectNode(node: TransformNode): void;

  /**
   * Move one terrain-object node to world (x, z), re-snapping its height to the terrain, and register
   * the move so it is persisted on save (handles both persisted and pending-new objects).
   */
  moveObjectNode(node: TransformNode, x: number, z: number): void;
}

export abstract class AbstractObjectBrush {
  /** Brush radius in meters (also drives the cursor disc and the stroke dab spacing). */
  radius = 20;

  protected constructor(protected readonly ctx: ObjectBrushContext) {
  }

  /** Apply the brush once, centred on the given world position (one "dab" of a stroke). */
  abstract apply(center: Vector3): void;

  /** Uniformly distributed random point inside the brush disc around `center` (x/z plane). */
  protected randomPointInDisc(center: Vector3): { x: number, z: number } {
    const angle = 2 * Math.PI * Math.random();
    const r = this.radius * Math.sqrt(Math.random()); // sqrt → uniform over area, not clustered at centre
    return {x: center.x + r * Math.cos(angle), z: center.z + r * Math.sin(angle)};
  }
}
