import {TreeNode} from "primeng/api";

// ---------- Root ----------

export abstract class GwtAngularFacade {
  gwtAngularBoot!: GwtAngularBoot;
  mainCockpit!: MainCockpit;
  itemCockpitFrontend!: ItemCockpitFrontend;
  baseItemPlacerPresenter!: BaseItemPlacerPresenter;
  editorFrontendProvider!: EditorFrontendProvider;
  statusProvider!: StatusProvider;
  threeJsRendererServiceAccess!: ThreeJsRendererServiceAccess;
  inputService!: InputService;
  terrainTypeService!: TerrainTypeService;
  threeJsModelPackService!: ThreeJsModelPackService;
  assetService!: AssetService;

  abstract onCrash(): void;
}

// ---------- Boot ----------

export interface GwtAngularBoot {
  loadThreeJsModels(threeJsModelConfigs: ThreeJsModelConfig[]): Promise<void>;
}

// ---------- Common ----------

export interface Index {
  getX(): number;

  getY(): number;

  toString(): string;
}

export interface DecimalPosition {
  getX(): number;

  getY(): number;

  toString(): string;
}

export interface Vertex {
  getX(): number;

  getY(): number;

  getZ(): number;

  toString(): string;
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

  onMouseMove(x: number, y: number, primaryButtonDown: boolean): void;

  onMouseDown(x: number, y: number): void;

  onMouseUp(x: number, y: number): void;

}

export interface TerrainTypeService {
  getTerrainObjectConfig(id: number): TerrainObjectConfig;

  getSlopeConfig(id: number): SlopeConfig;

  getDrivewayConfig(drivewayConfigId: number): DrivewayConfig;

  getGroundConfig(groundConfigId: number): GroundConfig;

  getWaterConfig(waterConfigId: number): WaterConfig;
}

export interface ThreeJsModelPackService {
  getThreeJsModelPackConfig(id: number): ThreeJsModelPackConfig;
}

export interface AssetService {
  getMeshContainers(): MeshContainer[];
}

// ---------- Configs ----------
export interface TerrainObjectConfig {
  getThreeJsModelPackConfigId(): number;

  getId(): number;

  getInternalName(): string;

  getRadius(): number;

  setRadius(radius: number): void;

  toString(): string;
}

export interface SlopeConfig {
  getId(): number;

  getInternalName(): string;

  getThreeJsMaterial(): number;

  getShallowWaterThreeJsMaterial(): number;

  getGroundConfigId(): number;

  getWaterConfigId(): number;
}

export interface DrivewayConfig {
}

export interface GroundConfig {
  getId(): number;

  getInternalName(): string;

  getTopThreeJsMaterial(): number;

  getBottomThreeJsMaterial(): number;

  getSplatting(): GroundSplattingConfig | null;
}

export interface WaterConfig {
  getId(): number;

  getInternalName(): string;

  getMaterial(): number;
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

export interface ThreeJsModelConfig {
  getId(): number;

  getInternalName(): string;

  getType(): ThreeJsModelConfig.Type

  getNodeMaterialId(): number | null;

  isDisabled():boolean;
}

export namespace ThreeJsModelConfig {
  export enum Type {
    GLTF = "GLTF",
    NODES_MATERIAL = "NODES_MATERIAL"
  }
}

export interface ThreeJsModelPackConfig {
  getId(): number;

  getInternalName(): string;

  getThreeJsModelId(): number;

  toNamePathAsArray(): string[];

  getPosition(): Vertex;

  getScale(): Vertex;

  getRotation(): Vertex;
}

export interface MeshContainer {
  getId(): number;

  getInternalName(): string;

  toChildrenArray(): MeshContainer[] | null;

  getMesh(): Mesh | null;
}

export interface Mesh {
  getThreeJsModelId(): number | null;

  getElement3DId(): string;

  toShapeTransformsArray(): ShapeTransform[] | null;
}

export interface ShapeTransform {
  getTranslateX(): number;

  getTranslateY(): number;

  getTranslateZ(): number;

  getRotateX(): number;

  getRotateY(): number;

  getRotateZ(): number;

  getRotateW(): number;

  getScaleX(): number;

  getScaleY(): number;

  getScaleZ(): number;
}

// ---------- Renderer ----------
export interface ThreeJsRendererServiceAccess {
  createTerrainTile(terrainTile: TerrainTile, defaultGroundConfigId: number): ThreeJsTerrainTile;

  createSyncBaseItem(id: number, threeJsModelPackConfigId: number | null, meshContainerId: number | null, internalName: string, diplomacy: Diplomacy, radius: number): BabylonBaseItem;

  createProjectile(start: Vertex, destination: Vertex, duration: number): void;

  setViewFieldCenter(x: number, y: number): void;

  initMeshContainers(meshContainers: MeshContainer[]): void;
}

export interface TerrainTile {
  getGroundTerrainTiles(): GroundTerrainTile[];

  getTerrainSlopeTiles(): TerrainSlopeTile[];

  getTerrainWaterTiles(): TerrainWaterTile[];

  getTerrainTileObjectLists(): TerrainTileObjectList[];

  getIndex(): Index;
}

export enum Diplomacy {
  OWN = "OWN",
  FRIEND = "FRIEND",
  ENEMY = "ENEMY"
}

export interface GroundTerrainTile {
  groundConfigId: number;
  positions: Float32Array;
  norms: Float32Array
}

export interface TerrainSlopeTile {
  slopeConfigId: number;
  centerSlopeGeometry: SlopeGeometry | null;
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
  terrainObjectModels: TerrainObjectModel[];
  terrainObjectConfigId: number;
}

export interface TerrainObjectModel {
  terrainObjectId: number;
  position: Vertex;
  scale: Vertex | null;
  rotation: Vertex | null;
}

export interface ThreeJsTerrainTile {
  addToScene(): void;

  removeFromScene(): void;
}

export interface BabylonBaseItem {

  getId(): number;

  dispose(): void;

  getPosition(): Vertex | null;

  setPosition(position: Vertex): void;

  updatePosition(): void;

  getAngle(): number;

  setAngle(angle: number): void;

  updateAngle(): void;

  getHealth(): number;

  setHealth(health: number): void;

  updateHealth(): void;

  select(active: boolean): void;

  hover(active: boolean): void;
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

export interface BaseItemPlacerPresenter {
  activate(baseItemPlacer: BaseItemPlacer): void;

  deactivate(): void;
}

export interface BaseItemPlacer {
  isPositionValid(): boolean;

  getPosition(): DecimalPosition;

  getEnemyFreeRadius(): number;
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
}

export interface GenericEditorFrontendProvider {
  collectionNames(): string[];

  requestObjectNameIds(collectionName: string): Promise<ObjectNameId[]>;

  requestObjectNameId(collectionName: string, configId: number): Promise<ObjectNameId>;

  createConfig(collectionName: string): Promise<GwtAngularPropertyTable>;

  deleteConfig(collectionName: string, gwtAngularPropertyTable: GwtAngularPropertyTable): Promise<void>;

  getPathForCollection(collectionName: string): string;
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

  onCreate(gwtAngularPropertyTable: GwtAngularPropertyTable, createOption: any): void;

  onDelete(gwtAngularPropertyTable: GwtAngularPropertyTable): void;

  setValue(value: any): void;
}

export interface TerrainEditorService {
  save(createdTerrainObjects: TerrainObjectPosition[], updatedTerrainObjects: TerrainObjectPosition[]): Promise<string>;

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

  getTerrainObjectPositions(): Promise<TerrainObjectPosition[]>;
}

export interface TerrainObjectPosition {
  getId(): number;

  setId(id: number): void;

  getTerrainObjectConfigId(): number;

  setTerrainObjectConfigId(terrainObjectConfigId: number): void;

  getPosition(): DecimalPosition;

  setPosition(position: DecimalPosition): void;

  getScale(): Vertex;

  setScale(scale: Vertex): void;

  getRotation(): Vertex;

  setRotation(rotation: Vertex): void;

  getOffset(): Vertex;

  setOffset(offset: Vertex): void;
}

export interface ImageGalleryItem {
  id: number;
  size: number;
  type: string;
  internalName: string
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
