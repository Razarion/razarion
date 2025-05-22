import { Component, EventEmitter, Input, Output } from '@angular/core';
import { EditorService } from '../../editor-service';
import {SelectModule} from 'primeng/select';
import {FormsModule} from '@angular/forms';
import {NgIf} from '@angular/common';

@Component({
  selector: 'level',
  imports: [
    SelectModule,
    FormsModule,
    NgIf
  ],
  templateUrl: './level.component.html'
})
export class LevelComponent {
  @Input("levelId")
  levelId: number | null = null;
  @Output()
  levelIdChange = new EventEmitter<number | null>();
  @Input("readOnly")
  readOnly: boolean = false;
  levelOptions: { label: string, levelId: number }[] = [];

  constructor(editorService: EditorService) {
    editorService.readLevelObjectNameIds().then(objectNameIds => {
      this.levelOptions = [];
      objectNameIds.forEach(objectNameId => {
        this.levelOptions.push({ label: `${objectNameId.internalName} '${objectNameId.id}'`, levelId: objectNameId.id });
      });
    });
  }

  onChange() {
    this.levelIdChange.emit(this.levelId);
  }

  getCurrentName(): string {
    if (this.levelId) {
      return this.levelOptions.find(value => value.levelId === this.levelId)?.label || "";
    } else {
      return "";
    }
  }

}
