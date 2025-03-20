import { TransformNode } from "@babylonjs/core";
import { TerrainObjectModel } from "src/app/gwtangular/GwtAngularFacade";

export class GeneratedTerrainObjects {
    generatedObjects: { mesh: TransformNode, model: TerrainObjectModel }[] = [];
    date: Date = new Date();
    terrainObjectConfigs = "";
    count = 0;
}