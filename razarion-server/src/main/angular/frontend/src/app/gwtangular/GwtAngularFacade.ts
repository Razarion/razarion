import {TreeNode} from "primeng/api";

// ---------- Common ----------
export abstract class GwtAngularFacade {
  canvasElement!: HTMLCanvasElement;
  mainCockpit!: MainCockpit;
  itemCockpitFrontend!: ItemCockpitFrontend;
  editorFrontendProvider!: EditorFrontendProvider;
  canvasResizeCallback!: Callback;
  statusProvider!: StatusProvider;

  abstract onCrash(): void;
}

export interface StatusProvider {
  getClientAlarms(): Alarm[];

  requestServerAlarms(): Promise<Alarm[]>;

  setStats(stats: Stats | null): void;

  getStats(): Stats;
}

export interface Callback {
  onCallback(): void;
}

export interface Rectangle {

}

export interface Alarm {
  angularTypeString(): string;

  angularDateAsLong(): number;

  getText(): string;

  getId(): number;
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

  isRenderInterpolation(): boolean;

  setRenderInterpolation(value: boolean): void;

  getTerrainMarkerService(): TerrainMarkerService;

  getTerrainEditorService(): TerrainEditorService;
}

export interface GenericEditorFrontendProvider {
  crudControllers(): string[];

  requestObjectNameIds(crudControllerIndex: number): Promise<ObjectNameId[]>;

  createConfig(crudControllerIndex: number): Promise<GwtAngularPropertyTable>;

  readConfig(crudControllerIndex: number, configId: number): Promise<GwtAngularPropertyTable>;

  updateConfig(crudControllerIndex: number, gwtAngularPropertyTable: GwtAngularPropertyTable): Promise<void>;

  deleteConfig(crudControllerIndex: number, gwtAngularPropertyTable: GwtAngularPropertyTable): Promise<void>;
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
  getId(): number;

  getInternalName(): string;

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
