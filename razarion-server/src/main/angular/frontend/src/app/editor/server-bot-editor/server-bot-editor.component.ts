import {Component, OnInit} from '@angular/core';
import {EditorPanel} from "../editor-model";
import {EditorService} from "../editor-service";
import {BotConfig, ServerGameEngineConfig} from "../../generated/razarion-share";

@Component({
  selector: 'server-bot-editor',
  templateUrl: './server-bot-editor.component.html',
  styleUrls: ['./server-bot-editor.component.scss']
})
export class ServerBotEditorComponent extends EditorPanel implements OnInit {
  serverGameEngineConfig?: ServerGameEngineConfig;
  selectedBot?: BotConfig;

  constructor(private editorService: EditorService) {
    super();
  }

  ngOnInit(): void {
    this.editorService.readServerGameEngineConfig().then(serverGameEngineConfig => {
      this.serverGameEngineConfig = serverGameEngineConfig;
    })
  }

  onSave() {

  }
}
