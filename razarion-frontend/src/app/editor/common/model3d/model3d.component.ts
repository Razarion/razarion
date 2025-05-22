import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {Model3DControllerClient} from "../../../generated/razarion-share";
import {HttpClient} from "@angular/common/http";
import {TypescriptGenerator} from "../../../backend/typescript-generator";
import {FormsModule} from '@angular/forms';
import {SelectModule} from 'primeng/select';

@Component({
  selector: 'model3d',
  imports: [
    SelectModule,
    FormsModule
  ],
  templateUrl: './model3d.component.html'
})
export class Model3dComponent implements OnInit {
  @Input("model3DId")
  model3DId: number | null = null;
  @Output()
  model3DIdChange = new EventEmitter<number | null>();
  private model3DControllerClient: Model3DControllerClient;
  model3DOptions: { label: string, model3DId: number }[] = [];

  constructor(httpClient: HttpClient) {
    this.model3DControllerClient = new Model3DControllerClient(TypescriptGenerator.generateHttpClientAdapter(httpClient))
  }

  ngOnInit(): void {
    this.model3DControllerClient.getObjectNameIds().then(objectNameIds => {
      this.model3DOptions = [];
      objectNameIds.forEach(objectNameId => {
        this.model3DOptions.push({
          label: `${objectNameId.internalName} '${objectNameId.id}'`,
          model3DId: objectNameId.id
        });
      });
    });
  }

  onChange() {
    this.model3DIdChange.emit(this.model3DId);
  }

}
