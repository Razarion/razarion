import {
  AfterViewInit,
  Component,
  ComponentFactoryResolver,
  OnDestroy,
  Type,
  ViewChild,
  ViewContainerRef
} from '@angular/core';
import { HttpClient } from "@angular/common/http";
import { GwtAngularService } from "../../gwtangular/GwtAngularService";
import { MessageService } from "primeng/api";
import pako from 'pako';
import { HttpClient as HttpClientAdapter, RestResponse, TerrainType } from '../../generated/razarion-share';
import { PlanetConfig } from "../../gwtangular/GwtAngularFacade";
import { BabylonRenderServiceAccessImpl } from "../../game/renderer/babylon-render-service-access-impl.service";
import { Nullable, Observer, PointerEventTypes, PointerInfo, Vector3 } from "@babylonjs/core";
import { EditorService } from "../editor-service";
import { BabylonTerrainTileImpl } from 'src/app/game/renderer/babylon-terrain-tile.impl';
import { EditorTerrainTile } from './editor-terrain-tile';
import { GwtInstance } from 'src/app/gwtangular/GwtInstance';
import { TerrainEditorControllerClient, TerrainHeightMapControllerClient } from 'src/app/generated/razarion-share';
import { TypescriptGenerator } from 'src/app/backend/typescript-generator';
import { AbstractBrush, BrushContext } from "./brushes/abstract-brush";
import { FixHeightBrushComponent } from './brushes/fix-height-brush.component';
import { FlattenBrushComponent } from "./brushes/flattem-brush.component";
import { RadarComponent } from 'src/app/game/cockpit/main/radar/radar.component';

@Component({
    selector: 'shape-terrain-editor',
    templateUrl: './shape-terrain-editor.component.html',
    standalone: false
})
export class ShapeTerrainEditorComponent implements AfterViewInit, OnDestroy {
  wireframe: boolean = false;
  showTerrainType: boolean = false;
  showTerrainTypeWorker: boolean = false;
  private pointerObservable: Nullable<Observer<PointerInfo>> = null;
  private planetConfig: PlanetConfig;
  private editorTerrainTiles: EditorTerrainTile[][] = [];
  private xTileCount: number;
  private yTileCount: number;
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
    this.xTileCount = Math.ceil(this.planetConfig.getSize().getX() / BabylonTerrainTileImpl.NODE_X_COUNT);
    this.yTileCount = Math.ceil(this.planetConfig.getSize().getY() / BabylonTerrainTileImpl.NODE_Y_COUNT);
    for (let y = 0; y < this.yTileCount; y++) {
      this.editorTerrainTiles[y] = [];
      for (let x = 0; x < this.xTileCount; x++) {
        this.editorTerrainTiles[y][x] = new EditorTerrainTile(renderService,
          gwtAngularService.gwtAngularFacade.inputService,
          gwtAngularService.gwtAngularFacade.terrainUiService,
          GwtInstance.newIndex(x, y));
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
    for (let x = 0; x < this.xTileCount; x++) {
      for (let y = 0; y < this.yTileCount; y++) {
        this.editorTerrainTiles[y][x].setWireframe(this.wireframe);
      }
    }

  }

  onShowTerrainTypeChanged(): void {
    for (let x = 0; x < this.xTileCount; x++) {
      for (let y = 0; y < this.yTileCount; y++) {
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
    for (let x = 0; x < this.xTileCount; x++) {
      for (let y = 0; y < this.yTileCount; y++) {
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
            this.modelTerrain(pickingInfo.pickedPoint);
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
            if ((pointerInfo.event.buttons & 0x01) == 0x01) {
              this.modelTerrain(pickingInfo.pickedPoint);
            }
          }
          break;
        }
      }
    });

  }

  private loadEditorTerrainTiles() {
    this.renderService.getAllBabylonTerrainTile().forEach(babylonTerrainTile => {
       this.setupEditorTerrainTile(babylonTerrainTile);
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

  private modelTerrain(position: Vector3) {
    if (this.currentBrush!.isContextDependent()) {
      let brushContext = new BrushContext(this.currentBrush!);

      for (let x = 0; x < this.xTileCount; x++) {
        for (let y = 0; y < this.yTileCount; y++) {
          if (this.editorTerrainTiles[y][x].isInside(position)) {
            this.editorTerrainTiles[y][x].prepareContext(brushContext, position);
          }
        }
      }
      brushContext.finishPrepare();
      this.currentBrush!.setBrushContext(brushContext);
    }

    for (let x = 0; x < this.xTileCount; x++) {
      for (let y = 0; y < this.yTileCount; y++) {
        if (this.editorTerrainTiles[y][x].isInside(position)) {
          this.editorTerrainTiles[y][x].modelTerrain(this.currentBrush!, position);
        }
      }
    }
    this.currentBrush!.setBrushContext(null);
  }

  deactivate() {
    if (this.pointerObservable) {
      this.renderService.getScene().onPointerObservable.remove(this.pointerObservable);
      this.pointerObservable = null;
    }
  }

  save() {
    const uint16Array = new Uint16Array(this.xTileCount * this.yTileCount * BabylonTerrainTileImpl.NODE_X_COUNT * BabylonTerrainTileImpl.NODE_Y_COUNT);

    let index = 0;
    for (let y = 0; y < this.yTileCount; y++) {
      for (let x = 0; x < this.xTileCount; x++) {
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
          for (let i = 0; i < BabylonTerrainTileImpl.NODE_X_COUNT * BabylonTerrainTileImpl.NODE_Y_COUNT; i++) {
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

  generateMiniMap(canvas: HTMLCanvasElement) {
    canvas.width = RadarComponent.MINI_MAP_IMAGE_WIDTH;
    canvas.height = RadarComponent.MINI_MAP_IMAGE_HEIGHT;
    const context = canvas.getContext('2d')!;
    context.fillStyle = "rgba(0, 0, 0, 1)";
    context.fillRect(0, 0, canvas.width, canvas.height)
    const factor = RadarComponent.MINI_MAP_IMAGE_WIDTH / (this.xTileCount * BabylonTerrainTileImpl.NODE_SIZE * BabylonTerrainTileImpl.NODE_X_COUNT);

    let y = 0;
    const self = this;
    let invokeDraw = function () {
      self.drawMiniMapLine(y, context, factor);
      y++;
      if (y < self.yTileCount) {
        setTimeout(invokeDraw);
      }
    }
    setTimeout(invokeDraw);
  }

  private drawMiniMapLine(y: number, context: CanvasRenderingContext2D, factor: number) {
    for (let x = 0; x < this.xTileCount; x++) {
      let editorTerrainTile = this.editorTerrainTiles[y][x];
      const xNodeTile = x * BabylonTerrainTileImpl.NODE_SIZE * BabylonTerrainTileImpl.NODE_X_COUNT;
      const yNodeTile = y * BabylonTerrainTileImpl.NODE_SIZE * BabylonTerrainTileImpl.NODE_Y_COUNT;
      const xCanvas = xNodeTile * factor;
      const yCanvas = (((this.yTileCount - 1) * BabylonTerrainTileImpl.NODE_SIZE * BabylonTerrainTileImpl.NODE_Y_COUNT) - yNodeTile) * factor;

      if (editorTerrainTile.hasPositions()) {
        context.translate(xCanvas, yCanvas);
        editorTerrainTile.drawMiniMap(
          context,
          true,
          factor,
          0,
          "rgba(0, 0, 255, 0.5)",
          "rgba(0, 255, 0, 0.5)",
          "rgba(255, 0, 0, 0.5)");
        context.translate(-xCanvas, -yCanvas);
      } else {
        if (!this.originalUint16HeightMap) {
          this.messageService.add({ severity: 'error', summary: 'Error', detail: 'No original height map loaded' });
          return;
        }

        const indexTileNode = (BabylonTerrainTileImpl.NODE_Y_COUNT * BabylonTerrainTileImpl.NODE_Y_COUNT) * (y * this.xTileCount + x)

        const originalUint16HeightMap = this.originalUint16HeightMap;
        const setupTerrainType = function (indexNode: number): TerrainType {
          const indexAbsolute = indexNode + indexTileNode;
          if (indexAbsolute >= originalUint16HeightMap.length) {
            return TerrainType.LAND;
          }
          const blHeight = BabylonTerrainTileImpl.uint16ToHeight(originalUint16HeightMap[indexAbsolute]);
          if (indexAbsolute + 1 >= originalUint16HeightMap.length) {
            return TerrainType.LAND;
          }
          const brHeight = BabylonTerrainTileImpl.uint16ToHeight(originalUint16HeightMap[indexAbsolute + 1]);
          const indexTop = indexAbsolute + BabylonTerrainTileImpl.NODE_Y_COUNT;
          if (indexTop >= originalUint16HeightMap.length) {
            return TerrainType.LAND;
          }
          const tlHeight = BabylonTerrainTileImpl.uint16ToHeight(originalUint16HeightMap[indexTop]);

          if (indexTop + 1 >= originalUint16HeightMap.length) {
            return TerrainType.LAND;
          }
          const trHeight = BabylonTerrainTileImpl.uint16ToHeight(originalUint16HeightMap[indexTop + 1]);
          return <any>EditorTerrainTile.setupTerrainType(blHeight, brHeight, trHeight, tlHeight);
        }
        context.translate(xCanvas, yCanvas);

        let indexNode = 0;
        for (let yNode = 0; yNode < BabylonTerrainTileImpl.NODE_Y_COUNT; yNode++) {
          for (let xNode = 0; xNode < BabylonTerrainTileImpl.NODE_X_COUNT; xNode++) {
            switch (setupTerrainType(indexNode)) {
              case TerrainType.WATER:
                context.fillStyle = "rgba(0, 0, 255, 0.5)";
                break;
              case TerrainType.LAND:
                context.fillStyle = "rgba(0, 255, 0, 0.5)";
                break;
              case TerrainType.BLOCKED:
                context.fillStyle = "rgba(255, 0, 0, 0.5)";
                break;
              default:
                context.fillStyle = "rgba(1, 1, 1, 1)";
            }
            indexNode++;
            context.fillRect(
              xNode * BabylonTerrainTileImpl.NODE_SIZE * factor,
              (BabylonTerrainTileImpl.NODE_Y_COUNT - yNode) * BabylonTerrainTileImpl.NODE_SIZE * factor,
              BabylonTerrainTileImpl.NODE_SIZE * factor,
              BabylonTerrainTileImpl.NODE_SIZE * factor);
          }
        }
        context.translate(-xCanvas, -yCanvas);
      }
    }
  }


  restartPlanetWarm() {
    this.editorService.executeServerCommand(EditorService.RESTART_PLANET_WARM);
  }

  restartPlanetCold() {
    this.editorService.executeServerCommand(EditorService.RESTART_PLANET_COLD);
  }
}


