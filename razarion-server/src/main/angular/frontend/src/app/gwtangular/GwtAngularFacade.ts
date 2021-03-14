export class GwtAngularFacade {
  canvasElement!: HTMLCanvasElement;
  editorFrontendProvider!: EditorFrontendProvider;
}

export interface EditorFrontendProvider {
  crudControllers(): string[];

  requestConfigs(crudControllerIndex: number): Promise<ObjectNameId[]>;
}

export interface ObjectNameId {
  getId(): string;

  getInternalName(): string;
}
