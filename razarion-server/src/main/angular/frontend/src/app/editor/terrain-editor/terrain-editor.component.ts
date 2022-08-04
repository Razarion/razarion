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
import {VertexHolder} from "../../common/components/angles-3-editor.component";

@Component({
  selector: 'app-terrain-editor',
  templateUrl: './terrain-editor.component.html'
})
export class TerrainEditorComponent extends EditorPanel implements OnInit, OnDestroy, ThreeJsRendererServiceMouseEventListener {
  terrainEditorService: TerrainEditorService;
  slopeMode: boolean = true;
  slopes: any[] = [];
  driveways: any[] = [];
  terrainObjects: any[] = [];
  selectedSlope: any;
  selectedDriveway: any;
  selectedTerrainObject: any;
  terrainObjectRotation = new VertexHolder(GwtInstance.newVertex(0, 0, 0));
  terrainObjectScale = new VertexHolder(GwtInstance.newVertex(1, 1, 1));
  terrainObjectOffset = new VertexHolder(GwtInstance.newVertex(0, 0, 0));
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
      let terrainObjectConfig = this.gwtAngularService.gwtAngularFacade.terrainTypeService.getTerrainObjectConfig(this.selectedTerrainObject.objectNameId.id);
      if (terrainObjectConfig.getThreeJsUuid() === undefined) {
        throw new Error(`TerrainObjectConfig has no threeJsUuid: ${terrainObjectConfig.toString()}`);
      }
      let threeJsModel = this.threeJsModelService.cloneObject3D(terrainObjectConfig.getThreeJsUuid());
      if (threeJsRendererServiceMouseEvent.pointOnObject3D) {
        threeJsModel.position.x = threeJsRendererServiceMouseEvent.pointOnObject3D.x;
        threeJsModel.position.y = threeJsRendererServiceMouseEvent.pointOnObject3D.y;
        threeJsModel.position.z = threeJsRendererServiceMouseEvent.pointOnObject3D.z;

        threeJsModel.rotation.x = this.terrainObjectRotation.vertex.getX();
        threeJsModel.rotation.y = this.terrainObjectRotation.vertex.getY();
        threeJsModel.rotation.z = this.terrainObjectRotation.vertex.getZ();
      }
      this.threeJsRendererServiceImpl.scene.add(threeJsModel);

      if (threeJsRendererServiceMouseEvent.pointOnObject3D) {
        let terrainObjectPosition: TerrainObjectPosition = GwtInstance.newTerrainObjectPosition();
        terrainObjectPosition.setTerrainObjectConfigId(this.selectedTerrainObject.objectNameId.id);
        terrainObjectPosition.setPosition(GwtInstance.newDecimalPosition(threeJsRendererServiceMouseEvent.pointOnObject3D.x, threeJsRendererServiceMouseEvent.pointOnObject3D.y));
        terrainObjectPosition.setScale(this.terrainObjectScale.vertex);
        terrainObjectPosition.setRotation(this.terrainObjectRotation.vertex);
        terrainObjectPosition.setOffset(this.terrainObjectOffset.vertex);
        this.createdTerrainObjects.push(terrainObjectPosition)
      }
    }
  }

}
