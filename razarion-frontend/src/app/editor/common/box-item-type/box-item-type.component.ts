import { HttpClient } from '@angular/common/http';
import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { TypescriptGenerator } from 'src/app/backend/typescript-generator';
import { BoxItemTypeEditorControllerClient } from 'src/app/generated/razarion-share';
import {SelectModule} from 'primeng/select';
import {FormsModule} from '@angular/forms';

@Component({
  selector: 'box-item-type',
  imports: [
    SelectModule,
    FormsModule
  ],
  templateUrl: './box-item-type.component.html'
})
export class BoxItemTypeComponent implements OnInit {
  @Input("boxItemTypeId")
  boxItemTypeId: number | null = null;
  @Output()
  boxItemTypeIdChange = new EventEmitter<number | null>();
  private boxItemTypeEditorControllerClient: BoxItemTypeEditorControllerClient;
  options: { label: string, boxItemTypeId: number }[] = [];

  constructor(httpClient: HttpClient) {
    this.boxItemTypeEditorControllerClient = new BoxItemTypeEditorControllerClient(TypescriptGenerator.generateHttpClientAdapter(httpClient))
  }

  ngOnInit(): void {
    this.boxItemTypeEditorControllerClient.getObjectNameIds().then(objectNameIds => {
      this.options = [];
      objectNameIds.forEach(objectNameId => {
        this.options.push({ label: `${objectNameId.internalName} '${objectNameId.id}'`, boxItemTypeId: objectNameId.id });
      });
    });
  }

  onChange() {
    this.boxItemTypeIdChange.emit(this.boxItemTypeId);
  }

}
