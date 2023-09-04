import {ComponentFactoryResolver, Injectable, Type} from "@angular/core";
import {
  BASE_ITEM_TYPE_EDITOR_PATH,
  LEVEL_EDITOR_PATH,
  RESOURCE_ITEM_TYPE_EDITOR_PATH,
  SERVER_GAME_ENGINE_EDITOR,
  SERVER_GAME_ENGINE_PATH
} from "../common";
import {GwtAngularService} from "../gwtangular/GwtAngularService";
import {HttpClient} from "@angular/common/http";
import {MessageService} from "primeng/api";
import {
  BotConfig,
  ObjectNameId,
  ResourceRegionConfig,
  ServerGameEngineConfig,
  ServerLevelQuestConfig,
  StartRegionConfig
} from "../generated/razarion-share";

export class ServerCommand {
  constructor(public name: string, public methodUrl: string) {
  }
}

@Injectable()
export class EditorService {
  static readonly SERVER_GAME_ENGINE_ID = 3; // TODO read from game engine
  static RESTART_BOTS: ServerCommand = new ServerCommand("Restart Bots", "restartBots");
  static RELOAD_STATIC: ServerCommand = new ServerCommand("Reload Static", "reloadStatic");
  static RESTART_RESOURCE_REGIONS: ServerCommand = new ServerCommand("Restart Resource Regions", "restartResourceRegions");
  static RELOAD_PLANET_SHAPES: ServerCommand = new ServerCommand("Reload Planet Shapes", "reloadPlanetShapes");
  static RESTART_BOX_REGIONS: ServerCommand = new ServerCommand("Restart Box Regions", "restartBoxRegions");
  static RESTART_PLANET_WARM: ServerCommand = new ServerCommand("Restart Planet warm", "restartPlanetWarm");
  static RESTART_PLANET_COLD: ServerCommand = new ServerCommand("Restart Planet cold", "restartPlanetCold");

  public static ALL_SERVER_COMMANDS: ServerCommand[] = [
    EditorService.RESTART_BOTS,
    EditorService.RELOAD_STATIC,
    EditorService.RESTART_RESOURCE_REGIONS,
    EditorService.RELOAD_PLANET_SHAPES,
    EditorService.RESTART_BOX_REGIONS,
    EditorService.RESTART_PLANET_WARM,
    EditorService.RESTART_PLANET_COLD,
  ]
  private propertyEditorComponents = new Map();

  constructor(private componentFactoryResolver: ComponentFactoryResolver,
              private gwtAngularService: GwtAngularService,
              private httpClient: HttpClient,
              private messageService: MessageService) {
  }

  getPropertyEditorComponent<T>(selector: string): Type<T> {
    let propertyEditorComponent = this.propertyEditorComponents.get(selector);
    if (propertyEditorComponent == null) {
      throw new TypeError(`No PropertyEditorComponent for ${selector}`);
    }
    return propertyEditorComponent;
  }

  registerPropertyEditorComponents<T>(propertyEditorComponents: Type<T>[]): void {
    propertyEditorComponents.forEach(propertyEditorComponent =>
      this.registerPropertyEditorComponent(propertyEditorComponent)
    )
  }

  registerPropertyEditorComponent<T>(propertyEditorComponent: Type<T>): void {
    let factory = this.componentFactoryResolver.resolveComponentFactory(propertyEditorComponent);
    this.propertyEditorComponents.set(factory.selector, propertyEditorComponent);
  }

  executeServerCommand(serverCommand: ServerCommand) {
    const url = SERVER_GAME_ENGINE_PATH + "/" + serverCommand.methodUrl
    this.httpClient.post(url, null).subscribe(value => {
        this.messageService.add({
          severity: 'success',
          life: 300,
          summary: serverCommand.name
        });
      },
      error => {
        this.messageService.add({
          severity: 'error',
          summary: `Can not invoke ${serverCommand.name} on Server. URL: ${url}`,
          detail: error,
          sticky: true
        });
      });
  }

  readServerGameEngineConfig(): Promise<ServerGameEngineConfig> {
    return new Promise((resolve) => {
      this.httpClient.get(`${SERVER_GAME_ENGINE_EDITOR}/read/${EditorService.SERVER_GAME_ENGINE_ID}`).subscribe({
        next: (serverGameEngineConfig: any) => {
          resolve(serverGameEngineConfig);
        },
        error: (err: any) => {
          this.messageService.add({
            severity: 'error',
            summary: `Loading SERVER_GAME_ENGINE failed`,
            detail: `${JSON.stringify(err)}`,
            sticky: true
          });
        }
      });
    });
  }

  updateResourceRegionConfig(resourceRegionConfigs: ResourceRegionConfig[] | undefined) {
    this.updateChildConfig(resourceRegionConfigs, "resourceRegionConfig");
  }

  updateStartRegionConfig(startRegionConfigs: StartRegionConfig[] | undefined) {
    this.updateChildConfig(startRegionConfigs, "startRegionConfig");
  }

  updateBotConfig(botConfigs: BotConfig[] | undefined) {
    this.updateChildConfig(botConfigs, "botConfig");
  }

  updateServerLevelQuestConfig(serverLevelQuestConfig: ServerLevelQuestConfig[] | undefined) {
    this.updateChildConfig(serverLevelQuestConfig, "serverLevelQuestConfig");
  }

  private updateChildConfig(configs: any[] | undefined, configName: string) {
    let url = `${SERVER_GAME_ENGINE_EDITOR}/update/${configName}/${EditorService.SERVER_GAME_ENGINE_ID}`;
    this.httpClient.post(url, configs).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          life: 300,
          summary: 'Saved'
        });
      },
      error: (err: any) => {
        this.messageService.add({
          severity: 'error',
          summary: `Failed saving ${url}`,
          detail: `${JSON.stringify(err)}`,
          sticky: true
        });
      }
    });
  }

  readBaseItemTypeObjectNameIds(): Promise<ObjectNameId[]> {
    return new Promise((resolve) => {
      this.httpClient.get(`${BASE_ITEM_TYPE_EDITOR_PATH}/objectNameIds`).subscribe({
        next: (objectNameIds: any) => {
          resolve(objectNameIds);
        },
        error: (err: any) => {
          this.messageService.add({
            severity: 'error',
            summary: `Loading base item types failed`,
            detail: `${JSON.stringify(err)}`,
            sticky: true
          });
        }
      });
    });
  }
  readResourceItemTypeObjectNameIds(): Promise<ObjectNameId[]> {
    return new Promise((resolve) => {
      this.httpClient.get(`${RESOURCE_ITEM_TYPE_EDITOR_PATH}/objectNameIds`).subscribe({
        next: (objectNameIds: any) => {
          resolve(objectNameIds);
        },
        error: (err: any) => {
          this.messageService.add({
            severity: 'error',
            summary: `Loading resource item types failed`,
            detail: `${JSON.stringify(err)}`,
            sticky: true
          });
        }
      });
    });
  }

  readLevelObjectNameIds(): Promise<ObjectNameId[]> {
    return new Promise((resolve) => {
      this.httpClient.get(`${LEVEL_EDITOR_PATH}/objectNameIds`).subscribe({
        next: (objectNameIds: any) => {
          resolve(objectNameIds);
        },
        error: (err: any) => {
          this.messageService.add({
            severity: 'error',
            summary: `Loading level failed`,
            detail: `${JSON.stringify(err)}`,
            sticky: true
          });
        }
      });
    });
  }
}
