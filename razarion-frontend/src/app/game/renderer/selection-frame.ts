import {MeshBuilder, Scene, Vector2, Vector3} from "@babylonjs/core";
import {LinesMesh} from "@babylonjs/core/Meshes/linesMesh";

export class SelectionFrame {
  private mousePos0: Vector2 | undefined;
  private mousePos1: Vector2 | undefined;
  private lines: LinesMesh | undefined;

  constructor(private scene: Scene) {
  }

  onPointerDown(x: number, y: number) {
    this.mousePos0 = new Vector2(x, y);
  }

  onPointerMove(x: number, y: number) {
    if (!this.mousePos0) {
      return;
    }

    if (!this.lines) {
      this.lines = this.setupLines();
    }

    this.mousePos1 = new Vector2(x, y);
    let rayBL = this.scene.createPickingRay(this.mousePos0.x, this.mousePos0.y, null, null)
    let rayBR = this.scene.createPickingRay(this.mousePos1.x, this.mousePos0.y, null, null)
    let rayTL = this.scene.createPickingRay(this.mousePos0.x, this.mousePos1.y, null, null)

    let bl = rayBL.origin.add(rayBL.direction);
    let br = rayBR.origin.add(rayBR.direction);
    let tl = rayTL.origin.add(rayTL.direction);

    this.lines.position.copyFrom(bl);
    this.lines.scaling.x = Vector3.Distance(bl, br);
    this.lines.scaling.y = -Vector3.Distance(bl, tl);

    this.lines.lookAt(br, -Math.PI * 0.5);

    if (this.mousePos1.y < this.mousePos0.y) {
      this.lines.scaling.y *= -1;
    }
  }

  onPointerUp(x: number, y: number) {
    if (this.lines) {
      this.lines.dispose();
      this.lines = undefined;
    }
    this.mousePos0 = undefined;
  }

  private setupLines() {
    let lines = MeshBuilder.CreateLines("Selection Frame",
      {
        points: [
          new Vector3(0, 0, 0),
          new Vector3(1, 0, 0),
          new Vector3(1, 1, 0),
          new Vector3(0, 1, 0),
          new Vector3(0, 0, 0)
        ]
      }, this.scene);
    lines.billboardMode = 7;
    return lines;
  }
}
