import {
  AfterViewInit,
  Component,
  ComponentFactoryResolver,
  OnDestroy,
  OnInit,
  Type,
  ViewChild,
  ViewContainerRef
} from '@angular/core';
import { HttpClient } from "@angular/common/http";
import { GwtAngularService } from "../../gwtangular/GwtAngularService";
import { MessageService } from "primeng/api";
import pako from 'pako';
import { HttpClient as HttpClientAdapter, RestResponse } from '../../generated/razarion-share';
import { PlanetConfig } from "../../gwtangular/GwtAngularFacade";
import { BabylonRenderServiceAccessImpl } from "../../game/renderer/babylon-render-service-access-impl.service";
import { Nullable, Observer, PointerEventTypes, PointerInfo, Vector3 } from "@babylonjs/core";
import { EditorService } from "../editor-service";
import { BabylonTerrainTileImpl } from 'src/app/game/renderer/babylon-terrain-tile.impl';
import { EditorTerrainTile } from './editor-terrain-tile';
import { GwtInstance } from 'src/app/gwtangular/GwtInstance';
import { TerrainEditorControllerClient, TerrainHeightMapControllerClient } from 'src/app/generated/razarion-share';
import { TypescriptGenerator } from 'src/app/backend/typescript-generator';
import { AbstractBrush } from "./brushes/abstract-brush";
import { FixHeightBrushComponent } from './brushes/fix-height-brush.component';
import { FlattenBrushComponent } from "./brushes/flattem-brush.component";

export enum UpDownMode {
  UP = 1,
  DOWN = 2,
  OFF = 0
}

@Component({
  selector: 'shape-terrain-editor',
  templateUrl: './shape-terrain-editor.component.html'
})
export class ShapeTerrainEditorComponent implements AfterViewInit, OnDestroy {
  wireframe: boolean = false;
  showTerrainType: boolean = false;
  showTerrainTypeWorker: boolean = false;
  private pointerObservable: Nullable<Observer<PointerInfo>> = null;
  private planetConfig: PlanetConfig;
  private editorTerrainTiles: EditorTerrainTile[][] = [];
  private xCount: number;
  private yCount: number;
  private terrainEditorControllerClient: TerrainEditorControllerClient;
  lastSavedTimeStamp: string = "";
  lastSavedSize: string = "";
  private originalUint16HeightMap?: Uint16Array;

  @ViewChild(' brushContainer', { read: ViewContainerRef })
  brushContainer?: ViewContainerRef;
  brushOptions = [
    { label: 'Fix height', value: FixHeightBrushComponent },
    { label: 'Flatten', value: FlattenBrushComponent }
  ];
  selectedBrush?: string;

  private currentBrush?: AbstractBrush

  constructor(httpClient: HttpClient,
    public gwtAngularService: GwtAngularService,
    private messageService: MessageService,
    private renderService: BabylonRenderServiceAccessImpl,
    private editorService: EditorService,
    private resolver: ComponentFactoryResolver) {
    this.terrainEditorControllerClient = new TerrainEditorControllerClient(TypescriptGenerator.generateHttpClientAdapter(httpClient));

    this.planetConfig = gwtAngularService.gwtAngularFacade.gameUiControl.getPlanetConfig();
    this.xCount = Math.ceil(this.planetConfig.getSize().getX() / BabylonTerrainTileImpl.NODE_X_COUNT);
    this.yCount = Math.ceil(this.planetConfig.getSize().getY() / BabylonTerrainTileImpl.NODE_Y_COUNT);
    for (let y = 0; y < this.yCount; y++) {
      this.editorTerrainTiles[y] = [];
      for (let x = 0; x < this.xCount; x++) {
        this.editorTerrainTiles[y][x] = new EditorTerrainTile(renderService, gwtAngularService.gwtAngularFacade.inputService, GwtInstance.newIndex(x, y));
      }
    }

    let terrainHeightMapControllerClient = new TerrainHeightMapControllerClient(new class implements HttpClientAdapter {
      request<R>(requestConfig: {
        method: string;
        url: string;
        queryParams?: any;
        data?: any;
        copyFn?: ((data: R) => R) | undefined;
      }): RestResponse<R> {
        return <RestResponse<R>>fetch(requestConfig.url, {
          headers: {
            'Content-Type': 'application/octet-stream'
          }
        })
      }
    });

    terrainHeightMapControllerClient.getCompressedHeightMap(this.planetConfig.getId())
      .then(response => response.arrayBuffer())
      .then(buffer => this.originalUint16HeightMap = new Uint16Array(buffer))
      .catch(error => this.messageService.add({ severity: 'error', summary: 'Error', detail: error.message }));

    this.renderService.setEditorTerrainTileCreationCallback((babylonTerrainTile: BabylonTerrainTileImpl) => {
      this.setupEditorTerrainTile(babylonTerrainTile);
      return undefined;
    });
  }

  ngAfterViewInit(): void {
    Promise.resolve().then(() => {
      this.setupBrushComponent(FixHeightBrushComponent);
    });
  }

  ngOnDestroy(): void {
    this.renderService.setEditorTerrainTileCreationCallback(undefined);
  }

  activate() {
    this.registerInputEvents();
    this.loadEditorTerrainTiles();
  }

  onBrushChange(event: any) {
    this.setupBrushComponent(event.value);
  }

  private setupBrushComponent(type: Type<AbstractBrush>) {
    const componentFactory = this.resolver.resolveComponentFactory(type);
    this.brushContainer!.clear();
    let componentRef = this.brushContainer!.createComponent(componentFactory);
    this.currentBrush = componentRef.instance;
    this.currentBrush.shapeTerrainEditorComponent = this;
  }

  onWireframeChanged(): void {
    for (let x = 0; x < this.xCount; x++) {
      for (let y = 0; y < this.yCount; y++) {
        this.editorTerrainTiles[y][x].setWireframe(this.wireframe);
      }
    }

  }

  onShowTerrainTypeChanged(): void {
    for (let x = 0; x < this.xCount; x++) {
      for (let y = 0; y < this.yCount; y++) {
        if (this.editorTerrainTiles[y][x].hasPositions()) {
          if (this.showTerrainType) {
            this.editorTerrainTiles[y][x].showTerrainType();
          } else {
            this.editorTerrainTiles[y][x].hideTerrainType();
          }
        }
      }
    }
  }

  onShowTerrainTypeWorkerChanged(): void {
    for (let x = 0; x < this.xCount; x++) {
      for (let y = 0; y < this.yCount; y++) {
        if (this.editorTerrainTiles[y][x].hasPositions()) {
          if (this.showTerrainTypeWorker) {
            this.editorTerrainTiles[y][x].showTerrainTypeWorker();
          } else {
            this.editorTerrainTiles[y][x].hideTerrainTypeWorker();
          }
        }
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
      this.setupEditorTerrainTile((<BabylonTerrainTileImpl>babylonTerrainTile));
    });
  }

  private setupEditorTerrainTile(babylonTerrainTile: BabylonTerrainTileImpl) {
    let index = babylonTerrainTile.terrainTile.getIndex();
    if (babylonTerrainTile.getGroundMesh().material) {
      babylonTerrainTile.getGroundMesh().material!.wireframe = this.wireframe;
    }
    this.editorTerrainTiles[index.getY()][index.getX()].setBabylonTerrainTile(babylonTerrainTile);
    if (this.showTerrainType) {
      this.editorTerrainTiles[index.getY()][index.getX()].showTerrainType();
    }
  }

  private onPointerDown(position: Vector3) {
    for (let x = 0; x < this.xCount; x++) {
      for (let y = 0; y < this.yCount; y++) {
        if (this.editorTerrainTiles[y][x].isInside(position)) {
          this.editorTerrainTiles[y][x].onPointerDown(this.currentBrush!, position);
        }
      }
    }
  }

  private onPointerMove(position: Vector3, buttonDown: boolean) {
    for (let x = 0; x < this.xCount; x++) {
      for (let y = 0; y < this.yCount; y++) {
        if (this.editorTerrainTiles[y][x].isInside(position)) {
          this.editorTerrainTiles[y][x].onPointerMove(this.currentBrush!,
            position,
            buttonDown);
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
    const uint16Array = new Uint16Array(this.xCount * this.yCount * (BabylonTerrainTileImpl.NODE_X_COUNT + 1) * (BabylonTerrainTileImpl.NODE_Y_COUNT + 1));

    let index = 0;
    for (let y = 0; y < this.yCount; y++) {
      for (let x = 0; x < this.xCount; x++) {
        let editorTerrainTile = this.editorTerrainTiles[y][x];
        if (editorTerrainTile.hasPositions()) {
          this.editorTerrainTiles[y][x].fillHeights(height => {
            uint16Array[index++] = height;
          });
        } else {
          if (!this.originalUint16HeightMap) {
            this.messageService.add({ severity: 'error', summary: 'Error', detail: 'No original height map loaded' });
            return;
          }
          for (let i = 0; i < (BabylonTerrainTileImpl.NODE_X_COUNT + 1) * (BabylonTerrainTileImpl.NODE_Y_COUNT + 1); i++) {
            if (index < this.originalUint16HeightMap.length) {
              uint16Array[index] = this.originalUint16HeightMap[index];
            } else {
              uint16Array[index] = BabylonTerrainTileImpl.heightToUnit16(BabylonTerrainTileImpl.HEIGHT_DEFAULT);
            }
            index++;
          }
        }
      }
    }

    let compressed = pako.gzip(new Uint8Array(uint16Array.buffer));
    const blob = new Blob([compressed.buffer], { type: 'application/octet-stream' });

    this.terrainEditorControllerClient.updateCompressedHeightMap(this.planetConfig.getId(), blob)
      .then(() => {
        this.lastSavedTimeStamp = new Date().toLocaleString();
        this.lastSavedSize = `${compressed.length} bytes`;
        this.messageService.add({ severity: 'success', summary: 'Success', detail: 'Terrain saved' })
      })
      .catch(error => {
        this.messageService.add({ severity: 'error', summary: 'Error', detail: error.message })
      });
  }

  restartPlanetWarm() {
    this.editorService.executeServerCommand(EditorService.RESTART_PLANET_WARM);
  }

  restartPlanetCold() {
    this.editorService.executeServerCommand(EditorService.RESTART_PLANET_COLD);
  }
}


