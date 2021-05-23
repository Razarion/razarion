import {Component} from "@angular/core";
import {GwtAngularService} from "../../gwtangular/GwtAngularService";
import {EditorPanel} from "../editor-model";
import {HttpClient} from "@angular/common/http";
import {SERVER_GAME_ENGINE_PATH} from "../../common";
import {MessageService} from "primeng/api";

@Component({
  selector: 'server-panel',
  templateUrl: 'server-panel.component.html'
})
export class ServerPanelComponent extends EditorPanel {
  serverCommands: ServerCommand[] = [
    new ServerCommand("Restart Bots", "restartBots"),
    new ServerCommand("Reload Static", "reloadStatic"),
    new ServerCommand("Restart Resource Regions", "restartResourceRegions"),
    new ServerCommand("Reload Planet Shapes", "reloadPlanetShapes"),
    new ServerCommand("Restart Box Regions", "restartBoxRegions")
  ]

  constructor(private gwtAngularService: GwtAngularService,
              private http: HttpClient,
              private messageService: MessageService) {
    super();
  }

  execute(serverCommand: ServerCommand) {
    const url = SERVER_GAME_ENGINE_PATH + "/" + serverCommand.methodUrl
    console.info("URL: " + url);
    this.http.post(url, null).subscribe(value => {
      console.log("next: " + value);
    }, error => {
      this.messageService.add({
        severity: 'error',
        summary: `Can not invoke ${serverCommand.name} on Server. URL: ${url}`,
        detail: error,
        sticky: true
      });
    }, () => {
      this.messageService.add({
        severity: 'success',
        summary: `${serverCommand.name}`
      });
    });
  }
}

export class ServerCommand {
  constructor(public name: string, public methodUrl: string) {
  }
}
