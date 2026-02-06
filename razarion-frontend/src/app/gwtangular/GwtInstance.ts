import {DecimalPosition, Index, Vertex} from "./GwtAngularFacade";

export class GwtInstance {
  static newIndex(x: number, y: number): Index {
    return new class implements Index {
      getX(): number {
        return x;
      }

      getY(): number {
        return y;
      }

      add(x: number, y: number): Index {
        return GwtInstance.newIndex(this.getX() + x, this.getY() + y);
      }

      divide(x: number, y: number): Index {
        return GwtInstance.newIndex(
          this.getX() / x,
          this.getY() / y
        );
      }

      toString(): string {
        return `Index(${this.getX()}, ${this.getY()})`;
      }
    }
  }

  static newDecimalPosition(x: number, y: number): DecimalPosition {
    return new class implements DecimalPosition {
      getX(): number {
        return x;
      }

      getY(): number {
        return y;
      }

      add(x: number, y: number): DecimalPosition {
        return GwtInstance.newDecimalPosition(this.getX() + x, this.getY() + y);
      }

      divide(x: number, y: number): DecimalPosition {
        return GwtInstance.newDecimalPosition(
          this.getX() / x,
          this.getY() / y
        );
      }

    }
  }

  static newVertex(x: number, y: number, z: number): Vertex {
    return new class implements Vertex {
      getX(): number {
        return x;
      }

      getY(): number {
        return y;
      }

      getZ(): number {
        return z;
      }

      distance(vertex: Vertex): number {
        return Math.sqrt(Math.pow(x - vertex.getX(), 2) + Math.pow(y - vertex.getY(), 2) + Math.pow(z - vertex.getZ(), 2));
      }

    }
  }

}
