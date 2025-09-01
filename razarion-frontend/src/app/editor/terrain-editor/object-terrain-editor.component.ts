import {Component, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {ObjectNameId, TerrainObjectConfig, TerrainObjectModel} from "../../gwtangular/GwtAngularFacade";
import {GwtAngularService} from "../../gwtangular/GwtAngularService";
import {MessageService} from "primeng/api";
import {
  BabylonRenderServiceAccessImpl,
  RazarionMetadataType
} from "../../game/renderer/babylon-render-service-access-impl.service";
import {BabylonModelService} from "../../game/renderer/babylon-model.service";
import {TerrainObjectPositionComponent} from "./terrain-object-position.component";
import {GwtInstance} from "../../gwtangular/GwtInstance";
import {GizmoManager, Mesh, MeshBuilder, Node, PointerEventTypes, Tools, TransformNode} from "@babylonjs/core";
import {BabylonTerrainTileImpl} from "../../game/renderer/babylon-terrain-tile.impl";
import {Observer} from "@babylonjs/core/Misc/observable";
import {PointerInfo} from "@babylonjs/core/Events/pointerEvents";
import {Nullable} from "@babylonjs/core/types";
import {EditorService} from "../editor-service";
import {SimpleMaterial} from "@babylonjs/materials";
import {Color3} from "@babylonjs/core/Maths/math.color";
import {UPDATE_RADIUS_REST_CALL} from "../../common";
import {HttpClient} from "@angular/common/http";
import {EditorPanel} from '../editor-model';
import {TerrainObjectGeneratorComponent} from "./terrain-object-generator/terrain-object-generator.component";
import {
  TerrainEditorControllerClient,
  TerrainEditorUpdate,
  TerrainObjectEditorControllerClient,
  TerrainObjectPosition
} from "../../generated/razarion-share";
import {TypescriptGenerator} from "../../backend/typescript-generator";
import {GeneratedRestHelper} from "../../common/generated-rest-helper";
import {Button} from 'primeng/button';
import {Divider} from 'primeng/divider';
import {SelectButton} from 'primeng/selectbutton';
import {InputNumber} from 'primeng/inputnumber';
import {FormsModule} from '@angular/forms';
import {SelectModule} from 'primeng/select';
import {CommonModule} from '@angular/common';

@Component({
  selector: 'object-terrain-editor',
  imports: [
    Button,
    Divider,
    TerrainObjectGeneratorComponent,
    SelectButton,
    TerrainObjectPositionComponent,
    InputNumber,
    FormsModule,
    SelectModule,
    CommonModule
  ],
  templateUrl: './object-terrain-editor.component.html'
})
export class ObjectTerrainEditorComponent extends EditorPanel implements OnInit, OnDestroy {
  private readonly discRadiusMaterial: SimpleMaterial;
  terrainObjectConfigs: { objectNameId: ObjectNameId, name: string }[] = [];
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
  @ViewChild('terrainObjectGenerator')
  terrainObjectGenerator!: TerrainObjectGeneratorComponent;
  private mouseObservable: Nullable<Observer<PointerInfo>> = null;
  private newTerrainObjects: TerrainObjectPosition[] = [];
  private updatedTerrainObjects: TerrainObjectPosition[] = [];
  gizmoManager: GizmoManager;
  private selectionTransformNodeObservable: Nullable<Observer<TransformNode>> = null;
  private terrainEditorControllerClient: TerrainEditorControllerClient;
  private terrainObjectEditorControllerClient: TerrainObjectEditorControllerClient;

  constructor(private gwtAngularService: GwtAngularService,
              private messageService: MessageService,
              private babylonModelService: BabylonModelService,
              private babylonRenderServiceAccess: BabylonRenderServiceAccessImpl,
              private editorService: EditorService,
              private httpClient: HttpClient) {
    super();
    this.gizmoManager = new GizmoManager(babylonRenderServiceAccess.getScene());
    this.gizmoManager.positionGizmoEnabled = true;
    this.gizmoManager.rotationGizmoEnabled = false;
    this.gizmoManager.scaleGizmoEnabled = false;
    this.gizmoManager.boundingBoxGizmoEnabled = false;
    this.gizmoManager.usePointerToAttachGizmos = false;
    this.discRadiusMaterial = new SimpleMaterial(`Radius`, babylonRenderServiceAccess.getScene());
    this.discRadiusMaterial.diffuseColor = Color3.Yellow();
    this.discRadiusMaterial.backFaceCulling = false;

    this.terrainEditorControllerClient = new TerrainEditorControllerClient(TypescriptGenerator.generateHttpClientAdapter(httpClient));
    this.terrainObjectEditorControllerClient = new TerrainObjectEditorControllerClient(TypescriptGenerator.generateHttpClientAdapter(httpClient));
  }


  ngOnInit(): void {
    this.terrainObjectEditorControllerClient
      .getObjectNameIds()
      .then(objectNameIds => {
        this.terrainObjectConfigs = [];
        objectNameIds.forEach(objectNameId => {
          this.terrainObjectConfigs.push({
            name: `${objectNameId.internalName} '${objectNameId.id}'`,
            objectNameId: objectNameId
          })
        });
        this.newTerrainObjectConfig = this.terrainObjectConfigs[0];
        this.terrainObjectGenerator.init((terrainObjectModel: TerrainObjectModel, node: TransformNode) => {
          let terrainObjectPosition = GeneratedRestHelper.newTerrainObjectPosition();
          terrainObjectPosition.terrainObjectConfigId = terrainObjectModel.terrainObjectId;
          this.updateTerrainObjectPosition(node, terrainObjectPosition);
          this.newTerrainObjects.push(terrainObjectPosition);
        });
      })
  }

  ngOnDestroy(): void {
    this.deactivate();
  }

  public activate() {
    this.mouseObservable = this.babylonRenderServiceAccess.getScene().onPointerObservable.add((pointerInfo) => {
      if (!this.gwtAngularService.gwtAngularFacade.inputService) {
        return;
      }
      switch (pointerInfo.type) {
        case PointerEventTypes.POINTERDOWN: {
          this.selectedNode = null;
          let pickingInfo = this.babylonRenderServiceAccess.setupMeshPickPoint();
          if (pickingInfo.hit) {
            let node = BabylonRenderServiceAccessImpl.findRazarionMetadataNode(pickingInfo.pickedMesh!);
            if (!node) {
              return;
            }
            let razarionMetadata = BabylonRenderServiceAccessImpl.getRazarionMetadata(node)!;
            if (razarionMetadata.type == RazarionMetadataType.TERRAIN_OBJECT) {
              // Select existing
              this.selectActiveTerrainObject(<TransformNode>node, false)
            } else if (razarionMetadata.type == RazarionMetadataType.GROUND) {
              // Create new while click on ground
              if (!this.newTerrainObjectMode) {
                return;
              }
              let terrainObjectConfig = this.gwtAngularService.gwtAngularFacade.terrainTypeService.getTerrainObjectConfig(this.newTerrainObjectConfig.objectNameId.id);
              if (!terrainObjectConfig.getModel3DId()) {
                throw new Error(`TerrainObjectConfig has no model3DId: ${terrainObjectConfig.toString()}`);
              }

              let terrainObjectModel = new class implements TerrainObjectModel {
                position = GwtInstance.newVertex(pickingInfo.pickedPoint!.x, pickingInfo.pickedPoint!.z, pickingInfo.pickedPoint!.y);
                rotation = GwtInstance.newVertex(0, 0, 0);
                scale = GwtInstance.newVertex(1, 1, 1);
                terrainObjectId = -1;
              }
              let newTerrainObjectMesh = BabylonTerrainTileImpl.createTerrainObject(terrainObjectModel, terrainObjectConfig, this.babylonModelService, null);
              this.selectActiveTerrainObject(<TransformNode>newTerrainObjectMesh, true)
            }
          }
          break;
        }
      }
    });
  }

  public deactivate() {
    this.babylonRenderServiceAccess.getScene().onPointerObservable.remove(this.mouseObservable);
    this.clearSelection();
  }

  private clearGizmos() {
    this.gizmoManager.attachToNode(null);
  }

  private setupGizmo(node: Node) {
    this.clearGizmos();
    this.gizmoManager.attachToNode(node);
  }

  save() {
    let terrainEditorUpdate: TerrainEditorUpdate = {
      createdTerrainObjects: this.newTerrainObjects,
      updatedTerrainObjects: this.updatedTerrainObjects,
      deletedTerrainObjectsIds: []
    }
    this.terrainEditorControllerClient
      .updateTerrain(this.editorService.getPlanetId(), terrainEditorUpdate)
      .then(() => {
        this.newTerrainObjects = [];
        this.updatedTerrainObjects = [];
        this.clearSelection();
        this.messageService.add({
          severity: 'success',
          life: 300,
          summary: 'Terrain objects saved'
        })
      }).catch(reason => {
      console.error(reason);
      this.messageService.add({
        severity: 'error',
        summary: `Save terrain objects failed`,
        detail: reason.message || `${JSON.stringify(reason)}`,
        sticky: true
      });
    })
  }

  private selectActiveTerrainObject(node: TransformNode, isNew: boolean) {
    this.setupGizmo(node);

    this.selectedNode = node;
    let terrainObjectPosition: TerrainObjectPosition;

    if (isNew) {
      terrainObjectPosition = GeneratedRestHelper.newTerrainObjectPosition();
      terrainObjectPosition.terrainObjectConfigId = this.newTerrainObjectConfig.objectNameId.id;
      this.updateTerrainObjectPosition(node, terrainObjectPosition);
      let razarionMetadata = BabylonRenderServiceAccessImpl.getRazarionMetadata(node);
      razarionMetadata!.editorHintTerrainObjectPosition = terrainObjectPosition;
      this.newTerrainObjects.push(terrainObjectPosition);
      this.selectedTerrainObject = this.gwtAngularService.gwtAngularFacade.terrainTypeService.getTerrainObjectConfig(this.newTerrainObjectConfig.objectNameId.id);
      this.selectedRadius = this.selectedTerrainObject.getRadius();
      this.showHideRadius();
    } else {
      let razarionMetadata = BabylonRenderServiceAccessImpl.getRazarionMetadata(node);
      if (razarionMetadata!.id) {
        let to = this.findUpdatedTerrainObjectPosition(razarionMetadata!.id);
        if (!to) {
          terrainObjectPosition = GeneratedRestHelper.newTerrainObjectPosition();
          terrainObjectPosition.id = razarionMetadata?.id!;
          terrainObjectPosition.terrainObjectConfigId = razarionMetadata?.configId!;
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
      return o.id === id
    });
  }

  private updateTerrainObjectPosition(node: TransformNode, terrainObjectPosition: TerrainObjectPosition) {
    terrainObjectPosition.position = GeneratedRestHelper.newDecimalPosition(node.position.x, node.position.z);
    terrainObjectPosition.rotation = GeneratedRestHelper.newVertex(node.rotation.x, node.rotation.z, node.rotation.y);
    terrainObjectPosition.scale = GeneratedRestHelper.newVertex(node.scaling.x, node.scaling.z, node.scaling.y);
  }

  public clearSelection() {
    this.clearGizmos();
    if (this.selectionTransformNodeObservable && this.terrainObjectPositionComponent.getTransformNode()) {
      this.terrainObjectPositionComponent.getTransformNode()!.onAfterWorldMatrixUpdateObservable.remove(this.selectionTransformNodeObservable!);
      this.selectionTransformNodeObservable = null;
    }
    this.terrainObjectPositionComponent.clearSelection();
    this.showHideRadius();
    this.selectedRadiusShow = false;
    this.selectedTerrainObject = null;
    this.newTerrainObjectMode = false;
    this.selectedNode = null;
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

  onSelectedRadiusChange(value: any) {
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
      next: () => {
        this.messageService.add({
          severity: 'success',
          life: 300,
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
      }
    });

  }

}
