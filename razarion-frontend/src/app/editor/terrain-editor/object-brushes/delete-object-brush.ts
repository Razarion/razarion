import {Vector3} from "@babylonjs/core";
import {AbstractObjectBrush, ObjectBrushContext} from "./abstract-object-brush";

/**
 * Removes terrain objects under the brush. Paint over an area to delete every object whose root sits
 * within the radius. With `onlySelectedType` only objects of the currently selected config are removed.
 */
export class DeleteObjectBrush extends AbstractObjectBrush {
  onlySelectedType = false;

  constructor(ctx: ObjectBrushContext) {
    super(ctx);
  }

  apply(center: Vector3): void {
    const r2 = this.radius * this.radius;
    const selectedId = this.ctx.selectedConfigId();
    // Snapshot the list first: deleteObjectNode disposes nodes, which mutates the scene's node array.
    for (const node of this.ctx.terrainObjectNodes()) {
      const dx = node.position.x - center.x;
      const dz = node.position.z - center.z;
      if (dx * dx + dz * dz > r2) {
        continue;
      }
      if (this.onlySelectedType && selectedId !== null && this.ctx.configIdOf(node) !== selectedId) {
        continue;
      }
      this.ctx.deleteObjectNode(node);
    }
  }
}
