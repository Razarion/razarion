import {TreeNode} from "primeng/api";

// ---------- Common ----------
export class GwtAngularFacade {
  canvasElement!: HTMLCanvasElement;
  itemCockpitFrontend!: ItemCockpitFrontend;
  editorFrontendProvider!: EditorFrontendProvider;
}

// ---------- Item Cockpit ----------
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
  sellButton: boolean;
  buildupItemInfos: BuildupItemCockpit[] | null;
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
}

export interface GenericEditorFrontendProvider {
  crudControllers(): string[];

  requestObjectNameIds(crudControllerIndex: number): Promise<ObjectNameId[]>;

  createConfig(crudControllerIndex: number): Promise<GwtAngularPropertyTable>;

  readConfig(crudControllerIndex: number, configId: number): Promise<GwtAngularPropertyTable>;

  updateConfig(crudControllerIndex: number, gwtAngularPropertyTable: GwtAngularPropertyTable): Promise<void>;

  deleteConfig(crudControllerIndex: number, gwtAngularPropertyTable: GwtAngularPropertyTable): Promise<void>;
}

export interface GwtAngularPropertyTable {
  rootTreeNodes: TreeNode[];
}

export interface ObjectNameId {
  getId(): number;

  getInternalName(): string;
}

export interface AngularTreeNodeData {
  name: string;
  value: any;
  propertyEditorSelector: string;
  nullable: boolean;
  deleteAllowed: boolean;
  createAllowed: boolean;

  onCreate(gwtAngularPropertyTable: GwtAngularPropertyTable): void;

  onDelete(gwtAngularPropertyTable: GwtAngularPropertyTable): void;

  setValue(value: any): void;
}
