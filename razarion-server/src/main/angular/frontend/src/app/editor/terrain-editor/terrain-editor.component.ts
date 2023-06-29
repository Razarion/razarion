import {Component, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {EditorPanel} from "../editor-model";
import {
  TerrainEditorService,
  TerrainObjectConfig,
  TerrainObjectModel,
  TerrainObjectPosition
} from "../../gwtangular/GwtAngularFacade";
import {GwtAngularService} from "../../gwtangular/GwtAngularService";
import {MessageService} from "primeng/api";
import {RazarionMetadataType, ThreeJsRendererServiceImpl} from "../../game/renderer/three-js-renderer-service.impl";
import {BabylonModelService} from "../../game/renderer/babylon-model.service";
import {TerrainObjectPositionComponent} from "./terrain-object-position.component";
import {GwtInstance} from "../../gwtangular/GwtInstance";
import {GizmoManager, Mesh, MeshBuilder, Node, PointerEventTypes, Tools, TransformNode} from "@babylonjs/core";
import {ThreeJsTerrainTileImpl} from "../../game/renderer/three-js-terrain-tile.impl";
import {Observer} from "@babylonjs/core/Misc/observable";
import {PointerInfo} from "@babylonjs/core/Events/pointerEvents";
import {Nullable} from "@babylonjs/core/types";
import {EditorService} from "../editor-service";
import {SimpleMaterial} from "@babylonjs/materials";
import {Color3} from "@babylonjs/core/Maths/math.color";
import {UPDATE_RADIUS_REST_CALL} from "../../common";
import {HttpClient} from "@angular/common/http";

@Component({
  selector: 'app-terrain-editor',
  templateUrl: './terrain-editor.component.html'
})
export class TerrainEditorComponent extends EditorPanel implements OnInit, OnDestroy {
  private readonly discRadiusMaterial: SimpleMaterial;
  terrainEditorService: TerrainEditorService;
  terrainObjectConfigs: any[] = [];
  newTerrainObjectConfig: any;
  onOffOptions: any[] = [{label: 'Off', value: false}, {label: 'On', value: true}];
  newTerrainObjectMode: boolean = false;
  selectedNode: TransformNode | null = null;
  selectedTerrainObject: TerrainObjectConfig | null = null;
  selectedRadius: number = 0;
  selectedRadiusShow: boolean = false;
  selectedRadiusDisc: Mesh | null = null;
  @ViewChild('terrainObjectPosition')
  terrainObjectPositionComponent!: TerrainObjectPositionComponent;
  private mouseObservable: Nullable<Observer<PointerInfo>> = null;
  private newTerrainObjects: TerrainObjectPosition[] = [];
  private updatedTerrainObjects: TerrainObjectPosition[] = [];
  gizmoManager: GizmoManager;
  private selectionTransformNodeObservable: Nullable<Observer<TransformNode>> = null;

  constructor(private gwtAngularService: GwtAngularService,
              private messageService: MessageService,
              private babylonModelService: BabylonModelService,
              private threeJsRendererServiceImpl: ThreeJsRendererServiceImpl,
              private editorService: EditorService,
              private httpClient: HttpClient) {
    super();
    this.terrainEditorService = gwtAngularService.gwtAngularFacade.editorFrontendProvider.getTerrainEditorService();
    this.gizmoManager = new GizmoManager(threeJsRendererServiceImpl.getScene());
    this.gizmoManager.positionGizmoEnabled = true;
    this.gizmoManager.rotationGizmoEnabled = false;
    this.gizmoManager.scaleGizmoEnabled = false;
    this.gizmoManager.boundingBoxGizmoEnabled = false;
    this.discRadiusMaterial = new SimpleMaterial(`Radius`, threeJsRendererServiceImpl.getScene());
    this.discRadiusMaterial.diffuseColor = Color3.Yellow();
    this.discRadiusMaterial.backFaceCulling = false;
  }

  ngOnInit(): void {
    this.mouseObservable = this.threeJsRendererServiceImpl.getScene().onPointerObservable.add((pointerInfo) => {
      if (!this.gwtAngularService.gwtAngularFacade.inputService) {
        return;
      }
      switch (pointerInfo.type) {
        case PointerEventTypes.POINTERDOWN: {
          this.selectedNode = null;
          let pickingInfo = this.threeJsRendererServiceImpl.setupMeshPickPoint();
          if (pickingInfo.hit) {
            let node = ThreeJsRendererServiceImpl.findRazarionMetadataNode(pickingInfo.pickedMesh!);
            if (!node) {
              return;
            }
            let razarionMetadata = ThreeJsRendererServiceImpl.getRazarionMetadata(node)!;
            if (razarionMetadata.type == RazarionMetadataType.TERRAIN_OBJECT) {
              // Select existing
              this.selectActiveTerrainObject(<TransformNode>node, false)
            } else if (razarionMetadata.type == RazarionMetadataType.GROUND) {
              // Create new while click on ground
              if (!this.newTerrainObjectMode) {
                return;
              }
              let terrainObjectConfig = this.gwtAngularService.gwtAngularFacade.terrainTypeService.getTerrainObjectConfig(this.newTerrainObjectConfig.objectNameId.id);
              if (!terrainObjectConfig.getThreeJsModelPackConfigId()) {
                throw new Error(`TerrainObjectConfig has no threeJsModelPackConfigId: ${terrainObjectConfig.toString()}`);
              }

              let terrainObjectModel = new class implements TerrainObjectModel {
                position = GwtInstance.newVertex(pickingInfo.pickedPoint!.x, pickingInfo.pickedPoint!.z, pickingInfo.pickedPoint!.y);
                rotation = GwtInstance.newVertex(0, 0, 0);
                scale = GwtInstance.newVertex(1, 1, 1);
                terrainObjectId = -1;
              }
              let newTerrainObjectMesh = ThreeJsTerrainTileImpl.createTerrainObject(terrainObjectModel, terrainObjectConfig, this.babylonModelService, null);
              this.threeJsRendererServiceImpl.addShadowCaster(newTerrainObjectMesh);
              this.selectActiveTerrainObject(<TransformNode>newTerrainObjectMesh, true)
            }
          }
          break;
        }
      }
    });

    this.terrainEditorService.getAllTerrainObjects().then(terrainObjects => {
      this.terrainObjectConfigs = [];
      terrainObjects.forEach(terrainObject => {
        this.terrainObjectConfigs.push({name: terrainObject.toString(), objectNameId: terrainObject})
      });
      this.newTerrainObjectConfig = this.terrainObjectConfigs[0];
    });
  }

  private clearGizmos() {
    this.gizmoManager.attachToNode(null);
  }

  private setupGizmo(node: Node) {
    this.clearGizmos();
    this.gizmoManager.attachToNode(node);
  }

  ngOnDestroy(): void {
    this.threeJsRendererServiceImpl.getScene().onPointerObservable.remove(this.mouseObservable);
  }

  onSelectedSlopeChange(event: any) {
    this.terrainEditorService.setSlope4New(event.value.objectNameId);
  }

  onSelectedDrivewayChange(event: any) {
    this.terrainEditorService.setDriveway4New(event.value.objectNameId);
  }

  save() {
    this.terrainEditorService.save(this.newTerrainObjects, this.updatedTerrainObjects)
      .then(okString => {
        this.newTerrainObjects = [];
        this.updatedTerrainObjects = [];
        this.clearSelection();
        this.messageService.add({
          severity: 'success',
          summary: okString
        })
      })
      .catch(error => {
        console.error(error);
        this.messageService.add({
          severity: 'error',
          summary: `Save terrain failed`,
          detail: error.message || `${JSON.stringify(error)}`,
          sticky: true
        });
      });
  }

  private selectActiveTerrainObject(node: TransformNode, isNew: boolean) {
    this.setupGizmo(node);

    this.selectedNode = node;
    let terrainObjectPosition: TerrainObjectPosition;

    if (isNew) {
      terrainObjectPosition = GwtInstance.newTerrainObjectPosition();
      terrainObjectPosition.setTerrainObjectConfigId(this.newTerrainObjectConfig.objectNameId.id);
      this.updateTerrainObjectPosition(node, terrainObjectPosition);
      let razarionMetadata = ThreeJsRendererServiceImpl.getRazarionMetadata(node);
      razarionMetadata!.editorHintTerrainObjectPosition = terrainObjectPosition;
      this.newTerrainObjects.push(terrainObjectPosition);
      this.selectedTerrainObject = this.gwtAngularService.gwtAngularFacade.terrainTypeService.getTerrainObjectConfig(this.newTerrainObjectConfig.objectNameId.id);
      this.selectedRadius = this.selectedTerrainObject.getRadius();
      this.showHideRadius();
    } else {
      let razarionMetadata = ThreeJsRendererServiceImpl.getRazarionMetadata(node);
      if (razarionMetadata!.id) {
        let to = this.findUpdatedTerrainObjectPosition(razarionMetadata!.id);
        if (!to) {
          terrainObjectPosition = GwtInstance.newTerrainObjectPosition();
          terrainObjectPosition.setId(razarionMetadata?.id!);
          terrainObjectPosition.setTerrainObjectConfigId(razarionMetadata?.configId!);
          this.updatedTerrainObjects.push(terrainObjectPosition);
        } else {
          terrainObjectPosition = to;
        }
      } else {
        terrainObjectPosition = razarionMetadata!.editorHintTerrainObjectPosition!;
      }
      this.updateTerrainObjectPosition(node, terrainObjectPosition);
      this.selectedTerrainObject = this.gwtAngularService.gwtAngularFacade.terrainTypeService.getTerrainObjectConfig(razarionMetadata!.configId!);
      this.selectedRadius = this.selectedTerrainObject.getRadius();
      this.showHideRadius();
    }

    if (this.terrainObjectPositionComponent.getTransformNode() !== node) {
      if (this.selectionTransformNodeObservable) {
        this.terrainObjectPositionComponent.getTransformNode()!.onAfterWorldMatrixUpdateObservable.remove(this.selectionTransformNodeObservable!);
      }
      this.selectionTransformNodeObservable = node.onAfterWorldMatrixUpdateObservable.add(() => {
        this.updateTerrainObjectPosition(node, terrainObjectPosition);
      });
    }

    this.terrainObjectPositionComponent.setSelected(node, terrainObjectPosition);
  }

  private findUpdatedTerrainObjectPosition(id: number): TerrainObjectPosition | undefined {
    return this.updatedTerrainObjects.find((o) => {
      return o.getId() === id
    });
  }

  private updateTerrainObjectPosition(node: TransformNode, terrainObjectPosition: TerrainObjectPosition) {
    terrainObjectPosition.setPosition(GwtInstance.newDecimalPosition(node.position.x, node.position.z))
    terrainObjectPosition.setRotation(GwtInstance.newVertex(node.rotation.x, node.rotation.z, node.rotation.z))
    terrainObjectPosition.setScale(GwtInstance.newVertex(node.scaling.x, node.scaling.z, node.scaling.z))
  }

  public clearSelection() {
    this.clearGizmos();
    if (this.selectionTransformNodeObservable && this.terrainObjectPositionComponent.getTransformNode()) {
      this.terrainObjectPositionComponent.getTransformNode()!.onAfterWorldMatrixUpdateObservable.remove(this.selectionTransformNodeObservable!);
      this.selectionTransformNodeObservable = null;
    }
    this.terrainObjectPositionComponent.clearSelection();
    this.showHideRadius();
  }

  restartPlanetWarm() {
    this.editorService.executeServerCommand(EditorService.RESTART_PLANET_WARM);
  }

  restartPlanetCold() {
    this.editorService.executeServerCommand(EditorService.RESTART_PLANET_COLD);
  }

  showHideRadius(): void {
    if (this.selectedRadiusDisc) {
      this.selectedRadiusDisc.dispose();
    }
    if (this.selectedRadiusShow && this.selectedTerrainObject) {
      this.selectedRadiusDisc = MeshBuilder.CreateDisc("Radius Disc", {radius: 1});
      this.selectedRadiusDisc.material = this.discRadiusMaterial;
      this.selectedRadiusDisc.position.y = 0.01;
      this.selectedRadiusDisc.rotation.x = Tools.ToRadians(90);
      this.selectedRadiusDisc.scaling.x = this.selectedRadius;
      this.selectedRadiusDisc.scaling.y = this.selectedRadius;
      this.selectedRadiusDisc.parent = this.selectedNode;
    }
  }

  onSelectedRadiusChange(value: number) {
    if (this.selectedRadiusDisc) {
      this.selectedRadiusDisc.scaling.x = value;
      this.selectedRadiusDisc.scaling.y = value;
    }
  }

  saveRadius() {
    if (!this.selectedTerrainObject) {
      this.messageService.add({
        severity: 'error',
        summary: `Nothing Selected`,
        sticky: true
      });
      return;
    }
    const url = `${UPDATE_RADIUS_REST_CALL}/${this.selectedTerrainObject.getId()}/${this.selectedRadius}`
    this.httpClient.post(url, null).subscribe({
      next: value => {
        this.messageService.add({
          severity: 'success',
          summary: `Radius updated ${this.selectedTerrainObject?.getInternalName()}`
        });
      },
    error: error => {
      this.messageService.add({
        severity: 'error',
        summary: `Error calling: ${url}`,
        detail: error,
        sticky: true
      });
    }});

  }
}
