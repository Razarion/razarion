import {Component, OnInit} from '@angular/core';
import {READ_TERRAIN_SLOPE_POSITIONS, SLOPE_EDITOR_PATH} from "../../common";
import {HttpClient} from "@angular/common/http";
import {GwtAngularService} from "../../gwtangular/GwtAngularService";
import {MessageService} from "primeng/api";
import {ObjectNameId, TerrainSlopeCorner, TerrainSlopePosition} from "../../gwtangular/GwtAngularFacade";
import {RazarionMetadataType, ThreeJsRendererServiceImpl} from "../../game/renderer/three-js-renderer-service.impl";
import {Mesh, PointerEventTypes, PolygonMeshBuilder, Vector2} from "@babylonjs/core";
import {SimpleMaterial} from "@babylonjs/materials";

import {BabylonJsUtils} from "../../game/renderer/babylon-js.utils";

@Component({
  selector: 'slope-editor',
  templateUrl: './slope-editor.component.html'
})
export class SlopeEditorComponent implements OnInit {
  slopeConfigs: any[] = [];
  newSlopeConfigId: number | undefined;
  onOffOptions: any[] = [{label: 'Off', value: false}, {label: 'On', value: true}];
  newSlopeMode: boolean = false;
  private terrainSlopePositions!: TerrainSlopePosition[];
  selectedTerrainSlopePosition: TerrainSlopePosition | undefined;
  selectedTerrainSlopePolygon: Vector2[] | undefined;
  selectedTerrainSlopeMesh: Mesh | undefined;
  private static readonly EAR_CUT = require('earcut');// Import not working


  constructor(private httpClient: HttpClient,
              private gwtAngularService: GwtAngularService,
              private messageService: MessageService,
              private renderService: ThreeJsRendererServiceImpl) {
    const url = `${READ_TERRAIN_SLOPE_POSITIONS}${gwtAngularService.gwtAngularFacade.gameUiControl.getPlanetConfig().getId()}`;
    this.httpClient.get(url).subscribe({
      next: (value) => {
        this.terrainSlopePositions = <TerrainSlopePosition[]>value;
      },
      error: error => {
        console.error(error);
        this.messageService.add({
          severity: 'Slope Load Error',
          summary: `Error calling: ${url}`,
          detail: error,
          sticky: true
        });
      }
    });

  }

  ngOnInit(): void {
    this.slopeConfigs = [];
    const url = `${SLOPE_EDITOR_PATH}/objectNameIds`;
    this.httpClient.get<ObjectNameId[]>(url).subscribe({
      next: objectNameIds => {
        objectNameIds.forEach(objectNameId => this.slopeConfigs.push({
          label: `${objectNameId.internalName} '${objectNameId.id}'`,
          value: objectNameId.id,
        }));
        this.newSlopeConfigId = objectNameIds[0].id;
      },
      error: error => {
        console.error(error);
        this.messageService.add({
          severity: 'Slope Load Error',
          summary: `Error getObjectNameIds: ${url}`,
          detail: error,
          sticky: true
        });
      }
    });
  }

  activate() {
    this.createPolygonMeshes(this.terrainSlopePositions);
    this.renderService.getScene().onPointerObservable.add((pointerInfo) => {
      switch (pointerInfo.type) {
        case PointerEventTypes.POINTERDOWN: {
          let pickingInfo = this.renderService.setupMeshPickPoint();
          if (!pickingInfo.hit) {
            return
          }

          if (this.newSlopeMode) {
            if (this.selectedTerrainSlopePosition) {
              // Add new corner
              this.selectedTerrainSlopeMesh!.dispose();
              SlopeEditorComponent.addPointToPolygon(new Vector2(pickingInfo.pickedPoint!.x, pickingInfo.pickedPoint!.z), this.selectedTerrainSlopePolygon!);
              this.selectedTerrainSlopeMesh = this.createPolygonMesh(this.selectedTerrainSlopePolygon!, this.selectedTerrainSlopePosition);
            } else {
              // Create TerrainSlopePosition
              let halfEdgeLength = 5;
              let center = new Vector2(pickingInfo.pickedPoint!.x, pickingInfo.pickedPoint!.z);
              let bottomLeft = new Vector2(center.x - halfEdgeLength, center.y - halfEdgeLength);
              let bottomRight = new Vector2(center.x + halfEdgeLength, center.y - halfEdgeLength);
              let topRight = new Vector2(center.x + halfEdgeLength, center.y + halfEdgeLength);
              let topLeft = new Vector2(center.x - halfEdgeLength, center.y + halfEdgeLength);

              this.selectedTerrainSlopePolygon = [bottomLeft, bottomRight, topRight, topLeft];
              let slopeConfigId = this.newSlopeConfigId!;
              this.selectedTerrainSlopePosition = new class implements TerrainSlopePosition {
                children = [];
                editorParentId = null;
                id = null;
                inverted = false;
                polygon = [
                  new class implements TerrainSlopeCorner {
                    position = bottomLeft;
                    slopeDrivewayId = null;
                  },
                  new class implements TerrainSlopeCorner {
                    position = bottomRight;
                    slopeDrivewayId = null;
                  },
                  new class implements TerrainSlopeCorner {
                    position = topRight;
                    slopeDrivewayId = null;
                  },
                  new class implements TerrainSlopeCorner {
                    position = topLeft;
                    slopeDrivewayId = null;
                  },
                ];
                slopeConfigId = slopeConfigId;
              }
              this.selectedTerrainSlopeMesh = this.createPolygonMesh(this.selectedTerrainSlopePolygon!, this.selectedTerrainSlopePosition);
            }
          } else {
            // Select TerrainSlopePosition
            let pickedMesh = pickingInfo.pickedMesh;
            let razarionMetadata = ThreeJsRendererServiceImpl.getRazarionMetadata(pickedMesh!);
            if (razarionMetadata && razarionMetadata.editorHintSlopePosition) {
              this.selectedTerrainSlopeMesh = <Mesh>pickedMesh;
              this.selectedTerrainSlopePolygon = razarionMetadata.editorHintSlopePolygon;
              this.selectedTerrainSlopePosition = razarionMetadata.editorHintSlopePosition;
            } else {
              this.selectedTerrainSlopeMesh = undefined;
              this.selectedTerrainSlopePolygon = undefined;
              this.selectedTerrainSlopePosition = undefined;
            }
          }
        }
      }
    });
  }

  deactivate() {

  }

  private createPolygonMesh(polygon: Vector2[], terrainSlopePosition: TerrainSlopePosition): Mesh {
    let height = this.gwtAngularService.gwtAngularFacade.terrainTypeService.calculateGroundHeight(terrainSlopePosition.slopeConfigId);
    let parentTerrainSlopePosition = terrainSlopePosition.editorParentId && this.findTerrainSlopePosition(terrainSlopePosition.editorParentId!);
    while (parentTerrainSlopePosition) {
      height += this.gwtAngularService.gwtAngularFacade.terrainTypeService.calculateGroundHeight(parentTerrainSlopePosition.slopeConfigId);
      parentTerrainSlopePosition = parentTerrainSlopePosition.editorParentId && this.findTerrainSlopePosition(parentTerrainSlopePosition.editorParentId!);
    }

    const polygonMeshBuilder = new PolygonMeshBuilder(`Editor Slope`, polygon, this.renderService.getScene(), SlopeEditorComponent.EAR_CUT);
    const polygonMesh = polygonMeshBuilder.build();
    polygonMesh.material = new SimpleMaterial("Slope", this.renderService.getScene());
    polygonMesh.position.y = height;
    ThreeJsRendererServiceImpl.setRazarionMetadataSimple(polygonMesh, RazarionMetadataType.EDITOR_SLOPE); // TODO set config id
    let razarionMetadata = ThreeJsRendererServiceImpl.getRazarionMetadata(polygonMesh);
    razarionMetadata!.editorHintSlopePolygon = polygon;
    razarionMetadata!.editorHintSlopePosition = terrainSlopePosition;
    return polygonMesh;
  }

  private findTerrainSlopePosition(id: number) {
    return this.terrainSlopePositions.find(terrainSlopePosition => terrainSlopePosition.id === id);
  }

  public static addPointToPolygon(point: Vector2, polygon: Vector2[]) {
    let index = SlopeEditorComponent.projectPointToPolygon(point, polygon);
    if (!index && index !== 0) {
      throw new Error("Invalid Polygon");
    }
    polygon.splice(index, 0, point);
  }

  public static getCorrectedIndex(index: number, listSize: number): number {
    let correctedIndex = index % listSize;
    if (correctedIndex < 0) {
      correctedIndex += listSize;
    }
    return correctedIndex;
  }

  public static getArrayCorrectedIndex(index: number, array: any[]): any {
    return array[SlopeEditorComponent.getCorrectedIndex(index, array.length)];
  }

  public static projectPointOnLine(point: Vector2, lineStart: Vector2, lineEnd: Vector2): Vector2 {
    const line = [lineEnd.x - lineStart.x, lineEnd.y - lineStart.y];
    const lineLengthSquared = line[0] * line[0] + line[1] * line[1];

    if (lineLengthSquared === 0) {
      return lineStart;
    }

    const t = ((point.x - lineStart.x) * line[0] + (point.y - lineStart.y) * line[1]) / lineLengthSquared;
    let x = lineStart.x + t * line[0];
    let y = lineStart.y + t * line[1];
    x = SlopeEditorComponent.clamp(x, Math.min(lineStart.x, lineEnd.x), Math.max(lineStart.x, lineEnd.x))
    y = SlopeEditorComponent.clamp(y, Math.min(lineStart.y, lineEnd.y), Math.max(lineStart.y, lineEnd.y))
    return new Vector2(x, y);
  }

  public static clamp(num: number, min: number, max: number): number {
    return Math.min(Math.max(num, min), max);
  };

  public static projectPointToPolygon(point: Vector2, polygon: Vector2[]): number | null {
    let closestPoint = null;
    let closestDistanceSquared = Infinity;

    let index = null;

    for (let i = 0; i < polygon.length; i++) {
      const currentLineStart = polygon[i];
      const currentLineEnd = SlopeEditorComponent.getArrayCorrectedIndex(i + 1, polygon);

      const projectedPoint = SlopeEditorComponent.projectPointOnLine(point, currentLineStart, currentLineEnd);

      const distanceSquared = (point.x - projectedPoint.x) * (point.x - projectedPoint.x) +
        (point.y - projectedPoint.y) * (point.y - projectedPoint.y);

      if (distanceSquared < closestDistanceSquared) {
        closestPoint = projectedPoint;
        closestDistanceSquared = distanceSquared;
        index = i;
      }
    }

    if (index == null) {
      return null;
    }
    return SlopeEditorComponent.getCorrectedIndex(index + 1, polygon.length);
  }

  private createPolygonMeshes(terrainSlopePositions: TerrainSlopePosition[]) {
    terrainSlopePositions.forEach(terrainSlopePosition => {
      if (terrainSlopePosition.polygon) {
        let polygon = BabylonJsUtils.toVertex2ArrayFromTerrainSlopeCorner(terrainSlopePosition.polygon);
        this.createPolygonMesh(polygon, terrainSlopePosition);
        if (terrainSlopePosition.children) {
          this.createPolygonMeshes(terrainSlopePosition.children);
        }
      }
    });
  }
}
