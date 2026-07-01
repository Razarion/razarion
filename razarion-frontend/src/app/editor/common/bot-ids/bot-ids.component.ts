import { Component, EventEmitter, Input, Output } from '@angular/core';
import { EditorService } from '../../editor-service';
import { MultiSelect } from 'primeng/multiselect';
import { FormsModule } from '@angular/forms';


@Component({
  selector: 'bot-ids',
  imports: [
    MultiSelect,
    FormsModule
  ],
  templateUrl: './bot-ids.component.html'
})
export class BotIdsComponent {
  @Input("botIds")
  botIds: number[] = [];
  @Output()
  botIdsChange = new EventEmitter<number[]>();
  @Input("readOnly")
  readOnly: boolean = false;
  botOptions: { label: string, botId: number }[] = [];

  constructor(editorService: EditorService) {
    editorService.readBotObjectNameIds().then(objectNameIds => {
      this.botOptions = [];
      objectNameIds.forEach(objectNameId => {
        this.botOptions.push({ label: `${objectNameId.internalName} '${objectNameId.id}'`, botId: objectNameId.id });
      });
    });
  }

  onChange() {
    this.botIdsChange.emit(this.botIds);
  }

  getCurrentNames(): string {
    if (this.botIds && this.botIds.length > 0) {
      return this.botIds
        .map(botId => this.botOptions.find(value => value.botId === botId)?.label || `'${botId}'`)
        .join(", ");
    } else {
      return "";
    }
  }

}
