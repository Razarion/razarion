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

  requestConfigs(crudControllerIndex: number): Promise<ObjectNameId[]>;

  readConfig(crudControllerIndex: number, configId: number): Promise<GwtAngularPropertyTable>;
}

export interface GwtAngularPropertyTable {
  rootTreeNodes: TreeNode[];
}

export interface ObjectNameId {
  getId(): number;

  getInternalName(): string;
}
