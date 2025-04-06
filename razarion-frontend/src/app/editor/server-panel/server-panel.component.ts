import {Component} from "@angular/core";
import {EditorPanel} from "../editor-model";
import {EditorService, ServerCommand} from "../editor-service";
import {Button} from 'primeng/button';
import {NgForOf} from '@angular/common';

@Component({
  selector: 'server-panel',
  imports: [
    Button,
    NgForOf
  ],
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

