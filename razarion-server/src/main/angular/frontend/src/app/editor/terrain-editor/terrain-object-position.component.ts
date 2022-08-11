import {Component} from "@angular/core";
import {Vector3} from "three/src/math/Vector3";
import {Euler} from "three/src/math/Euler";
import {Object3D} from "three";
import {TerrainObjectPosition} from "../../gwtangular/GwtAngularFacade";
import {GwtInstance} from "../../gwtangular/GwtInstance";

@Component({
  selector: 'terrain-object-position',
  template: `
    <table style="border-spacing: 0; border-collapse: separate; margin: 5px">
      <tr>
        <td>Position</td>
        <td>
          <vertex-editor [vertex]="position" (onInput)="positionChanged($event)"></vertex-editor>
        </td>
      </tr>
      <tr>
        <td>Rotation</td>
        <td>
          <angle-vertex-editor [euler]="rotation" (onInput)="rotationChanged($event)"></angle-vertex-editor>
        </td>
      </tr>
      <tr>
        <td>Scale</td>
        <td>
          <vertex-editor [vertex]="scale" (onInput)="scaleChanged($event)"></vertex-editor>
        </td>
      </tr>
      <tr>
        <td>Offset</td>
        <td>
          <vertex-editor [vertex]="offset" (onInput)="offsetChanged($event)"></vertex-editor>
        </td>
      </tr>
    </table>
  `
})
export class TerrainObjectPositionComponent {
  position = new Vector3(0, 0, 0);
  rotation = new Euler(0, 0, 0);
  scale = new Vector3(1, 1, 1);
  offset = new Vector3(0, 0, 0);
  terrainObjectPosition!: TerrainObjectPosition;

  init(object3D: Object3D, terrainObjectPosition: TerrainObjectPosition) {
    this.position = object3D.position;
    this.rotation = object3D.rotation;
    this.scale = object3D.scale;
    this.terrainObjectPosition = terrainObjectPosition;
  }

  positionChanged(vector3: Vector3) {
    this.terrainObjectPosition.setPosition(GwtInstance.newDecimalPosition(vector3.x, vector3.y))
  }

  rotationChanged(euler: Euler) {
    this.terrainObjectPosition.setRotation(GwtInstance.newVertex(euler.x, euler.y, euler.z))
  }

  scaleChanged(vector3: Vector3) {
    this.terrainObjectPosition.setScale(GwtInstance.newVertex(vector3.x, vector3.y, vector3.z))
  }

  offsetChanged(vector3: Vector3) {
    this.terrainObjectPosition.setOffset(GwtInstance.newVertex(vector3.x, vector3.y, vector3.z))
  }
}
