import {TreeNode} from "primeng/api";

// ---------- Root ----------

export abstract class GwtAngularFacade {
  gwtAngularBoot!: GwtAngularBoot;
  gameUiControl!: GameUiControl;
  language!: string;
  screenCover!: ScreenCover;
  angularCursorService!: AngularCursorService;
  mainCockpit!: MainCockpit;
  itemCockpitFrontend!: ItemCockpitFrontend;
  questCockpit!: QuestCockpit;
  baseItemPlacerPresenter!: BaseItemPlacerPresenter;
  editorFrontendProvider!: EditorFrontendProvider;
  statusProvider!: StatusProvider;
  threeJsRendererServiceAccess!: BabylonRenderServiceAccess;
  inputService!: InputService;
  terrainTypeService!: TerrainTypeService;
  itemTypeService!: ItemTypeService;
  threeJsModelPackService!: ThreeJsModelPackService;
  assetService!: AssetService;

  abstract onCrash(): void;
}

// ---------- Boot ----------

export interface GwtAngularBoot {
  loadThreeJsModels(threeJsModelConfigs: ThreeJsModelConfig[], particleSystemConfigs: ParticleSystemConfig[]): Promise<void>;
}

// ---------- Common ----------

export interface GameUiControl {
  getPlanetConfig(): PlanetConfig;
}


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

export interface JsonDecimalPosition {
  x: number;
  y: number;
}

export interface NativeVertexDto {
  x: number;
  y: number;
  z: number;
}

export interface StatusProvider {
  getClientAlarms(): Alarm[];

  requestServerAlarms(): Promise<Alarm[]>;
}

export interface Callback {
  onCallback(): void;
}

export interface Rectangle {

}

export interface I18nString {
  getString(language: string): string;
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

export interface ScreenCover {
  showStoryCover(html: string): void;

  hideStoryCover(): void;

  removeLoadingCover(): void;

  fadeOutLoadingCover(): void;

  fadeInLoadingCover(): void;
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

export interface ItemTypeService {
  getResourceItemType(resourceItemTypeId: number): ResourceItemType;

  getBaseItemType(baseItemTypeId: number): BaseItemType;
}

export interface TerrainTypeService {
  getTerrainObjectConfig(id: number): TerrainObjectConfig;

  getSlopeConfig(id: number): SlopeConfig;

  getDrivewayConfig(drivewayConfigId: number): DrivewayConfig;

  getGroundConfig(groundConfigId: number): GroundConfig;

  getWaterConfig(waterConfigId: number): WaterConfig;

  calculateGroundHeight(slopeConfigId: number): number;
}

export interface ThreeJsModelPackService {
  getThreeJsModelPackConfig(id: number): ThreeJsModelPackConfig;
}

export interface AssetService {
  getMeshContainers(): MeshContainer[];
}

// ---------- Configs ----------

export interface PlanetConfig {
  getId(): number;
}

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
    NODES_MATERIAL = "NODES_MATERIAL",
    PARTICLE_SYSTEM_JSON = "PARTICLE_SYSTEM_JSON"
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

// ---------- ItemType ----------

export interface ItemType {
  getId(): number;

  getInternalName(): string;

  getI18nName(): I18nString;

  getThreeJsModelPackConfigId(): number | null;

  getMeshContainerId(): number | null;
}

export interface BaseItemType extends ItemType {
  getPhysicalAreaConfig(): PhysicalAreaConfig;

  getBuilderType(): BuilderType | null;

  getWeaponType(): WeaponType | null;

  getHarvesterType(): HarvesterType | null;
}

export interface ResourceItemType extends ItemType {
  getRadius(): number;
}

export interface PhysicalAreaConfig {
  getRadius(): number;
}

export interface BuilderType {
  getParticleSystemConfigId(): number | null;
}

export interface WeaponType {
  getMuzzleFlashParticleSystemConfigId(): number | null;

  getProjectileSpeed(): number | null;
}

export interface HarvesterType {
  getParticleSystemConfigId(): number | null;
}

// ---------- Quest ----------

export interface QuestDescriptionConfig {
  getId(): number;

  getInternalName(): string;

  getTitle(): string;

  getDescription(): string;
}

export interface QuestConfig extends QuestDescriptionConfig {
  getConditionConfig(): ConditionConfig | null
}

export interface QuestProgressInfo {
  getCount(): number | null;

  toTypeCountAngular(): number[][]; // Key Item Type, Value count

  getSecondsRemaining(): number | null;

  getBotBasesInformation(): string | null;

}

export interface ConditionConfig {
  getConditionTrigger(): ConditionTrigger;

  getComparisonConfig(): ComparisonConfig;
}

export enum ConditionTrigger {
  SYNC_ITEM_KILLED = "SYNC_ITEM_KILLED",
  HARVEST = "HARVEST",
  SYNC_ITEM_CREATED = "SYNC_ITEM_CREATED",
  BASE_KILLED = "BASE_KILLED",
  SYNC_ITEM_POSITION = "SYNC_ITEM_POSITION",
  BOX_PICKED = "BOX_PICKED",
  INVENTORY_ITEM_PLACED = "INVENTORY_ITEM_PLACED"
}

export interface ComparisonConfig {
  getCount(): number | null;

  toTypeCountAngular(): number[][]; // Key Item Type, Value count

  getTimeSeconds(): number | null;
}

// ---------- Renderer ----------
export interface BabylonRenderServiceAccess {
  createTerrainTile(terrainTile: TerrainTile, defaultGroundConfigId: number): BabylonTerrainTile;

  createBabylonBaseItem(id: number, baseItemType: BaseItemType, diplomacy: Diplomacy): BabylonBaseItem;

  createBabylonResourceItem(id: number, resourceItemType: ResourceItemType): BabylonResourceItem;

  setViewFieldCenter(x: number, y: number): void;

  runRenderer(meshContainers: MeshContainer[]): void;
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
  ENEMY = "ENEMY",
  RESOURCE = "RESOURCE"
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

export interface BabylonTerrainTile {
  addToScene(): void;

  removeFromScene(): void;
}

export interface BabylonItem {
  getId(): number;

  dispose(): void;

  getPosition(): Vertex | null;

  setPosition(position: Vertex): void;

  updatePosition(): void;

  getAngle(): number;

  setAngle(angle: number): void;

  updateAngle(): void;

  select(active: boolean): void;

  hover(active: boolean): void;
}

export interface BabylonBaseItem extends BabylonItem {
  getBaseItemType(): BaseItemType;

  setHealth(health: number): void;

  setBuildingPosition(buildingPosition: NativeVertexDto | null): void;

  setHarvestingPosition(harvestingPosition: NativeVertexDto | null): void;

  setBuildup(buildup: number): void;

  setConstructing(progress: number): void;

  onProjectileFired(destination: Vertex): void;

  onExplode(): void;
}

export interface BabylonResourceItem extends BabylonItem {

}

export interface ParticleSystemConfig {
  getId(): number;

  getInternalName(): string;

  getThreeJsModelId(): number;

  getEmitterMeshPath(): string[];
}

// ---------- Item Cockpit ----------
export enum RadarState {
  NONE,
  NO_POWER,
  WORKING
}

export enum CursorType {
  GO = "GO",
  ATTACK = "ATTACK",
  COLLECT = "COLLECT",
  LOAD = "LOAD",
  UNLOAD = "UNLOAD",
  FINALIZE_BUILD = "FINALIZE_BUILD",
  PICKUP = "PICKUP"
}

export interface AngularCursorService {
  setDefaultCursor(): void;

  setPointerCursor(): void;

  setCursor(cursorType: CursorType, allowed: boolean): void;
}

export interface MainCockpit {
  show(admin: boolean): void;

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

export interface QuestCockpit {
  showQuestSideBar(questDescriptionConfig: QuestDescriptionConfig | null, questProgressInfo: QuestProgressInfo | null, showQuestSelectionButton: boolean): void;

  setShowQuestInGameVisualisation(): void;

  onQuestProgress(questProgressInfo: QuestProgressInfo | null): void;

  setBotSceneIndicationInfos(): void;
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
}


export interface TerrainSlopePosition {
  id: number | null;
  slopeConfigId: number;
  inverted: boolean;
  polygon: TerrainSlopeCorner[];
  children: TerrainSlopePosition[];
  editorParentIdIfCreated: number | null; // Only on Angular side
}

export interface TerrainSlopeCorner {
  position: JsonDecimalPosition;
  slopeDrivewayId: number | null;
}

export interface SlopeTerrainEditorUpdate {
  createdSlopes: TerrainSlopePosition[];
  updatedSlopes: TerrainSlopePosition[];
  deletedSlopeIds: number[];
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