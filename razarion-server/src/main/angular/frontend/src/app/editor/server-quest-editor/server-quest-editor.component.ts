import {Component, OnInit} from '@angular/core';
import {EditorPanel} from "../editor-model";
import {EditorService} from "../editor-service";
import {ConditionTrigger, ServerGameEngineConfig} from "../../generated/razarion-share";

@Component({
  selector: 'server-quest-editor',
  templateUrl: './server-quest-editor.component.html'
})
export class ServerQuestEditorComponent extends EditorPanel implements OnInit {
  serverGameEngineConfig?: ServerGameEngineConfig;
  protected readonly CONDITION_TRIGGERS =
    [ConditionTrigger.SYNC_ITEM_KILLED,
      ConditionTrigger.HARVEST,
      ConditionTrigger.SYNC_ITEM_CREATED,
      ConditionTrigger.BASE_KILLED,
      ConditionTrigger.SYNC_ITEM_POSITION,
      ConditionTrigger.BOX_PICKED,
      ConditionTrigger.INVENTORY_ITEM_PLACED,
    ];

  constructor(private editorService: EditorService) {
    super();
  }

  ngOnInit(): void {
    this.editorService.readServerGameEngineConfig().then(serverGameEngineConfig => {
      this.serverGameEngineConfig = serverGameEngineConfig;
    })
  }

  onSave() {
    this.editorService.updateServerLevelQuestConfig(this.serverGameEngineConfig?.serverLevelQuestConfigs)
  }
}
