import { Vector2, Vector3 } from "@babylonjs/core";
import { UpDownMode } from "../shape-terrain-editor.component";

export class AbstractBrush {

    diameter: number = 20;
    falloff: number = 7;
    height: number = 10;
    random: number = 1.5;
    upDownMode: UpDownMode = UpDownMode.UP;

    calculateHeight(mousePosition: Vector3, oldPosition: Vector3): number | null {
        const radius = this.diameter;
        let distance = Vector2.Distance(new Vector2(oldPosition.x, oldPosition.z), new Vector2(mousePosition.x, mousePosition.z));
        if (distance < (radius + this.falloff)) {
            let newHeight: number | null = null;
            if (distance <= radius) {
                newHeight = this.height;
            } else {
                const newValue = (this.height / this.falloff) * (this.falloff + radius - distance) + this.random * (Math.random() - 0.5) * 2.0;
                if (this.upDownMode === UpDownMode.DOWN) {
                    if (oldPosition.y > newValue) {
                        newHeight = newValue;
                    }
                    if (oldPosition.y < this.height) {
                        newHeight = this.height;
                    }
                } else if (this.upDownMode === UpDownMode.UP) {
                    if (oldPosition.y < newValue) {
                        newHeight = newValue;
                    }
                    if (oldPosition.y > this.height) {
                        newHeight = this.height;
                    }
                } else {
                    newHeight = newValue;
                }
            }
            // TODO if (vP.y < this._minY) {
            //   vP.y = this._minY;
            // } else if (vP.y > this._maxY) {
            //   vP.y = this._maxY;
            // }
            return newHeight
        } else {
            return null;
        }
    }
}
