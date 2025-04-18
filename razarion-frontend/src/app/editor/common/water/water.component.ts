import {Component, EventEmitter, Input, Output} from '@angular/core';
import {EditorService} from "../../editor-service";

@Component({
    selector: 'water',
    templateUrl: './water.component.html'
})
export class WaterComponent {
  @Input("waterId")
  waterId: number | null = null;
  @Output()
  waterIdChange = new EventEmitter<number | null>();
  @Input("readOnly")
  readOnly: boolean = false;
  waterOptions: { name: string, id: number }[] = [];

  constructor(private editorService: EditorService) {
    editorService.readWaterObjectNameIds().then(objectNameIds => {
      this.waterOptions = [];
      objectNameIds.forEach(objectNameId => {
        this.waterOptions.push({name: objectNameId.internalName, id: objectNameId.id});
      });
    })
  }

  onChange() {
    this.waterIdChange.emit(this.waterId);
  }

  getCurrentName(): string {
    if (this.waterId) {
      return this.waterOptions.find(value => value.id === this.waterId)?.name || "";
    } else {
      return "";
    }
  }
}
