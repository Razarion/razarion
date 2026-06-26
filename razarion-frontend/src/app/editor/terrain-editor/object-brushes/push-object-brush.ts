import {Vector3} from "@babylonjs/core";
import {AbstractObjectBrush, ObjectBrushContext} from "./abstract-object-brush";

/**
 * Shoves terrain objects out of the brush disc, like a snow plow: every object whose centre lies inside
 * the radius is pushed radially outward to just past the brush edge, with its height re-snapped to the
 * terrain. Dragging the brush therefore carves a cleared lane and heaps the objects up along its sides.
 */
export class PushObjectBrush extends AbstractObjectBrush {
  /** Distance below which an object counts as "at the centre" and is shoved in an arbitrary direction. */
  private static readonly CENTRE_EPS = 1e-4;
  /** Extra margin (m) pushed beyond the brush edge so objects clear the disc and aren't re-grabbed. */
  private static readonly PUSH_MARGIN = 1;

  constructor(ctx: ObjectBrushContext) {
    super(ctx);
  }

  apply(center: Vector3): void {
    const target = this.radius + PushObjectBrush.PUSH_MARGIN;
    for (const node of this.ctx.terrainObjectNodes()) {
      const dx = node.position.x - center.x;
      const dz = node.position.z - center.z;
      const dist = Math.sqrt(dx * dx + dz * dz);
      if (dist >= this.radius) {
        continue; // already outside the plow
      }
      let nx: number;
      let nz: number;
      if (dist < PushObjectBrush.CENTRE_EPS) {
        // Object sitting on the brush centre has no push direction: shove it out along +x.
        nx = center.x + target;
        nz = center.z;
      } else {
        const scale = target / dist;
        nx = center.x + dx * scale;
        nz = center.z + dz * scale;
      }
      this.ctx.moveObjectNode(node, nx, nz);
    }
  }
}
