import { Component, OnInit } from '@angular/core';
import {
  DRIVEWAY_EDITOR_PATH,
  READ_TERRAIN_SLOPE_POSITIONS,
  SLOPE_EDITOR_PATH,
  UPDATE_SLOPES_TERRAIN_EDITOR
} from "../../common";
import { HttpClient } from "@angular/common/http";
import { GwtAngularService } from "../../gwtangular/GwtAngularService";
import { MessageService } from "primeng/api";
import pako from 'pako';
import {
  ObjectNameId,
  PlanetConfig,
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
  PickingInfo,
  PointerEventTypes,
  PointerInfo,
  PolygonMeshBuilder,
  Tools,
  Vector2,
  Vector3
} from "@babylonjs/core";
import { SimpleMaterial } from "@babylonjs/materials";

import { BabylonJsUtils } from "../../game/renderer/babylon-js.utils";
import { Color3 } from "@babylonjs/core/Maths/math.color";
import { pointInPolygon } from "geometric";
import { EditorService } from "../editor-service";
import { BabylonTerrainTileImpl } from 'src/app/game/renderer/babylon-terrain-tile.impl';
import { EditorTerrainTile } from './editor-terrain-tile';
import { GwtInstance } from 'src/app/gwtangular/GwtInstance';
import { random } from '@turf/turf';
import { TerrainEditorControllerClient } from 'src/app/generated/razarion-share';
import { TypescriptGenerator } from 'src/app/backend/typescript-generator';

export enum UpDownMode {
  UP = 1,
  DOWN = 2,
  OFF = 0
}

@Component({
  selector: 'shape-terrain-editor',
  templateUrl: './shape-terrain-editor.component.html'
})
export class ShapeTerrainEditorComponent implements OnInit {
  cursorHeight: number = 1;
  cursorDiameter: number = 10;
  cursorFalloff: number = 0;
  cursorRandom: number = 0;
  upDownOptions: any = [
    { value: UpDownMode.UP, label: " Up " },
    { value: UpDownMode.DOWN, label: "Down" },
    { value: UpDownMode.OFF, label: "Off" }];
  upDownValue: any = UpDownMode.UP;
  wireframe: boolean = false;
  private pointerObservable: Nullable<Observer<PointerInfo>> = null;
  private planetConfig: PlanetConfig;
  private editorTerrainTiles: EditorTerrainTile[][] = [];
  private xCount: number;
  private yCount: number;
  private terrainEditorControllerClient: TerrainEditorControllerClient;

  constructor(private httpClient: HttpClient,
    public gwtAngularService: GwtAngularService,
    private messageService: MessageService,
    private renderService: BabylonRenderServiceAccessImpl,
    private editorService: EditorService) {
    this.terrainEditorControllerClient = new TerrainEditorControllerClient(TypescriptGenerator.generateHttpClientAdapter(httpClient));

    this.planetConfig = gwtAngularService.gwtAngularFacade.gameUiControl.getPlanetConfig();
    this.xCount = Math.ceil(this.planetConfig.getSize().getX() / BabylonTerrainTileImpl.NODE_X_COUNT);
    this.yCount = Math.ceil(this.planetConfig.getSize().getY() / BabylonTerrainTileImpl.NODE_Y_COUNT);
    for (let y = 0; y < this.yCount; y++) {
      this.editorTerrainTiles[y] = [];
      for (let x = 0; x < this.xCount; x++) {
        this.editorTerrainTiles[y][x] = new EditorTerrainTile(GwtInstance.newIndex(x, y));
      }
    }
  }

  ngOnInit(): void {
  }

  activate() {
    this.registerInputEvents();
    this.loadEditorTerrainTiles();
  }

  onWireframeChanged(): void {
    for (let x = 0; x < this.xCount; x++) {
      for (let y = 0; y < this.yCount; y++) {
        this.editorTerrainTiles[y][x].setWireframe(this.wireframe);
      }
    }

  }

  private registerInputEvents() {
    this.pointerObservable = this.renderService.getScene().onPointerObservable.add((pointerInfo) => {
      switch (pointerInfo.type) {
        case PointerEventTypes.POINTERDOWN: {
          let pickingInfo = this.renderService.setupTerrainPickPoint();
          if (pickingInfo && pickingInfo.pickedPoint) {
            this.onPointerDown(pickingInfo.pickedPoint);
          }
          break;
        }
        case PointerEventTypes.POINTERUP: {
          let pickingInfo = this.renderService.setupTerrainPickPoint();
          if (pickingInfo && pickingInfo.pickedPoint) {

          }
          break;
        }
        case PointerEventTypes.POINTERMOVE: {
          let pickingInfo = this.renderService.setupTerrainPickPoint();
          if (pickingInfo && pickingInfo.pickedPoint) {
            this.onPointerMove(pickingInfo.pickedPoint, (pointerInfo.event.buttons & 0x01) == 0x01);
          }
          break;
        }
      }
    });

  }

  private loadEditorTerrainTiles() {
    this.gwtAngularService.gwtAngularFacade.editorFrontendProvider.getTerrainEditorService().getDisplayTerrainTiles().forEach(babylonTerrainTile => {
      let index = (<BabylonTerrainTileImpl>babylonTerrainTile).terrainTile.getIndex();
      (<BabylonTerrainTileImpl>babylonTerrainTile).getGroundMesh().material!.wireframe = this.wireframe;
      this.editorTerrainTiles[index.getY()][index.getX()].setBabylonTerrainTile(<BabylonTerrainTileImpl>babylonTerrainTile);
    });
  }

  private onPointerDown(position: Vector3) {
    for (let x = 0; x < this.xCount; x++) {
      for (let y = 0; y < this.yCount; y++) {
        if (this.editorTerrainTiles[y][x].isInside(position)) {
          this.editorTerrainTiles[y][x].onPointerDown(position,
            this.cursorDiameter / 2.0,
            this.cursorFalloff,
            this.cursorHeight,
            this.cursorRandom,
            this.upDownValue);
        }
      }
    }
  }

  private onPointerMove(position: Vector3, buttonDown: boolean) {
    for (let x = 0; x < this.xCount; x++) {
      for (let y = 0; y < this.yCount; y++) {
        if (this.editorTerrainTiles[y][x].isInside(position)) {
          this.editorTerrainTiles[y][x].onPointerMove(position,
            buttonDown,
            this.cursorDiameter / 2.0,
            this.cursorFalloff,
            this.cursorHeight,
            this.cursorRandom,
            this.upDownValue);
        }
      }
    }
  }

  deactivate() {
    if (this.pointerObservable) {
      this.renderService.getScene().onPointerObservable.remove(this.pointerObservable);
      this.pointerObservable = null;
    }
  }

  async save() {
    const uint16Array = new Uint16Array(this.xCount * BabylonTerrainTileImpl.NODE_X_COUNT * this.yCount * BabylonTerrainTileImpl.NODE_Y_COUNT);

    let index = 0;
    for (let y = 0; y < this.yCount; y++) {
      for (let x = 0; x < this.xCount; x++) {
        let count = 0;
        let indexBefor = index;
        this.editorTerrainTiles[y][x].fillHeights(height => {
          uint16Array[index++] = height;
          if (height != 0) {
            count++;
          }
        });
        if (count > 0) {
          console.log(`Tile ${x} ${y} has ${count} index ${indexBefor}`);
        }
      }
    }


    // for (let i = 0; i < typedArray.length; i++) {
    //   // typedArray[i] = Math.random() * 65535;
    //   typedArray[i] = 65535;
    // }



    // for (let x = 0; x < nodeXCount; x++) {
    //   for (let y = 0; y < nodeYCount; y++) {
    //     let index = x * nodeXCount + y;
    //     typedArray[index] = index;
    //   }
    // } 

    console.log(uint16Array); // Gibt das komprimierte Array aus
    let compressed = pako.gzip(new Uint8Array(uint16Array.buffer));
    console.log(compressed); // Gibt das komprimierte Array aus
    // Erstellen Sie einen Blob aus dem typisierten Array
    const blob = new Blob([compressed.buffer], { type: 'application/octet-stream' });

    this.terrainEditorControllerClient.saveTerrainShape(-1, blob)
      .then(() => { this.messageService.add({ severity: 'success', summary: 'Success', detail: 'Terrain saved' }) })
      .catch(error => { this.messageService.add({ severity: 'error', summary: 'Error', detail: error.message }) });
  }

  restartPlanetWarm() {
    this.editorService.executeServerCommand(EditorService.RESTART_PLANET_WARM);
  }

  restartPlanetCold() {
    this.editorService.executeServerCommand(EditorService.RESTART_PLANET_COLD);
  }
}


