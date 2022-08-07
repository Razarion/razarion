import {Component, OnDestroy, OnInit} from '@angular/core';
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
import {GwtInstance} from "../../gwtangular/GwtInstance";
import {Euler} from "three/src/math/Euler";
import {Vector3} from "three/src/math/Vector3";

@Component({
  selector: 'app-terrain-editor',
  templateUrl: './terrain-editor.component.html'
})
export class TerrainEditorComponent extends EditorPanel implements OnInit, OnDestroy, ThreeJsRendererServiceMouseEventListener {
  terrainEditorService: TerrainEditorService;
  slopeMode: boolean = false;
  slopes: any[] = [];
  driveways: any[] = [];
  terrainObjects: any[] = [];
  selectedSlope: any;
  selectedDriveway: any;
  selectedTerrainObject: any;
  terrainObjectPosition = new Vector3(0, 0, 0);
  terrainObjectRotation = new Euler(0, 0, 0);
  terrainObjectScale = new Vector3(1, 1, 1);
  terrainObjectOffset = new Vector3(0, 0, 0);
  selectedTerrainObjectInfo: string = '';
  private createdTerrainObjects: TerrainObjectPosition[] = [];

  constructor(private gwtAngularService: GwtAngularService,
              private messageService: MessageService,
              private threeJsModelService: ThreeJsModelService,
              private threeJsRendererServiceImpl: ThreeJsRendererServiceImpl) {
    super();
    this.terrainEditorService = gwtAngularService.gwtAngularFacade.editorFrontendProvider.getTerrainEditorService();
  }

  ngOnInit(): void {
    this.threeJsRendererServiceImpl.addMouseDownHandler(this);
    this.terrainEditorService.getAllSlopes().then(slopes => {
      this.slopes = [];
      slopes.forEach(slope => {
        this.slopes.push({name: slope.toString(), objectNameId: slope})
      });
      this.terrainEditorService.setSlope4New(this.slopes[0].objectNameId);
      this.selectedSlope = this.slopes[0];
    })
    this.terrainEditorService.getAllDriveways().then(driveways => {
      this.driveways = [];
      driveways.forEach(driveway => {
        this.driveways.push({name: driveway.toString(), objectNameId: driveway})
      });
      this.terrainEditorService.setDriveway4New(this.driveways[0].objectNameId);
      this.selectedDriveway = this.driveways[0];
    });
    this.terrainEditorService.getAllTerrainObjects().then(terrainObjects => {
      this.terrainObjects = [];
      terrainObjects.forEach(terrainObject => {
        this.terrainObjects.push({name: terrainObject.toString(), objectNameId: terrainObject})
      });
      this.selectedTerrainObject = this.terrainObjects[0];
    });
  }

  ngOnDestroy(): void {
    this.threeJsRendererServiceImpl.removeMouseDownHandler(this);
  }

  onTabSelected(event: any) {
    this.slopeMode = event.index === 0;
  }

  onSelectedSlopeChange(event: any) {
    this.terrainEditorService.setSlope4New(event.value.objectNameId);
  }

  onSelectedDrivewayChange(event: any) {
    this.terrainEditorService.setDriveway4New(event.value.objectNameId);
  }

  save() {
    this.terrainEditorService.save(this.createdTerrainObjects)
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
    if (!this.slopeMode) {
      if (threeJsRendererServiceMouseEvent.razarionTerrainObject3D && threeJsRendererServiceMouseEvent.razarionTerrainObjectConfigId) {
        this.selectedTerrainObject = threeJsRendererServiceMouseEvent.razarionTerrainObject3D;
        let terrainObjectConfig = this.gwtAngularService.gwtAngularFacade.terrainTypeService.getTerrainObjectConfig(threeJsRendererServiceMouseEvent.razarionTerrainObjectConfigId);
        this.selectedTerrainObjectInfo = `Id: ${threeJsRendererServiceMouseEvent.razarionTerrainObjectId} Type: ${terrainObjectConfig.toString()} (${terrainObjectConfig.getId()})`
        this.terrainObjectPosition = threeJsRendererServiceMouseEvent.razarionTerrainObject3D.position;
        this.terrainObjectRotation = threeJsRendererServiceMouseEvent.razarionTerrainObject3D.rotation;
        this.terrainObjectScale = threeJsRendererServiceMouseEvent.razarionTerrainObject3D.scale;
        // TODO this.terrainObjectOffset = threeJsRendererServiceMouseEvent.razarionTerrainObject3D.???;
      } else {
        this.selectedTerrainObjectInfo = "New Terrain Object"
        let terrainObjectConfig = this.gwtAngularService.gwtAngularFacade.terrainTypeService.getTerrainObjectConfig(this.selectedTerrainObject.objectNameId.id);
        if (terrainObjectConfig.getThreeJsUuid() === undefined) {
          throw new Error(`TerrainObjectConfig has no threeJsUuid: ${terrainObjectConfig.toString()}`);
        }
        let threeJsModel = this.threeJsModelService.cloneObject3D(terrainObjectConfig.getThreeJsUuid());
        if (threeJsRendererServiceMouseEvent.pointOnObject3D) {
          // TODO threeJsModel.position.x = threeJsRendererServiceMouseEvent.pointOnObject3D.x + this.terrainObjectOffset.vertex.getX();
          // TODO threeJsModel.position.y = threeJsRendererServiceMouseEvent.pointOnObject3D.y + this.terrainObjectOffset.vertex.getY();
          // TODO threeJsModel.position.z = threeJsRendererServiceMouseEvent.pointOnObject3D.z + this.terrainObjectOffset.vertex.getZ();

          // TODO threeJsModel.rotation.x = this.terrainObjectRotation.vertex.getX();
          // TODO threeJsModel.rotation.y = this.terrainObjectRotation.vertex.getY();
          // TODO threeJsModel.rotation.z = this.terrainObjectRotation.vertex.getZ();

          // TODO threeJsModel.scale.x = this.terrainObjectScale.vertex.getX();
          // TODO threeJsModel.scale.y = this.terrainObjectScale.vertex.getY();
          // TODO threeJsModel.scale.z = this.terrainObjectScale.vertex.getZ();
        }
        this.threeJsRendererServiceImpl.scene.add(threeJsModel);

        if (threeJsRendererServiceMouseEvent.pointOnObject3D) {
          let terrainObjectPosition: TerrainObjectPosition = GwtInstance.newTerrainObjectPosition();
          terrainObjectPosition.setTerrainObjectConfigId(this.selectedTerrainObject.objectNameId.id);
          terrainObjectPosition.setPosition(GwtInstance.newDecimalPosition(threeJsRendererServiceMouseEvent.pointOnObject3D.x, threeJsRendererServiceMouseEvent.pointOnObject3D.y));
          // TODO terrainObjectPosition.setScale(this.terrainObjectScale.vertex);
          // TODO  terrainObjectPosition.setRotation(this.terrainObjectRotation.vertex);
          // TODO terrainObjectPosition.setOffset(this.terrainObjectOffset.vertex);
          this.createdTerrainObjects.push(terrainObjectPosition)
        }
      }
    }
  }

}
