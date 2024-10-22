// ---------- Root ----------

import {ConditionTrigger} from "../generated/razarion-share";

export abstract class GwtAngularFacade {
  gwtAngularBoot!: GwtAngularBoot;
  gameUiControl!: GameUiControl;
  screenCover!: ScreenCover;
  actionServiceListener!: ActionServiceListener;
  mainCockpit!: MainCockpit;
  itemCockpitFrontend!: ItemCockpitFrontend;
  questCockpit!: QuestCockpit;
  inGameQuestVisualizationService!: InGameQuestVisualizationService;
  baseItemPlacerPresenter!: BaseItemPlacerPresenter;
  statusProvider!: StatusProvider;
  babylonRenderServiceAccess!: BabylonRenderServiceAccess;
  inputService!: InputService;
  selectionService!: SelectionService;
  terrainTypeService!: TerrainTypeService;
  itemTypeService!: ItemTypeService;
  threeJsModelPackService!: ThreeJsModelPackService;
  baseItemUiService!: BaseItemUiService
  modelDialogPresenter!: ModelDialogPresenter;
  inventoryTypeService!: InventoryTypeService;
  inventoryUiService!: InventoryUiService;
  terrainUiService!: TerrainUiService;

  abstract onCrash(): void;
}

// ---------- Boot ----------

export interface GwtAngularBoot {
  loadThreeJsModels(threeJsModelConfigs: ThreeJsModelConfig[], particleSystemConfigs: ParticleSystemConfig[], babylonMaterialIds: number[]): Promise<void>;
}

// ---------- Common ----------

export interface GameUiControl {
  getPlanetConfig(): PlanetConfig;
}


export interface Index {
  getX(): number;

  getY(): number;

  add(deltaX: number, deltaY: number): Index;

  toString(): string;
}

export interface DecimalPosition {
  getX(): number;

  getY(): number;

  add(x: number, y: any): DecimalPosition;

  divide(x: number, y: number): DecimalPosition;

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

export interface Rectangle {

}

export interface I18nString {
  getString(): string;
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

  ownItemClicked(id: number, baseItemType: BaseItemType): void;

  friendItemClicked(id: number): void;

  enemyItemClicked(id: number): void;

  resourceItemClicked(id: number): void;

  boxItemClicked(id: number): void;

  terrainClicked(terrainPosition: DecimalPosition): void;

  getTerrainTypeOnTerrain(nodeIndex: Index): Promise<any>;
}

export interface SelectionService {
  hasOwnSelection(): boolean;

  hasOwnMovable(): boolean;

  hasAttackers(): boolean;

  canAttack(targetItemTypeId: number): boolean;

  hasHarvesters(): boolean;

  selectRectangle(xStart: number, yStart: number, width: number, height: number): void;

  setSelectionListener(callback: () => void): void;
}

export interface ItemTypeService {
  getResourceItemTypeAngular(resourceItemTypeId: number): ResourceItemType;

  getBaseItemTypeAngular(baseItemTypeId: number): BaseItemType;
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

// ---------- SyncBaseItems ----------

export interface BaseItemUiService {
  getVisibleNativeSyncBaseItemTickInfos(bottomLeft: DecimalPosition, topRight: DecimalPosition): NativeSyncBaseItemTickInfo[];

  diplomacy4SyncBaseItem(nativeSyncBaseItemTickInfo: NativeSyncBaseItemTickInfo): Diplomacy;

  getBases(): PlayerBaseDto[];
}

export interface NativeSyncBaseItemTickInfo {
  x: number;
  y: number;
}

export interface PlayerBaseDto {
  getBaseId(): number;

  getName(): string;

  getCharacter(): Character;
}

export enum Character {
  HUMAN = "HUMAN",
  BOT = "BOT",
  BOT_NCP = "BOT_NCP"
}

// ---------- Configs ----------

export interface PlanetConfig {
  getId(): number;

  getSize(): DecimalPosition;
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

  isDisabled(): boolean;
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

  getExplosionParticleId(): number | null;
}

export interface ResourceItemType extends ItemType {
  getRadius(): number;
}

export interface BoxItemType extends ItemType {
  getRadius(): number;
}

export interface PhysicalAreaConfig {
  getRadius(): number;

  fulfilledMovable(): boolean;
}

export interface BuilderType {
  getParticleSystemConfigId(): number | null;
}

export interface WeaponType {
  getMuzzleFlashParticleSystemConfigId(): number | null;

  getProjectileSpeed(): number | null;

  getTrailParticleSystemConfigId(): number | null;
}

export interface HarvesterType {
  getParticleSystemConfigId(): number | null;
}

// ---------- Quest ----------

export interface QuestDescriptionConfig {
  getId(): number;

  getInternalName(): string;

  getTitle(): string;

  getDescription(): string | null;
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

export interface ComparisonConfig {
  getCount(): number | null;

  toTypeCountAngular(): number[][]; // Key Item Type, Value count

  getTimeSeconds(): number | null;
}

// ---------- Renderer ----------
export interface BabylonRenderServiceAccess {
  createTerrainTile(terrainTile: TerrainTile): BabylonTerrainTile;

  createBabylonBaseItem(id: number, baseItemType: BaseItemType, diplomacy: Diplomacy): BabylonBaseItem;

  createBabylonResourceItem(id: number, resourceItemType: ResourceItemType): BabylonResourceItem;

  createBabylonBoxItem(id: number, boxItemType: BoxItemType): BabylonBoxItem;

  setViewFieldCenter(x: number, y: number): void;

  runRenderer(meshContainers: MeshContainer[]): void;

  showOutOfViewMarker(markerConfig: MarkerConfig | null, angle: number): void;

  showPlaceMarker(placeConfig: PlaceConfig | null, markerConfig: MarkerConfig | null): void;
}

export interface TerrainTile {
  getGroundHeightMap(): Uint16Array;

  getGroundConfigId(): number;

  getWaterConfigId(): number;

  getTerrainTileObjectLists(): TerrainTileObjectList[];

  getBabylonDecals(): BabylonDecal[];

  getIndex(): Index;
}

export interface BabylonDecal {
  babylonMaterialId: number;
  xPos: number;
  yPos: number;
  xSize: number;
  ySize: number;
}

export enum Diplomacy {
  OWN = "OWN",
  FRIEND = "FRIEND",
  ENEMY = "ENEMY",
  RESOURCE = "RESOURCE",
  BOX = "BOX",
}

export enum TerrainType {
  LAND = "LAND",
  WATER = "WATER",
  LAND_COAST = "LAND_COAST",
  WATER_COAST = "WATER_COAST",
  BLOCKED = "BLOCKED",
}

export interface MarkerConfig {
  radius: number;
  nodesMaterialId: number | null;
  placeNodesMaterialId: number | null;
  outOfViewNodesMaterialId: number | null;
  outOfViewSize: number;
  outOfViewDistanceFromCamera: number;
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

export interface Polygon2D {
  toCornersAngular(): DecimalPosition[];
}

export interface PlaceConfig {
  getPolygon2D(): Polygon2D | null;

  getPosition(): DecimalPosition | null;

  toRadiusAngular(): number;
}

export interface BabylonTerrainTile {
  addToScene(): void;

  removeFromScene(): void;
}

export interface BabylonItem {
  getId(): number;

  dispose(): void;

  getPosition(): DecimalPosition | null;

  setPosition(position: DecimalPosition): void;

  getAngle(): number;

  setAngle(angle: number): void;

  isEnemy(): boolean;

  select(active: boolean): void;

  hover(active: boolean): void;

  mark(markerConfig: MarkerConfig | null): void;
}

export interface BabylonBaseItem extends BabylonItem {
  getBaseItemType(): BaseItemType;

  setHealth(health: number): void;

  setBuildingPosition(buildingPosition: DecimalPosition | null): void;

  setHarvestingPosition(harvestingPosition: DecimalPosition | null): void;

  setBuildup(buildup: number): void;

  setConstructing(progress: number): void;

  onProjectileFired(destination: DecimalPosition): void;

  onExplode(): void;
}

export interface BabylonResourceItem extends BabylonItem {

}

export interface BabylonBoxItem extends BabylonItem {

}

export interface ParticleSystemConfig {
  getId(): number;

  getInternalName(): string;

  getThreeJsModelId(): number | null;

  getEmitterMeshPath(): string[];

  getPositionOffset(): Vertex | null;

  getImageId(): number | null;
}

// ---------- Item Cockpit ----------
export enum RadarState {
  NONE = "NONE",
  NO_POWER = "NO_POWER",
  WORKING = "WORKING",
}

export interface ActionServiceListener {
  onSelectionChanged(): void;
}

export interface MainCockpit {
  show(admin: boolean): void;

  hide(): void;

  displayResources(resources: number): void;

  displayXps(xp: number, xp2LevelUp: number): void;

  displayLevel(levelNumber: number): void;

  getInventoryDialogButtonLocation(): Rectangle;

  getScrollHomeButtonLocation(): Rectangle;

  displayItemCount(itemCount: number, usedHouseSpace: number, houseSpace: number): void;

  displayEnergy(consuming: number, generating: number): void;

  showRadar(radarState: RadarState): void;

  blinkAvailableUnlock(show: boolean): void

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
  showQuestSideBar(questDescriptionConfig: QuestDescriptionConfig | null, showQuestSelectionButton: boolean): void;

  setShowQuestInGameVisualisation(): void;

  onQuestProgress(questProgressInfo: QuestProgressInfo | null): void;
}

export interface InGameQuestVisualizationService {
  setVisible(visible: boolean): void;
}

export interface BaseItemPlacerPresenter {
  activate(baseItemPlacer: BaseItemPlacer): void;

  deactivate(): void;
}

export interface BaseItemPlacer {
  isPositionValid(): boolean;

  getEnemyFreeRadius(): number;

  onMove(xTerrainPosition: number, yTerrainPosition: number): void;

  onPlace(xTerrainPosition: number, yTerrainPosition: number): void;
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

// ---------- Inventory ----------
export interface BoxContent {
  toInventoryItemArray(): InventoryItem[];

  getCrystals(): number;
}

export interface InventoryItem {
  getI18nName(): I18nString;

  getRazarion(): number | null;

  getBaseItemTypeId(): number | null;

  getBaseItemTypeCount(): number;

  getImageId(): number | null;
}

export interface InventoryTypeService {
  getInventoryItem(id: number): InventoryItem
}

export interface InventoryUiService {
  useItem(inventoryItem: InventoryItem): void;
}

export interface TerrainUiService {

   getTerrainType(terrainPosition: DecimalPosition): TerrainType
}

// ---------- Dialog ----------

export interface ModelDialogPresenter {
  showLevelUp(): void;

  showQuestPassed(): void;

  showBaseLost(): void;

  showBoxPicked(boxContent: BoxContent): void;

  showUseInventoryItemLimitExceeded(baseItemType: BaseItemType): void;

  showUseInventoryHouseSpaceExceeded(): void;

  showRegisterDialog(): void;

  showSetUserNameDialog(): void;
}

export interface ObjectNameId {
  id: number;
  internalName: string;

  toString(): string;
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
