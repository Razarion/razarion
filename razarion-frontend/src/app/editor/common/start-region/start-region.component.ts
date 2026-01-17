import { Component, EventEmitter, Input, Output } from '@angular/core';
import { EditorService } from '../../editor-service';
import { Select } from 'primeng/select';
import { FormsModule } from '@angular/forms';


@Component({
  selector: 'start-region',
  imports: [
    Select,
    FormsModule
  ],
  templateUrl: './start-region.component.html'
})
export class StartRegionComponent {
  @Input("startRegionId")
  startRegionId: number | null = null;
  @Output()
  startRegionIdChange = new EventEmitter<number | null>();
  @Input("readOnly")
  readOnly: boolean = false;
  startRegionOptions: { label: string, startRegionId: number }[] = [];

  constructor(editorService: EditorService) {
    editorService.readStartRegionObjectNameIds().then(objectNameIds => {
      this.startRegionOptions = [];
      objectNameIds.forEach(objectNameId => {
        this.startRegionOptions.push({ label: `${objectNameId.internalName} '${objectNameId.id}'`, startRegionId: objectNameId.id });
      });
    });
  }

  onChange() {
    this.startRegionIdChange.emit(this.startRegionId);
  }

  getCurrentName(): string {
    if (this.startRegionId) {
      return this.startRegionOptions.find(value => value.startRegionId === this.startRegionId)?.label || "";
    } else {
      return "";
    }
  }

}
