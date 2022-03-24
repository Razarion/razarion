import { TreeNode } from "primeng/api";

// ---------- Common ----------
export abstract class GwtAngularFacade {
  canvasElement!: HTMLCanvasElement;
  mainCockpit!: MainCockpit;
  itemCockpitFrontend!: ItemCockpitFrontend;
  editorFrontendProvider!: EditorFrontendProvider;
  canvasResizeCallback!: Callback;
  statusProvider!: StatusProvider;
  threeJsRendererServiceAccess!: ThreeJsRendererServiceAccess;
  inputService!: InputService;
  terrainTypeService!: TerrainTypeService;

  abstract onCrash(): void;
}

export interface StatusProvider {
  getClientAlarms(): Alarm[];

  requestServerAlarms(): Promise<Alarm[]>;

  setStats(stats: Stats | null): void;

  getStats(): Stats | null;
}

export interface Callback {
  onCallback(): void;
}

export interface Rectangle {

}

export interface NativeMatrix {
  getColumnMajorFloat32Array(): Float32Array;
}

export interface Alarm {
  angularTypeString(): string;

  angularDateAsLong(): number;

  getText(): string;

  getId(): number;
}

export interface InputService {
  onViewFieldChanged(
    bottomLeftX: number, bottomLeftY: number,
    bottomRightX: number, bottomRightY: number,
    topRightX: number, topRightY: number,
    topLeftX: number, topLeftY: number): void;
}

export interface TerrainTypeService {
  getTerrainObjectConfig(id: number): TerrainObjectConfig;

  getSlopeConfig(id: number): SlopeConfig;

  getDrivewayConfig(drivewayConfigId: number): DrivewayConfig;

  getGroundConfig(groundConfigId: number): GroundConfig;

}

// ---------- Configs ----------
export interface TerrainObjectConfig {
}

export interface SlopeConfig {
}

export interface DrivewayConfig {
}

export interface GroundConfig {
  getId(): number;
  getInternalName(): string;
  getTopMaterial(): PhongMaterialConfig;
  getBottomMaterial(): PhongMaterialConfig | null;
  getSplatting(): GroundSplattingConfig | null;
}

export interface PhongMaterialConfig {
  getTextureId(): number;
  getScale(): number;
  getNormalMapId(): number;
  getNormalMapDepth(): number;
  getBumpMapId(): number;
  getBumpMapDepth(): number;
  getShininess(): number;
  getSpecularStrength(): number;
}

export interface GroundSplattingConfig {
  getTextureId(): number;
  getScale1(): number;
  getScale2(): number;
  getBlur(): number;
  getOffset(): number;
}

// ---------- Renderer ----------
export interface ThreeJsRendererServiceAccess {
  createTerrainTile(terrainTile: TerrainTile, threejsObject3D: any): ThreeJsTerrainTile;

  setViewFieldCenter(x: number, y: number): void;
}

export interface TerrainTile {
  getGroundTerrainTiles(): GroundTerrainTile[];

  getTerrainSlopeTiles(): TerrainSlopeTile[];

  getTerrainWaterTiles(): TerrainWaterTile[];

  getTerrainTileObjectLists(): TerrainTileObjectList[];
}

export interface GroundTerrainTile {
  groundConfigId: number;
  positions: Float32Array;
  norms: Float32Array
}

export interface TerrainSlopeTile {
  slopeConfigId: number;
  outerSlopeGeometry: SlopeGeometry | null;
  centerSlopeGeometry: SlopeGeometry | null;
  innerSlopeGeometry: SlopeGeometry | null;
}

export interface SlopeGeometry {
  positions: Float32Array;
  norms: Float32Array;
  uvs: Float32Array;
  slopeFactors: Float32Array;
}

export interface TerrainWaterTile {
  slopeConfigId: number;
  positions: Float32Array;
  shallowPositions: Float32Array;
  shallowUvs: Float32Array;
}

export interface TerrainTileObjectList {
  models: NativeMatrix[];
}

export interface ThreeJsTerrainTile {
  addToScene(): void;

  removeFromScene(): void;
}

// ---------- Item Cockpit ----------
export enum RadarState {
  NONE,
  NO_POWER,
  WORKING
}

export interface MainCockpit {
  show(): void;

  hide(): void;

  displayResources(resources: number): void;

  displayXps(xp: number, xp2LevelUp: number): void;

  displayLevel(levelNumber: number): void;

  getInventoryDialogButtonLocation(): Rectangle;

  getScrollHomeButtonLocation(): Rectangle;

  displayItemCount(itemCount: number, houseSpace: number): void;

  displayEnergy(consuming: number, generating: number): void;

  showRadar(radarState: RadarState): void;

  clean(): void;
}

export interface ItemCockpitFrontend {
  displayOwnSingleType(count: number, ownItemCockpit: OwnItemCockpit): void;

  displayOwnMultipleItemTypes(ownMultipleIteCockpits: OwnMultipleIteCockpit[]): void;

  displayOtherItemType(otherItemCockpit: OtherItemCockpit): void;

  dispose(): void;
}

export interface OwnItemCockpit {
  imageUrl: string;
  itemTypeName: string;
  itemTypeDescr: string;
  buildupItemInfos: BuildupItemCockpit[] | null;
  sellHandler: () => void;
}

export interface BuildupItemCockpit {
  imageUrl: string;
  price: number;
  itemCount: number;
  itemLimit: number;
  enabled: boolean;
  tooltip: string;
  progress: any;

  onBuild(): void;

  setAngularZoneRunner(angularZoneRunner: AngularZoneRunner): void;
}

export interface AngularZoneRunner {
  runInAngularZone(callback: any): void;
}

export interface OwnMultipleIteCockpit {
  ownItemCockpit: OwnItemCockpit;
  count: number;
  tooltip: string;

  onSelect(): void;
}

export interface OtherItemCockpit {
  imageUrl: string;
  itemTypeName: string;
  itemTypeDescr: string;
  baseName: string;
  type: string;
  friend: boolean;
}

// ---------- Editor ----------
export interface EditorFrontendProvider {
  getGenericEditorFrontendProvider(): GenericEditorFrontendProvider;

  getClientPerfmonStatistics(): PerfmonStatistic[];

  getWorkerPerfmonStatistics(): Promise<PerfmonStatistic[]>;

  getTerrainMarkerService(): TerrainMarkerService;

  getTerrainEditorService(): TerrainEditorService;

  getCameraFrontendService(): RendererEditorService;
}

export interface GenericEditorFrontendProvider {
  collectionNames(): string[];

  requestObjectNameIds(collectionName: string): Promise<ObjectNameId[]>;

  requestObjectNameId(collectionName: string, configId: number): Promise<ObjectNameId>;

  createConfig(collectionName: string): Promise<GwtAngularPropertyTable>;

  readConfig(collectionName: string, configId: number): Promise<GwtAngularPropertyTable>;

  updateConfig(collectionName: string, gwtAngularPropertyTable: GwtAngularPropertyTable): Promise<void>;

  deleteConfig(collectionName: string, gwtAngularPropertyTable: GwtAngularPropertyTable): Promise<void>;

  colladaConvert(gwtAngularPropertyTable: GwtAngularPropertyTable, colladaString: string): Promise<void>;
}

export interface TerrainMarkerService {
  showPosition(x: number, y: number): void;

  showPolygon(polygon: any): void;

  activatePositionCursor(positionCallback: PositionCallback): void;

  activatePolygonCursor(polygon: any, polygonCallback: PolygonCallback): void;
}

export interface PositionCallback {
  position(x: number, y: number): void;
}

export interface PolygonCallback {
  polygon(polygon: any): void;
}

export interface GwtAngularPropertyTable {
  rootTreeNodes: TreeNode[];
  configId: number;
}

export interface ObjectNameId {
  id: number;
  internalName: string;

  toString(): string;
}

export interface AngularTreeNodeData {
  name: string;
  value: any;
  options: string[];
  propertyEditorSelector: string;
  nullable: boolean;
  deleteAllowed: boolean;
  createAllowed: boolean;
  canHaveChildren: boolean;

  onCreate(gwtAngularPropertyTable: GwtAngularPropertyTable): void;

  onDelete(gwtAngularPropertyTable: GwtAngularPropertyTable): void;

  setValue(value: any): void;
}

export interface TerrainEditorService {
  activate(): void;

  deactivate(): void;

  save(): Promise<string>;

  isSlopeMode(): boolean;

  setSlopeMode(slopeMode: boolean): void;

  // --- Slope mode
  getAllSlopes(): Promise<ObjectNameId[]>;

  getCursorRadius(): number;

  setCursorRadius(cursorRadius: number): void;

  getCursorCorners(): number;

  setCursorCorners(cursorCorners: number): void;

  setSlope4New(slope4New: ObjectNameId): void;

  isInvertedSlope(): boolean;

  setInvertedSlope(invertedSlope: boolean): void

  getAllDriveways(): Promise<ObjectNameId[]>;

  setDrivewayMode(drivewayMode: boolean): void;

  isDrivewayMode(): boolean;

  setDriveway4New(driveway4New: ObjectNameId): void;

  // --- Terrain Object Mode
  getAllTerrainObjects(): Promise<ObjectNameId[]>;

  setTerrainObject4New(terrainObject4New: ObjectNameId): void;

  getTerrainObjectRandomZRotation(): number;

  setTerrainObjectRandomZRotation(terrainObjectRandomZRotation: number): void;

  getTerrainObjectRandomScale(): number;

  setTerrainObjectRandomScale(terrainObjectRandomScale: number): void;
}

export interface ImageGalleryItem {
  id: number;
  size: number;
  type: string;
  internalName: string
}

export interface RendererEditorService {
  isRenderInterpolation(): boolean;

  setRenderInterpolation(value: boolean): void;

  isCallGetError(): boolean;

  setCallGetError(callGetError: boolean): void;

  getCameraXPosition(): number;

  setCameraXPosition(x: number): void;

  getCameraYPosition(): number;

  setCameraYPosition(y: number): void;

  getCameraZPosition(): number;

  setCameraZPosition(z: number): void;

  getCameraXRotation(): number;

  setCameraXRotation(x: number): void;

  getCameraZRotation(): number;

  setCameraZRotation(z: number): void;

  getCameraOpeningAngleY(): number;

  setCameraOpeningAngleY(y: number): void;

  getRenderTaskRunnerControls(): RenderTaskRunnerControl[];
}

export interface RenderTaskRunnerControl {
  getName(): string;

  isEnabled(): boolean;

  setEnabled(enabled: boolean): void;
}

// ---------- Performance ----------
export interface PerfmonStatistic {
  getPerfmonEnumString(): string;

  getPerfmonStatisticEntriesArray(): PerfmonStatisticEntry[];
}

export interface PerfmonStatisticEntry {
  getFrequency(): number;

  getAvgDuration(): number;

  getSamples(): number;

  getDateAsLong(): number;
}

export enum PerfmonEnum {
  RENDERER,
  GAME_ENGINE,
  CLIENT_GAME_ENGINE_UPDATE,
  BOT_TICKER,
  BOT_SCENE_TICKER,
  BOT_TIMER,
  DETAILED_TRACKING,
  COVER_FADE,
  DRAW_MINI_MAP,
  PERFMON_SEND_TO_CLIENT,
  PERFMON_ANALYSE,
  PLAYBACK,
  SCENE_RUNNER,
  SCENE_WAIT,
  TRAIL_SERVICE,
  SCROLL,
  SCROLL_AUTO,
  TIP_SCROLL,
  TIP_SPAWN,
  TIP_GUI_POINTING,
  REGISTER,
  USER_SET_NAME,
  SERVER_RESTART_WATCHDOG,
  RELOAD_CLIENT_WRONG_INTERFACE_VERSION,
  ESTABLISH_CONNECTION,
  WAIT_RESTART,
  QUEST_PROGRESS_PANEL_TEXT_REFRESHER
}
