import { HttpClient } from '@angular/common/http';
import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { TypescriptGenerator } from 'src/app/backend/typescript-generator';
import { BabylonMaterialControllerClient } from 'src/app/generated/razarion-share';
import {DropdownModule} from 'primeng/dropdown';
import {FormsModule} from '@angular/forms';

@Component({
  selector: 'babylon-material',
  imports: [
    DropdownModule,
    FormsModule
  ],
  templateUrl: './babylon-material.component.html'
})
export class BabylonMaterialComponent implements OnInit {
  @Input("babylonMaterialId")
  babylonMaterialId: number | null = null;
  @Output()
  babylonMaterialIdChange = new EventEmitter<number | null>();
  private babylonMaterialControllerClient: BabylonMaterialControllerClient;
  babylonMaterialOptions: { label: string, babylonMaterialId: number }[] = [];

  constructor(httpClient: HttpClient) {
    this.babylonMaterialControllerClient = new BabylonMaterialControllerClient(TypescriptGenerator.generateHttpClientAdapter(httpClient));
  }

  ngOnInit(): void {
    this.babylonMaterialControllerClient.getObjectNameIds().then(objectNameIds => {
      this.babylonMaterialOptions = [];
      objectNameIds.forEach(objectNameId => {
        this.babylonMaterialOptions.push({ label: `${objectNameId.internalName} '${objectNameId.id}'`, babylonMaterialId: objectNameId.id });
      });
    });
  }

  onChange() {
    this.babylonMaterialIdChange.emit(this.babylonMaterialId);
  }
}
