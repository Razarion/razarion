import {DecimalPosition} from "../../gwtangular/GwtAngularFacade";
import {Vector3} from "@babylonjs/core";
import {GwtInstance} from "../../gwtangular/GwtInstance";

export interface ViewFieldListener {
  onViewFieldChanged(viewField: ViewField): void;
}

export class ViewField {
  private readonly bottomLeft: DecimalPosition;
  private readonly bottomRight: DecimalPosition;
  private readonly topRight: DecimalPosition;
  private readonly topLeft: DecimalPosition;
  private center?: DecimalPosition;

  constructor(bottomLeft: Vector3, bottomRight: Vector3, topRight: Vector3, topLeft: Vector3) {
    this.bottomLeft = GwtInstance.newDecimalPosition(bottomLeft.x, bottomLeft.z);
    this.bottomRight = GwtInstance.newDecimalPosition(bottomRight.x, bottomRight.z);
    this.topRight = GwtInstance.newDecimalPosition(topRight.x, topRight.z);
    this.topLeft = GwtInstance.newDecimalPosition(topLeft.x, topLeft.z);
  }

  getBottomLeft(): DecimalPosition {
    return this.bottomLeft;
  }

  getBottomRight(): DecimalPosition {
    return this.bottomRight;
  }

  getTopRight(): DecimalPosition {
    return this.topRight;
  }

  getTopLeft(): DecimalPosition {
    return this.topLeft;
  }

  getCenter(): DecimalPosition {
    if (!this.center) {
      this.center = GwtInstance.newDecimalPosition(
        (this.bottomLeft.getX() + this.bottomRight.getX() + this.topRight.getX() + this.topLeft.getX()) / 4,
        (this.bottomLeft.getY() + this.bottomRight.getY() + this.topRight.getY() + this.topLeft.getY()) / 4
      );
    }
    return this.center;
  }

  toString(): string {
    return `bottomLeft: ${this.bottomLeft.toString()}
    bottomRight : ${this.bottomRight.toString()}
    topRight ${this.topRight.toString()}
    topLeft ${this.topLeft.toString()}`;
  }
}
