import {Component, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {EditorPanel} from "../editor-model";
import {TerrainEditorService, TerrainObjectPosition} from "../../gwtangular/GwtAngularFacade";
import {GwtAngularService} from "../../gwtangular/GwtAngularService";
import {MessageService} from "primeng/api";
import {
  ThreeJsRendererServiceImpl,
  ThreeJsRendererServiceMouseEvent,
  ThreeJsRendererServiceMouseEventListener
} from "../../game/renderer/three-js-renderer-service.impl";
import {ThreeJsModelService} from "../../game/renderer/three-js-model.service";
import {TerrainObjectPositionComponent} from "./terrain-object-position.component";
import {GwtInstance} from "../../gwtangular/GwtInstance";

@Component({
  selector: 'app-terrain-editor',
  templateUrl: './terrain-editor.component.html'
})
export class TerrainEditorComponent extends EditorPanel implements OnInit, OnDestroy, ThreeJsRendererServiceMouseEventListener {
  terrainEditorService: TerrainEditorService;
  terrainObjectConfigs: any[] = [];
  selectedTerrainObjectConfig: any;
  selectedTerrainObjectInfo: string = '';
  @ViewChild('terrainObjectPosition')
  terrainObjectPositionComponent!: TerrainObjectPositionComponent;
  terrainObjectPositions: Map<number, TerrainObjectPosition> = new Map<number, TerrainObjectPosition>()
  private createdTerrainObjects: TerrainObjectPosition[] = [];
  private updatedTerrainObjects: TerrainObjectPosition[] = [];

  constructor(private gwtAngularService: GwtAngularService,
              private messageService: MessageService,
              private threeJsModelService: ThreeJsModelService,
              private threeJsRendererServiceImpl: ThreeJsRendererServiceImpl) {
    super();
    this.terrainEditorService = gwtAngularService.gwtAngularFacade.editorFrontendProvider.getTerrainEditorService();
  }

  ngOnInit(): void {
    this.threeJsRendererServiceImpl.addMouseDownHandler(this);
    this.terrainEditorService.getAllTerrainObjects().then(terrainObjects => {
      this.terrainObjectConfigs = [];
      terrainObjects.forEach(terrainObject => {
        this.terrainObjectConfigs.push({name: terrainObject.toString(), objectNameId: terrainObject})
      });
      this.selectedTerrainObjectConfig = this.terrainObjectConfigs[0];
    });
    this.terrainEditorService.getTerrainObjectPositions().then(terrainObjectPositions => {
      this.terrainObjectPositions.clear();
      terrainObjectPositions.forEach(terrainObjectPosition => this.terrainObjectPositions.set(terrainObjectPosition.getId(), terrainObjectPosition));
      this.messageService.add({
        severity: 'success',
        summary: "Terrain objects loaded"
      })
    }).catch(error => {
      this.messageService.add({
        severity: 'error',
        summary: `Load terrain object failed`,
        detail: error,
        sticky: true
      });
    });
  }

  ngOnDestroy(): void {
    this.threeJsRendererServiceImpl.removeMouseDownHandler(this);
  }

  onSelectedSlopeChange(event: any) {
    this.terrainEditorService.setSlope4New(event.value.objectNameId);
  }

  onSelectedDrivewayChange(event: any) {
    this.terrainEditorService.setDriveway4New(event.value.objectNameId);
  }

  save() {
    this.terrainEditorService.save(this.createdTerrainObjects, this.updatedTerrainObjects)
      .then(okString => {
        this.createdTerrainObjects = [];
        this.messageService.add({
          severity: 'success',
          summary: okString
        })
      })
      .catch(error => {
        this.messageService.add({
          severity: 'error',
          summary: `Save terrain failed`,
          detail: `${JSON.stringify(error)}`,
          sticky: true
        });
      });
  }

  onThreeJsRendererServiceMouseEvent(threeJsRendererServiceMouseEvent: ThreeJsRendererServiceMouseEvent): void {
    if (threeJsRendererServiceMouseEvent.razarionTerrainObject3D && (<any>threeJsRendererServiceMouseEvent.razarionTerrainObject3D).razarionNewTerrainObjectPosition) {
      // New reselected
      let terrainObjectPosition: TerrainObjectPosition = (<any>threeJsRendererServiceMouseEvent.razarionTerrainObject3D).razarionNewTerrainObjectPosition;
      let terrainObjectConfig = this.gwtAngularService.gwtAngularFacade.terrainTypeService.getTerrainObjectConfig(terrainObjectPosition.getTerrainObjectConfigId());
      this.selectedTerrainObjectInfo = `Unsaved [${terrainObjectConfig.getInternalName()} (${terrainObjectConfig.getId()})]`
      this.terrainObjectPositionComponent.init(threeJsRendererServiceMouseEvent.razarionTerrainObject3D!, terrainObjectPosition);
    } else if (threeJsRendererServiceMouseEvent.razarionTerrainObject3D && threeJsRendererServiceMouseEvent.razarionTerrainObjectId) {
      // Existing selected
      let terrainObjectConfig = this.gwtAngularService.gwtAngularFacade.terrainTypeService.getTerrainObjectConfig(threeJsRendererServiceMouseEvent.razarionTerrainObjectConfigId!);
      this.selectedTerrainObjectInfo = `Id: ${threeJsRendererServiceMouseEvent.razarionTerrainObjectId} [${terrainObjectConfig.getInternalName()} (${terrainObjectConfig.getId()})]`
      let terrainObjectPosition = this.terrainObjectPositions.get(threeJsRendererServiceMouseEvent.razarionTerrainObjectId!)!;
      this.terrainObjectPositionComponent.init(threeJsRendererServiceMouseEvent.razarionTerrainObject3D, terrainObjectPosition);
      if (this.updatedTerrainObjects.findIndex(o => o.getId() === terrainObjectPosition.getId()) === -1) {
        this.updatedTerrainObjects.push(terrainObjectPosition);
      }
    } else {
      // Create new
      let terrainObjectConfig = this.gwtAngularService.gwtAngularFacade.terrainTypeService.getTerrainObjectConfig(this.selectedTerrainObjectConfig.objectNameId.id);
      this.selectedTerrainObjectInfo = `Created [${terrainObjectConfig.getInternalName()} (${terrainObjectConfig.getId()})]`
      if (terrainObjectConfig.getThreeJsModelPackConfigId() === undefined) {
        throw new Error(`TerrainObjectConfig has no threeJsModelPackConfigId: ${terrainObjectConfig.toString()}`);
      }
      if (threeJsRendererServiceMouseEvent.pointOnObject3D) {
        let newObject3D = this.threeJsModelService.cloneObject3D(terrainObjectConfig.getThreeJsModelPackConfigId());
        newObject3D.position.x = threeJsRendererServiceMouseEvent.pointOnObject3D.x;
        newObject3D.position.y = threeJsRendererServiceMouseEvent.pointOnObject3D.y;
        newObject3D.position.z = threeJsRendererServiceMouseEvent.pointOnObject3D.z;
        (<any>newObject3D).razarionTerrainObjectConfigId = this.selectedTerrainObjectConfig.objectNameId.id;
        this.threeJsRendererServiceImpl.scene.add(newObject3D);
        let terrainObjectPosition = GwtInstance.newTerrainObjectPosition();
        terrainObjectPosition.setTerrainObjectConfigId(this.selectedTerrainObjectConfig.objectNameId.id);
        terrainObjectPosition.setPosition(GwtInstance.newDecimalPosition(threeJsRendererServiceMouseEvent.pointOnObject3D.x, threeJsRendererServiceMouseEvent.pointOnObject3D.y));
        terrainObjectPosition.setRotation(GwtInstance.newVertex(0, 0, 0));
        terrainObjectPosition.setScale(GwtInstance.newVertex(1, 1, 1));
        terrainObjectPosition.setOffset(GwtInstance.newVertex(0, 0, 0));
        (<any>newObject3D).razarionNewTerrainObjectPosition = terrainObjectPosition;
        this.createdTerrainObjects.push(terrainObjectPosition);
        this.terrainObjectPositionComponent.init(newObject3D, terrainObjectPosition);
      }
    }
  }

}
