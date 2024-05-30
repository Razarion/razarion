import { Vector3 } from "@babylonjs/core";
import { ShapeTerrainEditorComponent } from "../shape-terrain-editor.component";
import { EditorTerrainTile } from "../editor-terrain-tile";

export abstract class AbstractBrush {
    shapeTerrainEditorComponent!: ShapeTerrainEditorComponent;

    abstract calculateHeight(mousePosition: Vector3, oldPosition: Vector3, avgHeight: number | undefined): number | null;

    isInRadius(mousePosition: Vector3, oldPosition: Vector3): boolean {
        return false;
    }
}
