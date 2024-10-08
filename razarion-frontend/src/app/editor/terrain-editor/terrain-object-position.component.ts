import {Component} from "@angular/core";
import {TransformNode, Vector3} from "@babylonjs/core";
import {TerrainObjectPosition} from "../../generated/razarion-share";

@Component({
  selector: 'terrain-object-position',
  template: `
    <table style="border-spacing: 0; border-collapse: separate; margin: 5px">
      <tr>
        <td>Position</td>
        <td>
          <vector3-editor [vector3]="position" disabled="!position"></vector3-editor>
        </td>
      </tr>
      <tr>
        <td>Rotation</td>
        <td>
          <angle-vertex3-editor [euler]="rotation" disabled="!rotation"></angle-vertex3-editor>
        </td>
      </tr>
      <tr>
        <td>Scale</td>
        <td>
          <vector3-editor [vector3]="scale" disabled="!scale"></vector3-editor>
        </td>
      </tr>
    </table>
  `
})
export class TerrainObjectPositionComponent {
  position: Vector3 | null = null;
  rotation: Vector3 | null = null;
  scale: Vector3 | null = null;
  transformNode: TransformNode | null = null;
  terrainObjectPosition: TerrainObjectPosition | null = null;

  setSelected(transformNode: TransformNode, terrainObjectPosition: TerrainObjectPosition) {
    this.position = transformNode.position
    this.rotation = transformNode.rotation;
    this.scale = transformNode.scaling;
    this.transformNode = transformNode;
    this.terrainObjectPosition = terrainObjectPosition;
  }

  getTerrainObjectPosition(): TerrainObjectPosition | null {
    return this.terrainObjectPosition;
  }

  getTransformNode(): TransformNode | null {
    return this.transformNode;
  }

  clearSelection() {
    this.position = null;
    this.rotation = null;
    this.scale = null;
    this.transformNode = null;
    this.terrainObjectPosition = null;
  }
}
