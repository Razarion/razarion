import {
  AfterViewInit,
  Component,
  ComponentFactoryResolver,
  OnDestroy,
  Type,
  ViewChild,
  ViewContainerRef
} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {GwtAngularService} from "../../gwtangular/GwtAngularService";
import {MessageService} from "primeng/api";
import * as pako from "pako";
import {HttpClient as HttpClientAdapter, RestResponse} from '../../generated/razarion-share';
import {PlanetConfig} from "../../gwtangular/GwtAngularFacade";
import {BabylonRenderServiceAccessImpl} from "../../game/renderer/babylon-render-service-access-impl.service";
import {Nullable, Observer, PointerEventTypes, PointerInfo, Vector3} from "@babylonjs/core";
import {EditorService} from "../editor-service";
import {BabylonTerrainTileImpl} from 'src/app/game/renderer/babylon-terrain-tile.impl';
import {EditorTerrainTile} from './editor-terrain-tile';
import {GwtInstance} from 'src/app/gwtangular/GwtInstance';
import {TerrainEditorControllerClient, TerrainHeightMapControllerClient} from 'src/app/generated/razarion-share';
import {TypescriptGenerator} from 'src/app/backend/typescript-generator';
import {AbstractBrush, BrushContext} from "./brushes/abstract-brush";
import {FixHeightBrushComponent} from './brushes/fix-height-brush.component';
import {FlattenBrushComponent} from "./brushes/flattem-brush.component";
import {RaiseHeightBrushComponent} from './brushes/raise-height-brush.component';
import {SmoothBrushComponent} from './brushes/smooth-brush.component';
import {NoiseBrushComponent} from './brushes/noise-brush.component';
import {ErosionBrushComponent} from './brushes/erosion-brush.component';
import {FixBoundaryBrushComponent} from './brushes/fix-boundary-brush.component';
import {RadarComponent} from 'src/app/game/cockpit/main/radar/radar.component';
import {Button} from 'primeng/button';
import {Divider} from 'primeng/divider';
import {Checkbox} from 'primeng/checkbox';
import {FormsModule} from '@angular/forms';
import {SelectButton} from 'primeng/selectbutton';
import {FileUpload} from 'primeng/fileupload';
import {BotConfig} from 'src/app/generated/razarion-share';

@Component({
  selector: 'height-map-terrain-editor',
  imports: [
    Button,
    Divider,
    Checkbox,
    FormsModule,
    SelectButton,
    FileUpload
  ],
  templateUrl: './height-map-terrain-editor.component.html'
})
export class HeightMapTerrainEditorComponent implements AfterViewInit, OnDestroy {
  wireframe: boolean = false;
  showTerrainType: boolean = false;
  showTerrainTypeWorker: boolean = false;
  showMaterialIndex: boolean = false;
  private pointerObservable: Nullable<Observer<PointerInfo>> = null;
  private planetConfig: PlanetConfig;
  private editorTerrainTiles: EditorTerrainTile[][] = [];
  private xTileCount: number;
  private yTileCount: number;
  private terrainEditorControllerClient: TerrainEditorControllerClient;
  lastSavedTimeStamp: string = "";
  lastSavedSize: string = "";
  lastSavedSizeLoading: boolean = false;
  private originalUint16HeightMap?: Uint16Array;
  fixedUint16HeightMap?: Uint16Array;
  @ViewChild('fileUploadElement')
  fileUploadElement!: FileUpload;

  @ViewChild('brushContainer', {read: ViewContainerRef})
  brushContainer?: ViewContainerRef;
  brushOptions = [
    {label: 'Fix height', value: FixHeightBrushComponent},
    {label: 'Flatten', value: FlattenBrushComponent},
    {label: 'Raise height', value: RaiseHeightBrushComponent},
    {label: 'Smooth', value: SmoothBrushComponent},
    {label: 'Noise', value: NoiseBrushComponent},
    {label: 'Erosion', value: ErosionBrushComponent},
    {label: 'Fix boundary', value: FixBoundaryBrushComponent}
  ];
  selectedBrush?: Type<AbstractBrush>;

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
        return <RestResponse<R>>fetch("/" + requestConfig.url, {
          headers: {
            'Content-Type': 'application/octet-stream'
          }
        })
      }
    });

    terrainHeightMapControllerClient.getCompressedHeightMap(this.planetConfig.getId())
      .then(response => response.arrayBuffer())
      .then(buffer => this.originalUint16HeightMap = new Uint16Array(buffer))
      .catch(error => this.messageService.add({severity: 'error', summary: 'Error', detail: error.message}));

    this.renderService.setEditorTerrainTileCreationCallback((babylonTerrainTile: BabylonTerrainTileImpl) => {
      this.setupEditorTerrainTile(babylonTerrainTile);
      return undefined;
    });

    this.loadHeightmapSize();
  }

  ngAfterViewInit(): void {
    Promise.resolve().then(() => {
      this.selectedBrush = FixHeightBrushComponent;
      this.setupBrushComponent(FixHeightBrushComponent);
    });
  }

  ngOnDestroy(): void {
    this.renderService.setEditorTerrainTileCreationCallback(undefined);
  }

  activate() {
    this.registerInputEvents();
    this.loadEditorTerrainTiles();
    if (this.currentBrush) {
      this.currentBrush.showCursor()
    }
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

  onShowMaterialIndexChanged(): void {
    for (let x = 0; x < this.xTileCount; x++) {
      for (let y = 0; y < this.yTileCount; y++) {
        if (this.editorTerrainTiles[y][x].hasPositions()) {
          if (this.showMaterialIndex) {
            this.editorTerrainTiles[y][x].showMaterialIndex();
          } else {
            this.editorTerrainTiles[y][x].hideMaterialIndex();
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
          break;
        }
        case PointerEventTypes.POINTERMOVE: {
          let pickingInfo = this.renderService.setupTerrainPickPoint();
          if (pickingInfo && pickingInfo.pickedPoint) {
            if ((pointerInfo.event.buttons & 0x01) == 0x01) {
              // Stamp-mode brushes only apply on click, not on drag
              if (!this.currentBrush || !this.currentBrush.isStampMode()) {
                this.modelTerrain(pickingInfo.pickedPoint);
              }
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
    this.editorTerrainTiles[index.getY()][index.getX()].setBabylonTerrainTile(babylonTerrainTile);
    this.editorTerrainTiles[index.getY()][index.getX()].setWireframe(this.wireframe);
    if (this.showTerrainType) {
      this.editorTerrainTiles[index.getY()][index.getX()].showTerrainType();
    }
  }

  private modelTerrain(position: Vector3) {
    const brushRadius = this.currentBrush!.getEffectiveRadius();

    if (this.currentBrush!.isContextDependent()) {
      let brushContext = new BrushContext(this.currentBrush!);

      for (let x = 0; x < this.xTileCount; x++) {
        for (let y = 0; y < this.yTileCount; y++) {
          if (this.editorTerrainTiles[y][x].isInside(position, brushRadius)) {
            this.editorTerrainTiles[y][x].prepareContext(brushContext, position);
          }
        }
      }
      brushContext.finishPrepare();
      this.currentBrush!.setBrushContext(brushContext);
    }

    this.currentBrush!.preCalculate(position);

    for (let x = 0; x < this.xTileCount; x++) {
      for (let y = 0; y < this.yTileCount; y++) {
        if (this.editorTerrainTiles[y][x].isInside(position, brushRadius)) {
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
    if (this.currentBrush) {
      this.currentBrush.hideCursor()
    }
  }

  loadHeightmapSize() {
    this.lastSavedSizeLoading = true;
    this.terrainEditorControllerClient.getHeightmapSize(this.planetConfig.getId())
      .then(size => {
        this.lastSavedSize = this.formatSize(size);
        this.lastSavedSizeLoading = false;
      })
      .catch(error => {
        console.error(error);
        this.lastSavedSizeLoading = false;
      });
  }

  formatSize(bytes: number): string {
    if (bytes < 0) return 'Error';
    if (bytes < 1024) return `${bytes} B`;
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
    return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
  }

  save() {
    const compressedHeightMap = this.generateCompressedHeightMap()
    if (!compressedHeightMap) {
      return;
    }

    const blob = new Blob([compressedHeightMap.buffer as ArrayBuffer], {type: 'application/octet-stream'});

    this.terrainEditorControllerClient.updateCompressedHeightMap(this.planetConfig.getId(), blob)
      .then(() => {
        this.lastSavedTimeStamp = new Date().toLocaleString();
        this.lastSavedSize = this.formatSize(compressedHeightMap.length);
        this.messageService.add({severity: 'success', summary: 'Success', detail: 'Terrain saved'})
      })
      .catch(error => {
        this.messageService.add({severity: 'error', summary: 'Error', detail: error.message})
      });
  }

  private generateCompressedHeightMap(): Uint8Array | undefined {
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
            this.messageService.add({
              severity: 'error',
              summary: 'Error',
              detail: 'No original height map loaded'
            });
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

    return pako.gzip(new Uint8Array(uint16Array.buffer));
  }

  generateMiniMap(canvas: HTMLCanvasElement, botConfigs?: BotConfig[]) {
    const width = RadarComponent.MINI_MAP_IMAGE_WIDTH;
    const height = RadarComponent.MINI_MAP_IMAGE_HEIGHT;
    canvas.width = width;
    canvas.height = height;
    const context = canvas.getContext('2d')!;
    const imageData = context.createImageData(width, height);
    const data = imageData.data;

    const totalNodesX = this.xTileCount * BabylonTerrainTileImpl.NODE_X_COUNT;
    const totalNodesY = this.yTileCount * BabylonTerrainTileImpl.NODE_Y_COUNT;
    const tileNodesX = BabylonTerrainTileImpl.NODE_X_COUNT;
    const tileNodesY = BabylonTerrainTileImpl.NODE_Y_COUNT;

    for (let py = 0; py < height; py++) {
      // Flip Y: top of canvas = high Y in world
      const nodeY = Math.floor((height - 1 - py) * totalNodesY / height);
      const tileY = Math.floor(nodeY / tileNodesY);
      const localY = nodeY - tileY * tileNodesY;

      for (let px = 0; px < width; px++) {
        const nodeX = Math.floor(px * totalNodesX / width);
        const tileX = Math.floor(nodeX / tileNodesX);
        const localX = nodeX - tileX * tileNodesX;

        let h = BabylonTerrainTileImpl.HEIGHT_DEFAULT;

        const editorTile = this.editorTerrainTiles[tileY]?.[tileX];
        if (editorTile && editorTile.hasPositions()) {
          h = editorTile.getHeightAtNode(localX, localY);
        } else if (this.originalUint16HeightMap) {
          const tileIndex = tileY * this.xTileCount + tileX;
          const idx = tileIndex * tileNodesX * tileNodesY + localY * tileNodesX + localX;
          if (idx < this.originalUint16HeightMap.length) {
            h = BabylonTerrainTileImpl.uint16ToHeight(this.originalUint16HeightMap[idx]);
          }
        }

        const rgb = EditorTerrainTile.heightToRgb(h);
        const offset = (py * width + px) * 4;
        data[offset] = rgb[0];
        data[offset + 1] = rgb[1];
        data[offset + 2] = rgb[2];
        data[offset + 3] = 255;
      }
    }

    context.putImageData(imageData, 0, 0);

    if (botConfigs) {
      this.drawBotOverlay(context, botConfigs, width, height, totalNodesX, totalNodesY);
    }
  }

  private drawBotOverlay(context: CanvasRenderingContext2D, botConfigs: BotConfig[], width: number, height: number, totalNodesX: number, totalNodesY: number) {
    // Draw bot grounds as orange squares
    for (const botConfig of botConfigs) {
      if (botConfig.groundBoxPositions?.length) {
        for (const pos of botConfig.groundBoxPositions) {
          const px = Math.round(pos.x * width / totalNodesX);
          const py = height - 1 - Math.round(pos.y * height / totalNodesY);
          context.fillStyle = 'rgba(255, 160, 0, 0.8)';
          context.fillRect(px - 2, py - 2, 5, 5);
        }
      }

    }
  }

  restartPlanetWarm() {
    this.editorService.executeServerCommand(EditorService.RESTART_PLANET_WARM);
  }

  restartPlanetCold() {
    this.editorService.executeServerCommand(EditorService.RESTART_PLANET_COLD);
  }

  onDownload() {
    const compressedHeightMap = this.generateCompressedHeightMap()
    if (!compressedHeightMap) {
      return;
    }

    const blob = new Blob([compressedHeightMap.buffer as ArrayBuffer], {type: 'application/octet-stream'});

    const link = document.createElement("a");
    link.href = URL.createObjectURL(blob);
    link.setAttribute("download", "CompressedHeightMap.bin");
    link.click();
  }

  onImportHeightMap(event: any) {
    this.fileUploadElement.clear();

    const blob = new Blob([event.files[0]], {type: 'application/octet-stream'});

    this.terrainEditorControllerClient.updateCompressedHeightMap(this.planetConfig.getId(), blob)
      .then(() => {
        this.lastSavedTimeStamp = new Date().toLocaleString();
        this.lastSavedSize = this.formatSize(event.files[0].length);
        this.messageService.add({severity: 'success', summary: 'Success', detail: 'Terrain saved'})
        this.fixedUint16HeightMap = undefined;
      })
      .catch(error => {
        this.messageService.add({severity: 'error', summary: 'Error', detail: error.message})
      });

  }

  fixWaterLevel() {
    const xNodeCount = this.xTileCount * BabylonTerrainTileImpl.NODE_X_COUNT;
    const yNodeCount = this.yTileCount * BabylonTerrainTileImpl.NODE_Y_COUNT;

    this.fixedUint16HeightMap = new Uint16Array(this.originalUint16HeightMap!.length);


    for (let y = 0; y < yNodeCount - 1; y++) {
      for (let x = 0; x < xNodeCount - 1; x++) {
        const bLHeight = this.groundHeightAt(x, y);
        const bRHeight = this.groundHeightAt(x + 1, y);
        const tLHeight = this.groundHeightAt(x + 1, y + 1);
        const tRHeight = this.groundHeightAt(x, y + 1);

        const index = this.groundHeightMapIndex(x, y);
        if (bLHeight >= BabylonTerrainTileImpl.WATER_LEVEL
          && bRHeight >= BabylonTerrainTileImpl.WATER_LEVEL
          && tLHeight >= BabylonTerrainTileImpl.WATER_LEVEL
          && tRHeight >= BabylonTerrainTileImpl.WATER_LEVEL) {
          this.fixedUint16HeightMap[index] = this.originalUint16HeightMap![index]!;
        } else if (bLHeight <= BabylonTerrainTileImpl.WATER_LEVEL
          && bRHeight <= BabylonTerrainTileImpl.WATER_LEVEL
          && tLHeight <= BabylonTerrainTileImpl.WATER_LEVEL
          && tRHeight <= BabylonTerrainTileImpl.WATER_LEVEL) {
          this.fixedUint16HeightMap[index] = this.originalUint16HeightMap![index]!;
        } else {
          this.fixedUint16HeightMap[index] = BabylonTerrainTileImpl.heightToUnit16(BabylonTerrainTileImpl.WATER_LEVEL);
        }
      }
    }
  }

  groundHeightMapIndex(xTerrainNode: number, yTerrainNode: number) {
    const xTerrainTileIndex = Math.floor(xTerrainNode / BabylonTerrainTileImpl.NODE_X_COUNT);
    const yTerrainTileIndex = Math.floor(yTerrainNode / BabylonTerrainTileImpl.NODE_Y_COUNT);

    const startTileNodeIndex = BabylonTerrainTileImpl.TILE_NODE_SIZE * (yTerrainTileIndex * this.xTileCount + xTerrainTileIndex)

    const xOffset = xTerrainNode - (xTerrainTileIndex * BabylonTerrainTileImpl.NODE_X_COUNT);
    const yOffset = yTerrainNode - (yTerrainTileIndex * BabylonTerrainTileImpl.NODE_Y_COUNT);
    const offsetTileNodeIndex = yOffset * BabylonTerrainTileImpl.NODE_X_COUNT + xOffset;

    return startTileNodeIndex + offsetTileNodeIndex;
  }

  groundHeightAt(xTerrainNode: number, yTerrainNode: number) {
    const uint16Height = this.originalUint16HeightMap![this.groundHeightMapIndex(xTerrainNode, yTerrainNode)];

    return BabylonTerrainTileImpl.uint16ToHeight(uint16Height!);
  }

  saveFixed() {
    const compressedHeightMap = pako.gzip(new Uint8Array(this.fixedUint16HeightMap!.buffer));
    const blob = new Blob([compressedHeightMap.buffer as ArrayBuffer], {type: 'application/octet-stream'});
    this.terrainEditorControllerClient.updateCompressedHeightMap(this.planetConfig.getId(), blob)
      .then(() => {
        this.lastSavedTimeStamp = new Date().toLocaleString();
        this.lastSavedSize = this.formatSize(compressedHeightMap!.length);
        this.messageService.add({severity: 'success', summary: 'Success', detail: 'Terrain saved'})
        this.fixedUint16HeightMap = undefined;
      })
      .catch(error => {
        this.messageService.add({severity: 'error', summary: 'Error', detail: error.message})
      });
  }
}


