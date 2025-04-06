import {Component, EventEmitter, Input, Output} from '@angular/core';
import {EditorService} from "../../editor-service";
import {DropdownModule} from 'primeng/dropdown';
import {FormsModule} from '@angular/forms';
import {NgIf} from '@angular/common';

@Component({
  selector: 'ground',
  imports: [
    DropdownModule,
    FormsModule,
    NgIf
  ],
  templateUrl: './ground.component.html'
})
export class GroundComponent {
  @Input("groundId")
  groundId: number | null = null;
  @Output()
  groundIdChange = new EventEmitter<number | null>();
  @Input("readOnly")
  readOnly: boolean = false;
  groundOptions: { name: string, id: number }[] = [];

  constructor(private editorService: EditorService) {
    editorService.readGroundObjectNameIds().then(objectNameIds => {
      this.groundOptions = [];
      objectNameIds.forEach(objectNameId => {
        this.groundOptions.push({name: objectNameId.internalName, id: objectNameId.id});
      });
    })
  }

  onChange() {
    this.groundIdChange.emit(this.groundId);
  }

  getCurrentName(): string {
    if (this.groundId) {
      return this.groundOptions.find(value => value.id === this.groundId)?.name || "";
    } else {
      return "";
    }
  }

}
