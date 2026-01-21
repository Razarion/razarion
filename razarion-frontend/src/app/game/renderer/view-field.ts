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
  private readonly screenCenter: DecimalPosition;
  private center?: DecimalPosition;

  constructor(bottomLeft: Vector3, bottomRight: Vector3, topRight: Vector3, topLeft: Vector3, screenCenter: Vector3) {
    this.bottomLeft = GwtInstance.newDecimalPosition(bottomLeft.x, bottomLeft.z);
    this.bottomRight = GwtInstance.newDecimalPosition(bottomRight.x, bottomRight.z);
    this.topRight = GwtInstance.newDecimalPosition(topRight.x, topRight.z);
    this.topLeft = GwtInstance.newDecimalPosition(topLeft.x, topLeft.z);
    this.screenCenter = GwtInstance.newDecimalPosition(screenCenter.x, screenCenter.z);
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

  getScreenCenter(): DecimalPosition {
    return this.screenCenter;
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

  contains(position: DecimalPosition): boolean {
    const x = position.getX();
    const y = position.getY();

    const corners = [
      {x: this.bottomLeft.getX(), y: this.bottomLeft.getY()},
      {x: this.bottomRight.getX(), y: this.bottomRight.getY()},
      {x: this.topRight.getX(), y: this.topRight.getY()},
      {x: this.topLeft.getX(), y: this.topLeft.getY()}
    ];

    let inside = false;
    for (let i = 0, j = corners.length - 1; i < corners.length; j = i++) {
      const xi = corners[i].x, yi = corners[i].y;
      const xj = corners[j].x, yj = corners[j].y;

      if (((yi > y) !== (yj > y)) && (x < (xj - xi) * (y - yi) / (yj - yi) + xi)) {
        inside = !inside;
      }
    }
    return inside;
  }

  /**
   * Calculates the inner axis-aligned bounding box rectangle.
   * This is a rectangle inscribed within the view field quadrilateral.
   */
  calculateInnerAabbRectangle(): { x: number, y: number, width: number, height: number } {
    const blX = this.bottomLeft.getX();
    const blY = this.bottomLeft.getY();
    const brX = this.bottomRight.getX();
    const brY = this.bottomRight.getY();
    const trY = this.topRight.getY();

    const width = Math.sqrt((brX - blX) * (brX - blX) + (brY - blY) * (brY - blY));
    const height = Math.abs(brY - trY);

    return { x: blX, y: blY, width, height };
  }

  getAngleTo(position: DecimalPosition): number {
    const normalizedX = position.getX() - this.screenCenter.getX();
    const normalizedY = position.getY() - this.screenCenter.getY();

    const QUARTER_RADIANT = Math.PI / 2;
    const HALF_RADIANT = Math.PI;
    const THREE_QUARTER_RADIANT = 3 * Math.PI / 2;

    if (normalizedY === 0.0) {
      if (normalizedX > 0.0) {
        return 0;
      } else {
        return HALF_RADIANT;
      }
    }
    if (normalizedX === 0.0) {
      if (normalizedY > 0.0) {
        return QUARTER_RADIANT;
      } else {
        return THREE_QUARTER_RADIANT;
      }
    }
    if (normalizedX > 0 && normalizedY > 0) {
      // Quadrant 1
      return Math.atan(normalizedY / normalizedX);
    } else if (normalizedX < 0 && normalizedY > 0) {
      // Quadrant 2
      return QUARTER_RADIANT + Math.atan(-normalizedX / normalizedY);
    } else if (normalizedX < 0 && normalizedY < 0) {
      // Quadrant 3
      return HALF_RADIANT + Math.atan(-normalizedY / -normalizedX);
    } else {
      // Quadrant 4
      return THREE_QUARTER_RADIANT + Math.atan(normalizedX / -normalizedY);
    }
  }

  toString(): string {
    return `bottomLeft: ${this.bottomLeft.toString()}
    bottomRight : ${this.bottomRight.toString()}
    topRight ${this.topRight.toString()}
    topLeft ${this.topLeft.toString()}`;
  }
}
