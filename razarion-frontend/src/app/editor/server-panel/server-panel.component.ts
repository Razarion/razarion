import {Component} from "@angular/core";
import {EditorPanel} from "../editor-model";
import {EditorService, ServerCommand} from "../editor-service";

@Component({
    selector: 'server-panel',
    templateUrl: 'server-panel.component.html'
})
export class ServerPanelComponent extends EditorPanel {

  constructor(private editorService: EditorService) {
    super();
  }

  execute(serverCommand: ServerCommand) {
    this.editorService.executeServerCommand(serverCommand);
  }

  protected readonly EditorService = EditorService;
}

