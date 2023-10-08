import {Component, OnInit} from '@angular/core';
import {
  DRIVEWAY_EDITOR_PATH,
  READ_TERRAIN_SLOPE_POSITIONS,
  SLOPE_EDITOR_PATH,
  UPDATE_SLOPES_TERRAIN_EDITOR
} from "../../common";
import {HttpClient} from "@angular/common/http";
import {GwtAngularService} from "../../gwtangular/GwtAngularService";
import {MessageService} from "primeng/api";
import {
  ObjectNameId,
  SlopeTerrainEditorUpdate,
  TerrainSlopeCorner,
  TerrainSlopePosition
} from "../../gwtangular/GwtAngularFacade";
import {
  BabylonRenderServiceAccessImpl,
  RazarionMetadataType
} from "../../game/renderer/babylon-render-service-access-impl.service";
import {
  AxisDragGizmo,
  HighlightLayer,
  Mesh,
  MeshBuilder,
  Node,
  Nullable,
  Observer,
  PointerEventTypes,
  PointerInfo,
  PolygonMeshBuilder,
  Tools,
  Vector2,
  Vector3
} from "@babylonjs/core";
import {SimpleMaterial} from "@babylonjs/materials";

import {BabylonJsUtils} from "../../game/renderer/babylon-js.utils";
import {Color3} from "@babylonjs/core/Maths/math.color";
import {pointInPolygon} from "geometric";
import {EditorService} from "../editor-service";

@Component({
  selector: 'slope-terrain-editor',
  templateUrl: './slope-terrain-editor.component.html'
})
export class SlopeTerrainEditorComponent implements OnInit {
  public static readonly EAR_CUT = require('earcut');// Import not working
  readonly SELECTION_BOOST = 0.01;
  slopeConfigs: any[] = [];
  newSlopeConfigId: number | undefined;
  onOffOptions: any[] = [{label: 'Off', value: false}, {label: 'On', value: true}];
  newSlopeMode: boolean = false;
  clickToAddCornerMode: boolean = false;
  private terrainSlopePositions!: TerrainSlopePosition[];
  selectedTerrainSlopePosition: TerrainSlopePosition | undefined;
  selectedTerrainSlopePolygon: Vector2[] | undefined;
  selectedTerrainSlopeMesh: Mesh | undefined;

  private readonly slopeMaterial;
  private readonly cornerDiscMaterial;
  minCornerSelectionDistance = 5;
  driveways: any[] = [];
  draggableCorner: DraggableCorner | undefined;
  private slopeTerrainEditorUpdate: SlopeTerrainEditorUpdate | undefined;
  private highlightLayer: HighlightLayer;
  private pointerObservable: Nullable<Observer<PointerInfo>> = null;


  constructor(private httpClient: HttpClient,
              public gwtAngularService: GwtAngularService,
              private messageService: MessageService,
              private renderService: BabylonRenderServiceAccessImpl,
              private editorService: EditorService) {
    const url = `${READ_TERRAIN_SLOPE_POSITIONS}/${gwtAngularService.gwtAngularFacade.gameUiControl.getPlanetConfig().getId()}`;
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
    this.cornerDiscMaterial = new SimpleMaterial(`Slope Editor Corner`, renderService.getScene());
    this.cornerDiscMaterial.diffuseColor = Color3.Yellow();
    this.cornerDiscMaterial.backFaceCulling = false;

    this.slopeMaterial = new SimpleMaterial("Slope Editor", this.renderService.getScene());
    this.slopeMaterial.diffuseColor = Color3.Red();
    this.slopeMaterial.alpha = 0.4;
    this.slopeMaterial.backFaceCulling = false;


    this.createSlopeTerrainEditorUpdate()

    this.highlightLayer = new HighlightLayer("Slope Mesh HighlightLayer", renderService.getScene());
  }

  private createSlopeTerrainEditorUpdate() {
    this.slopeTerrainEditorUpdate = new class implements SlopeTerrainEditorUpdate {
      createdSlopes: TerrainSlopePosition[] = [];
      deletedSlopeIds: number[] = [];
      updatedSlopes: TerrainSlopePosition[] = [];
    };
  }

  ngOnInit(): void {
    this.loadSlopeObjectNameIds();
    this.loadDrivewayNameIds();
  }

  private loadSlopeObjectNameIds() {
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

  private loadDrivewayNameIds() {
    this.driveways = [];
    const url = `${DRIVEWAY_EDITOR_PATH}/objectNameIds`;
    this.httpClient.get<ObjectNameId[]>(url).subscribe({
      next: objectNameIds => {
        objectNameIds.forEach(objectNameId => this.driveways.push({
          label: `${objectNameId.internalName} '${objectNameId.id}'`,
          value: objectNameId.id,
        }));
        this.driveways.push({
          label: `-`,
          value: null,
        })
      },
      error: error => {
        console.error(error);
        this.messageService.add({
          severity: 'Driveway Load Error',
          summary: `Error getObjectNameIds: ${url}`,
          detail: error,
          sticky: true
        });
      }
    });
  }

  deleteSlope() {
    if (this.selectedTerrainSlopePosition!.id) {
      if (!this.slopeTerrainEditorUpdate!.deletedSlopeIds.includes(this.selectedTerrainSlopePosition!.id)) {
        this.slopeTerrainEditorUpdate!.deletedSlopeIds.push(this.selectedTerrainSlopePosition!.id);
      }
    } else {
      let razarionMetadata = BabylonRenderServiceAccessImpl.getRazarionMetadata(this.selectedTerrainSlopeMesh!);
      this.slopeTerrainEditorUpdate!.createdSlopes.splice(this.slopeTerrainEditorUpdate!.createdSlopes.indexOf(razarionMetadata!.editorHintSlopePosition!), 1)
    }

    this.deleteChildSlopes(this.selectedTerrainSlopeMesh!);

    this.selectedTerrainSlopeMesh!.dispose();
    this.selectedTerrainSlopeMesh = undefined;
  }

  deleteChildSlopes(slope: Mesh) {
    let razarionMetadata = BabylonRenderServiceAccessImpl.getRazarionMetadata(slope);
    if (!razarionMetadata || !razarionMetadata!.editorHintSlopePosition || !razarionMetadata!.editorHintSlopePosition!.children) {
      return;
    }
    razarionMetadata!.editorHintSlopePosition!.children.forEach(childTerrainSlopePosition => {
      let childSlopeMesh = this.findSlopeMesh4TerrainSlopePosition(childTerrainSlopePosition);
      if (childSlopeMesh) {
        this.deleteChildSlopes(childSlopeMesh);
        childSlopeMesh!.dispose();
      }
    });
  }

  private findSlopeMesh4TerrainSlopePosition(terrainSlopePosition: TerrainSlopePosition, node?: Node): Mesh | undefined {
    if (!node) {
      return <Mesh>this.renderService.getScene().meshes.find(mesh => {
        this.findSlopeMesh4TerrainSlopePosition(terrainSlopePosition, mesh);
      });
    }
    let metadata = BabylonRenderServiceAccessImpl.getRazarionMetadata(node)
    if (metadata && metadata.editorHintSlopePosition === terrainSlopePosition) {
      return <Mesh>node;
    }

    if (node.getChildren) {
      const children = node.getChildren();
      for (const child of children) {
        let found = this.findSlopeMesh4TerrainSlopePosition(terrainSlopePosition, child);
        if (found) {
          return found;
        }
      }
    }

    return undefined
  }


  activate() {
    this.createPolygonMeshes(this.terrainSlopePositions);
    this.pointerObservable = this.renderService.getScene().onPointerObservable.add((pointerInfo) => {
      switch (pointerInfo.type) {
        case PointerEventTypes.POINTERDOWN: {
          let pickingInfo = this.renderService.setupMeshPickPoint();
          if (!pickingInfo.hit) {
            return
          }

          if (this.newSlopeMode) {
            // Create TerrainSlopePosition
            let halfEdgeLength = 5;
            let center = new Vector2(pickingInfo.pickedPoint!.x, pickingInfo.pickedPoint!.z);
            let bottomLeft = new Vector2(center.x - halfEdgeLength, center.y - halfEdgeLength);
            let bottomRight = new Vector2(center.x + halfEdgeLength, center.y - halfEdgeLength);
            let topRight = new Vector2(center.x + halfEdgeLength, center.y + halfEdgeLength);
            let topLeft = new Vector2(center.x - halfEdgeLength, center.y + halfEdgeLength);

            this.selectedTerrainSlopePolygon = [bottomLeft, bottomRight, topRight, topLeft];
            let slopeConfigId = this.newSlopeConfigId!;
            let parentSlopeId = this.findParentSlope(center);

            this.selectedTerrainSlopePosition = new class implements TerrainSlopePosition {
              children = [];
              id = null;
              inverted = false;
              slopeConfigId = slopeConfigId;
              editorParentIdIfCreated = parentSlopeId;
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
            }
            this.slopeTerrainEditorUpdate?.createdSlopes.push(this.selectedTerrainSlopePosition)
            this.selectedTerrainSlopeMesh = this.createPolygonMesh(this.selectedTerrainSlopePolygon!, this.selectedTerrainSlopePosition);
            if (!parentSlopeId) {
              this.terrainSlopePositions.push(this.selectedTerrainSlopePosition);
            }
            this.updateHighlight();
            this.clearDraggableCorner();
            this.newSlopeMode = false;
            return;
          }

          if (this.clickToAddCornerMode && this.selectedTerrainSlopePosition) {
            // Add new corner
            this.selectedTerrainSlopeMesh!.dispose();
            SlopeTerrainEditorComponent.addPointToPolygon(new Vector2(pickingInfo.pickedPoint!.x, pickingInfo.pickedPoint!.z), this.selectedTerrainSlopePolygon!, this.selectedTerrainSlopePosition);
            this.selectedTerrainSlopeMesh = this.createPolygonMesh(this.selectedTerrainSlopePolygon!, this.selectedTerrainSlopePosition);
            BabylonJsUtils.updateTerrainSlopeCornerFromVertex2Array(this.selectedTerrainSlopePolygon!, this.selectedTerrainSlopePosition);
            this.updateHighlight();
            this.onSelectionEdited();
            return;
          }

          // Select TerrainSlopePosition
          let pickedMesh = pickingInfo.pickedMesh;
          let razarionMetadata = BabylonRenderServiceAccessImpl.getRazarionMetadata(pickedMesh!);
          if (razarionMetadata && razarionMetadata.editorHintSlopePosition) {
            if (razarionMetadata.editorHintSlopePosition === this.selectedTerrainSlopePosition) {
              this.selectNearestCorner(this.selectedTerrainSlopePolygon!, this.selectedTerrainSlopeMesh!.position.y, new Vector2(pickingInfo.pickedPoint!.x, pickingInfo.pickedPoint!.z));
            } else {
              this.selectedTerrainSlopeMesh = <Mesh>pickedMesh;
              this.selectedTerrainSlopePolygon = razarionMetadata.editorHintSlopePolygon;
              this.selectedTerrainSlopePosition = razarionMetadata.editorHintSlopePosition;
              this.clickToAddCornerMode = false;
              this.clearDraggableCorner();
              this.updateHighlight();
            }
          } else if (this.selectedTerrainSlopePolygon) {
            let hasCorner = this.selectNearestCorner(this.selectedTerrainSlopePolygon!, this.selectedTerrainSlopeMesh!.position.y, new Vector2(pickingInfo.pickedPoint!.x, pickingInfo.pickedPoint!.z));
            if (!hasCorner) {
              this.clearSelection();
            }
          } else {
            this.clearSelection();
          }
        }
      }
    });
  }

  deactivate() {
    if (this.pointerObservable) {
      this.renderService.getScene().onPointerObservable.remove(this.pointerObservable);
      this.pointerObservable = null;
    }

    this.disposeAllEditorMeshes()

    this.clearSelection();

    this.newSlopeMode = false;
  }

  private updateHighlight() {
    this.highlightLayer.removeAllMeshes();
    if (this.selectedTerrainSlopeMesh) {
      this.highlightLayer.addMesh(this.selectedTerrainSlopeMesh, Color3.Green());
    }
  }

  private clearSelection() {
    this.selectedTerrainSlopeMesh = undefined;
    this.selectedTerrainSlopePolygon = undefined;
    this.selectedTerrainSlopePosition = undefined;
    this.clickToAddCornerMode = false;
    this.clearDraggableCorner();
    this.updateHighlight();
  }

  private clearDraggableCorner() {
    if (this.draggableCorner) {
      this.draggableCorner.dispose();
      this.draggableCorner = undefined;
    }
  }

  private createPolygonMesh(polygon: Vector2[], terrainSlopePosition: TerrainSlopePosition): Mesh {
    let height = this.gwtAngularService.gwtAngularFacade.terrainTypeService.calculateGroundHeight(terrainSlopePosition.slopeConfigId);
    let parentTerrainSlopePosition = this.findParent(terrainSlopePosition);
    while (parentTerrainSlopePosition) {
      height += this.gwtAngularService.gwtAngularFacade.terrainTypeService.calculateGroundHeight(parentTerrainSlopePosition.slopeConfigId);
      parentTerrainSlopePosition = this.findParent(parentTerrainSlopePosition);
    }

    const polygonMeshBuilder = new PolygonMeshBuilder(`Editor Slope`, polygon, this.renderService.getScene(), SlopeTerrainEditorComponent.EAR_CUT);
    const polygonMesh = polygonMeshBuilder.build();
    polygonMesh.material = this.slopeMaterial;
    polygonMesh.position.y = height + this.SELECTION_BOOST;
    BabylonRenderServiceAccessImpl.setRazarionMetadataSimple(polygonMesh, RazarionMetadataType.EDITOR_SLOPE); // TODO set config id
    let razarionMetadata = BabylonRenderServiceAccessImpl.getRazarionMetadata(polygonMesh);
    razarionMetadata!.editorHintSlopePolygon = polygon;
    razarionMetadata!.editorHintSlopePosition = terrainSlopePosition;
    return polygonMesh;
  }

  private findParent(terrainSlopePosition: TerrainSlopePosition): TerrainSlopePosition | null {
    for (const parent of this.terrainSlopePositions) {
      let found = this.findChild4Parent(terrainSlopePosition, parent);
      if (found) {
        return found
      }
    }
    return null;
  }

  private findChild4Parent(terrainSlopePosition: TerrainSlopePosition, parent: TerrainSlopePosition): TerrainSlopePosition | null {
    if (!parent.children) {
      return null;
    }

    for (const child of parent.children) {
      if (child === terrainSlopePosition) {
        return parent
      }
      let found = this.findChild4Parent(terrainSlopePosition, child);
      if (found) {
        return found;
      }
    }
    return null;
  }

  public static addPointToPolygon(point: Vector2, polygon: Vector2[], selectedTerrainSlopePosition: TerrainSlopePosition) {
    let index = SlopeTerrainEditorComponent.projectPointToPolygon(point, polygon);
    if (!index && index !== 0) {
      throw new Error("Invalid Polygon");
    }
    polygon.splice(index, 0, point);
    selectedTerrainSlopePosition.polygon.splice(index, 0, new class implements TerrainSlopeCorner {
      position = {x: point.x, y: point.y};
      slopeDrivewayId = null;
    });
  }

  public static getCorrectedIndex(index: number, listSize: number): number {
    let correctedIndex = index % listSize;
    if (correctedIndex < 0) {
      correctedIndex += listSize;
    }
    return correctedIndex;
  }

  public static getArrayCorrectedIndex(index: number, array: any[]): any {
    return array[SlopeTerrainEditorComponent.getCorrectedIndex(index, array.length)];
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
    x = SlopeTerrainEditorComponent.clamp(x, Math.min(lineStart.x, lineEnd.x), Math.max(lineStart.x, lineEnd.x))
    y = SlopeTerrainEditorComponent.clamp(y, Math.min(lineStart.y, lineEnd.y), Math.max(lineStart.y, lineEnd.y))
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
      const currentLineEnd = SlopeTerrainEditorComponent.getArrayCorrectedIndex(i + 1, polygon);

      const projectedPoint = SlopeTerrainEditorComponent.projectPointOnLine(point, currentLineStart, currentLineEnd);

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
    return SlopeTerrainEditorComponent.getCorrectedIndex(index + 1, polygon.length);
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

  private selectNearestCorner(polygon: Vector2[], height: number, position: Vector2): boolean {
    let minDistance = Infinity;
    let corner: Vector2 | undefined;
    let index: number | undefined;
    for (let i = 0; i < polygon.length; i++) {
      const polygonCorner = polygon[i];
      let distance = polygonCorner.subtract(position).length()
      if (distance < minDistance && distance <= this.minCornerSelectionDistance) {
        minDistance = distance;
        corner = polygonCorner;
        index = i;
      }
    }
    if (corner) {
      if (this.draggableCorner) {
        if (this.draggableCorner.index !== index) {
          this.draggableCorner.dispose();
          this.draggableCorner = this.createDraggableCorner(index!, this.selectedTerrainSlopePosition?.polygon[index!]!, corner, height, this.minCornerSelectionDistance * 0.1);
        }
      } else {
        this.draggableCorner = this.createDraggableCorner(index!, this.selectedTerrainSlopePosition?.polygon[index!]!, corner, height, this.minCornerSelectionDistance * 0.1);
      }
    }
    return !!corner;
  }

  private onSelectionEdited() {
    if (this.slopeTerrainEditorUpdate?.createdSlopes.includes(this.selectedTerrainSlopePosition!)) {
      return;
    }

    if (!this.slopeTerrainEditorUpdate?.updatedSlopes.includes(this.selectedTerrainSlopePosition!)) {
      this.slopeTerrainEditorUpdate?.updatedSlopes.push(this.selectedTerrainSlopePosition!);
    }
  }

  private createDraggableCorner(index: number, terrainSlopeCorner: TerrainSlopeCorner, corner: Vector2, height: number, radius: number) {
    let slopeTerrainEditorComponent = this;
    return new class extends DraggableCorner {
      private readonly xGizmo;
      private readonly yGizmo;

      constructor() {
        super(MeshBuilder.CreateDisc("Slope Editor Corner", {radius: radius}), terrainSlopeCorner, index);
        this.disc.material = slopeTerrainEditorComponent.cornerDiscMaterial;
        this.disc.position.x = corner.x;
        this.disc.position.y = height;
        this.disc.position.z = corner.y;
        this.disc.rotation.x = Tools.ToRadians(90);

        this.xGizmo = new AxisDragGizmo(new Vector3(1, 0, 0), Color3.FromHexString("#FF0000"));
        this.xGizmo.attachedMesh = this.disc;
        this.xGizmo.updateGizmoRotationToMatchAttachedMesh = false;
        this.xGizmo.updateGizmoPositionToMatchAttachedMesh = true;

        this.yGizmo = new AxisDragGizmo(new Vector3(0, 0, 1), Color3.FromHexString("#00FF00"));
        this.yGizmo.attachedMesh = this.disc;
        this.yGizmo.updateGizmoRotationToMatchAttachedMesh = false;
        this.yGizmo.updateGizmoPositionToMatchAttachedMesh = true;

        this.disc.onAfterWorldMatrixUpdateObservable.add(() => {
          slopeTerrainEditorComponent.selectedTerrainSlopeMesh!.dispose();
          slopeTerrainEditorComponent.selectedTerrainSlopePolygon![index!] = new Vector2(this.disc.position.x, this.disc.position.z);
          slopeTerrainEditorComponent.selectedTerrainSlopeMesh = slopeTerrainEditorComponent.createPolygonMesh(slopeTerrainEditorComponent.selectedTerrainSlopePolygon!, slopeTerrainEditorComponent.selectedTerrainSlopePosition!);
          slopeTerrainEditorComponent.onSelectionEdited();
          slopeTerrainEditorComponent.updateHighlight();
          BabylonJsUtils.updateTerrainSlopeCornerFromVertex2Array(slopeTerrainEditorComponent.selectedTerrainSlopePolygon!, slopeTerrainEditorComponent.selectedTerrainSlopePosition!);
        });
      }

      dispose(): void {
        this.disc.dispose();
        this.xGizmo.dispose();
        this.yGizmo.dispose();
      }
    }
  }

  deleteCorner() {
    this.selectedTerrainSlopePolygon!.splice(this.draggableCorner!.index, 1);
    this.selectedTerrainSlopePosition!.polygon.splice(this.draggableCorner!.index, 1);
    this.clearDraggableCorner();

    this.selectedTerrainSlopeMesh!.dispose();
    this.selectedTerrainSlopeMesh = this.createPolygonMesh(this.selectedTerrainSlopePolygon!, this.selectedTerrainSlopePosition!);

    this.updateHighlight();
  }

  private findParentSlope(center: Vector2): number | null {
    for (const terrainSlopePosition of this.terrainSlopePositions) {
      if (this.isInside(center, terrainSlopePosition.polygon)) {
        let nearestChildSlope = this.findNearestChildSlope(center, terrainSlopePosition);
        return nearestChildSlope || terrainSlopePosition.id;
      }
    }
    return null;
  }

  private findNearestChildSlope(center: Vector2, slope: TerrainSlopePosition): number | null {
    if (slope.children) {
      for (const child of slope.children) {
        let slopeId = this.findNearestChildSlope(center, child);
        if (slopeId) {
          return slopeId;
        }
      }
      if (this.isInside(center, slope.polygon)) {
        return slope.id;
      } else {
        return null;
      }
    } else {
      return null;
    }
  }

  private isInside(point: Vector2, corners: TerrainSlopeCorner[]): boolean {
    let polygon: any[] = [];
    corners.forEach(terrainSlopeCorner => polygon.push([terrainSlopeCorner.position.x, terrainSlopeCorner.position.y]));
    return pointInPolygon([point.x, point.y], polygon);
  }

  save() {
    const url = `${UPDATE_SLOPES_TERRAIN_EDITOR}/${this.gwtAngularService.gwtAngularFacade.gameUiControl.getPlanetConfig().getId()}`;
    this.httpClient.put(url, this.slopeTerrainEditorUpdate).subscribe({
      next: () => {
        this.createSlopeTerrainEditorUpdate();
        this.messageService.add({
          severity: 'success',
          life: 300,
          summary: `Slope saved`
        });
      },
      error: error => {
        console.error(error);
        this.messageService.add({
          severity: 'Slope Save Error',
          summary: `Error calling: ${url}`,
          detail: error,
          sticky: true
        });
      }

    })
  }

  private disposeAllEditorMeshes() {
    let editorMeshes: Mesh[] = [];

    this.renderService.getScene().meshes.forEach(mesh => {
      let razarionMetadata = BabylonRenderServiceAccessImpl.getRazarionMetadata(mesh);
      if (razarionMetadata && razarionMetadata.editorHintSlopePosition) {
        editorMeshes.push(<Mesh>mesh);
      }
    });

    editorMeshes.forEach(mesh => mesh.dispose());
  }

  restartPlanetWarm() {
    this.editorService.executeServerCommand(EditorService.RESTART_PLANET_WARM);
  }

  restartPlanetCold() {
    this.editorService.executeServerCommand(EditorService.RESTART_PLANET_COLD);
  }
}

abstract class DraggableCorner {
  public readonly disc;

  protected constructor(disc: Mesh, public terrainSlopeCorner: TerrainSlopeCorner, readonly index: number) {
    this.disc = disc;
  }

  abstract dispose(): void;
}
