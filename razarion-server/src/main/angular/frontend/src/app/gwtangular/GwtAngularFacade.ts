import {TreeNode} from "primeng/api";

export class GwtAngularFacade {
  canvasElement!: HTMLCanvasElement;
  editorFrontendProvider!: EditorFrontendProvider;
}

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
