import {Injectable} from "@angular/core";
import {
  BASE_ITEM_TYPE_EDITOR_PATH,
  GROUND_EDITOR_PATH,
  PLANET_EDITOR_PATH,
  RESOURCE_ITEM_TYPE_EDITOR_PATH,
  WATER_EDITOR_PATH
} from "../common";
import {GwtAngularService} from "../gwtangular/GwtAngularService";
import {HttpClient} from "@angular/common/http";
import {MessageService} from "primeng/api";
import {
  BotConfig,
  BoxRegionConfig,
  GroundConfig,
  LevelEditorControllerClient,
  ObjectNameId,
  PlanetConfig,
  ResourceRegionConfig,
  ServerGameEngineConfig,
  ServerGameEngineControllerClient,
  ServerGameEngineEditorControllerClient,
  ServerLevelQuestConfig,
  StartRegionConfig
} from "../generated/razarion-share";
import {TypescriptGenerator} from "../backend/typescript-generator";

export class ServerCommand {
  constructor(public name: string, public restcall: (client: ServerGameEngineControllerClient) => (Promise<void>)) {
  }
}

@Injectable({
  providedIn: 'root',
})
export class EditorService {
  static readonly SERVER_GAME_ENGINE_ID = 3; // TODO read from game engine
  static RESTART_BOTS: ServerCommand = new ServerCommand("Restart Bots", (client) => client.restartBots());
  static RELOAD_STATIC: ServerCommand = new ServerCommand("Reload Static", (client) => client.reloadStatic());
  static RESTART_RESOURCE_REGIONS: ServerCommand = new ServerCommand("Restart Resource Regions", (client) => client.restartResourceRegions());
  static RELOAD_PLANET_SHAPES: ServerCommand = new ServerCommand("Reload Planet Shapes", (client) => client.reloadPlanetShapes());
  static RESTART_BOX_REGIONS: ServerCommand = new ServerCommand("Restart Box Regions", (client) => client.restartBoxRegions());
  static RESTART_PLANET_WARM: ServerCommand = new ServerCommand("Restart Planet warm", (client) => client.restartPlanetWarm());
  static RESTART_PLANET_COLD: ServerCommand = new ServerCommand("Restart Planet cold", (client) => client.restartPlanetCold());
  private serverGameEngineEditorControllerClient: ServerGameEngineEditorControllerClient;
  private levelEditorControllerClient: LevelEditorControllerClient;
  private serverGameEngineControllerClient: ServerGameEngineControllerClient;

  public static ALL_SERVER_COMMANDS: ServerCommand[] = [
    EditorService.RESTART_BOTS,
    EditorService.RELOAD_STATIC,
    EditorService.RESTART_RESOURCE_REGIONS,
    EditorService.RELOAD_PLANET_SHAPES,
    EditorService.RESTART_BOX_REGIONS,
    EditorService.RESTART_PLANET_WARM,
    EditorService.RESTART_PLANET_COLD,
  ]

  constructor(private gwtAngularService: GwtAngularService,
              private httpClient: HttpClient,
              private messageService: MessageService) {
    this.serverGameEngineEditorControllerClient = new ServerGameEngineEditorControllerClient(TypescriptGenerator.generateHttpClientAdapter(this.httpClient));
    this.levelEditorControllerClient = new LevelEditorControllerClient(TypescriptGenerator.generateHttpClientAdapter(httpClient))
    this.serverGameEngineControllerClient = new ServerGameEngineControllerClient(TypescriptGenerator.generateHttpClientAdapter(httpClient))
  }

  executeServerCommand(serverCommand: ServerCommand) {
    serverCommand.restcall(this.serverGameEngineControllerClient).then(() => {
      this.messageService.add({
        severity: 'success',
        life: 300,
        summary: serverCommand.name
      });
    }).catch(err => {
      this.messageService.add({
        severity: 'error',
        summary: `Can not invoke ${serverCommand.name}`,
        detail: err,
        sticky: true
      });
    });
  }

  readServerGameEngineConfig(): Promise<ServerGameEngineConfig> {
    return new Promise((resolve) => {
      this.serverGameEngineEditorControllerClient.read(EditorService.SERVER_GAME_ENGINE_ID).then(serverGameEngineConfig => {
        resolve(serverGameEngineConfig);
      }).catch(err => {
        this.messageService.add({
          severity: 'error',
          summary: `Loading SERVER_GAME_ENGINE failed`,
          detail: err.message,
          sticky: true
        });
      });
    });
  }

  updateResourceRegionConfig(resourceRegionConfigs: ResourceRegionConfig[]) {
    return new Promise((resolve) => {
      this.serverGameEngineEditorControllerClient.updateResourceRegionConfig(EditorService.SERVER_GAME_ENGINE_ID, resourceRegionConfigs).then(serverGameEngineConfig => {
        resolve(serverGameEngineConfig);
      }).catch(err => {
        this.messageService.add({
          severity: 'error',
          summary: `Failed updateResourceRegionConfig`,
          detail: err.message,
          sticky: true
        });
      });
    });
  }

  updateStartRegionConfig(startRegionConfigs: StartRegionConfig[]) {
    return new Promise((resolve) => {
      this.serverGameEngineEditorControllerClient.updateStartRegionConfig(EditorService.SERVER_GAME_ENGINE_ID, startRegionConfigs).then(serverGameEngineConfig => {
        resolve(serverGameEngineConfig);
      }).catch(err => {
        this.messageService.add({
          severity: 'error',
          summary: `Failed updateStartRegionConfig`,
          detail: err.message,
          sticky: true
        });
      });
    });
  }

  updateBotConfig(botConfigs: BotConfig[]) {
    return new Promise((resolve) => {
      this.serverGameEngineEditorControllerClient.updateBotConfig(EditorService.SERVER_GAME_ENGINE_ID, botConfigs).then(serverGameEngineConfig => {
        resolve(serverGameEngineConfig);
      }).catch(err => {
        this.messageService.add({
          severity: 'error',
          summary: `Failed updateBotConfig`,
          detail: err.message,
          sticky: true
        });
      });
    });
  }

  updateServerLevelQuestConfig(serverLevelQuestConfigs: ServerLevelQuestConfig[]): Promise<void> {
    return new Promise((resolve) => {
      this.serverGameEngineEditorControllerClient.updateServerLevelQuestConfig(EditorService.SERVER_GAME_ENGINE_ID, serverLevelQuestConfigs).then(() => {
        resolve();
      }).catch(err => {
        this.messageService.add({
          severity: 'error',
          summary: `Failed updateServerLevelQuestConfig`,
          detail: err.message,
          sticky: true
        });
      });
    });
  }

  updateBoxRegionConfig(boxRegionConfigs: BoxRegionConfig[]) {
    return new Promise((resolve) => {
      this.serverGameEngineEditorControllerClient.updateBoxRegionConfig(EditorService.SERVER_GAME_ENGINE_ID, boxRegionConfigs).then(serverGameEngineConfig => {
        resolve(serverGameEngineConfig);
      }).catch(err => {
        this.messageService.add({
          severity: 'error',
          summary: `Failed updateBoxRegionConfig`,
          detail: err.message,
          sticky: true
        });
      });
    });
  }

  readBaseItemTypeObjectNameIds(): Promise<ObjectNameId[]> {
    return this.readObjectNameIds(BASE_ITEM_TYPE_EDITOR_PATH);
  }

  readResourceItemTypeObjectNameIds(): Promise<ObjectNameId[]> {
    return this.readObjectNameIds(RESOURCE_ITEM_TYPE_EDITOR_PATH);
  }

  readLevelObjectNameIds(): Promise<ObjectNameId[]> {
    return new Promise((resolve) => {
      this.levelEditorControllerClient.getObjectNameIds().then(objectNameIds => {
        resolve(objectNameIds);
      }).catch(err => {
        this.messageService.add({
          severity: 'error',
          summary: `Failed levelEditorControllerClient getObjectNameIds`,
          detail: `${JSON.stringify(err)}`,
          sticky: true
        });
      });
    });
  }

  readGroundObjectNameIds(): Promise<ObjectNameId[]> {
    return this.readObjectNameIds(GROUND_EDITOR_PATH);
  }

  readWaterObjectNameIds(): Promise<ObjectNameId[]> {
    return this.readObjectNameIds(WATER_EDITOR_PATH);
  }

  readPlanetConfig(id: number): Promise<PlanetConfig> {
    return this.readConfig(PLANET_EDITOR_PATH, id);
  }

  readGroundConfig(id: number): Promise<GroundConfig> {
    return this.readConfig(GROUND_EDITOR_PATH, id);
  }

  readObjectNameIds(editorUrl: string): Promise<ObjectNameId[]> {
    return new Promise((resolve) => {
      this.httpClient.get(`${editorUrl}/objectNameIds`).subscribe({
        next: (objectNameIds: any) => {
          resolve(objectNameIds);
        },
        error: (err: any) => {
          this.messageService.add({
            severity: 'error',
            summary: `Loading objectNameId ${editorUrl}`,
            detail: err.message,
            sticky: true
          });
        }
      });
    });
  }

  readConfig(editorUrl: string, id: number): Promise<any> {
    return new Promise((resolve) => {
      this.httpClient.get(`${editorUrl}/read/${id}`).subscribe({
        next: (config: any) => {
          resolve(config);
        },
        error: (err: any) => {
          this.messageService.add({
            severity: 'error',
            summary: `Reading id: ${id} ${editorUrl}`,
            detail: err.message,
            sticky: true
          });
        }
      });
    });
  }

  getPlanetId(): number {
    return this.gwtAngularService.gwtAngularFacade.gameUiControl.getPlanetConfig().getId();
  }

}
