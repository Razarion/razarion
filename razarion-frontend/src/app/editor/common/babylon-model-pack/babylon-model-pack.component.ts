import { HttpClient } from '@angular/common/http';
import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { TypescriptGenerator } from 'src/app/backend/typescript-generator';
import { ThreeJsModelPackEditorControllerClient } from 'src/app/generated/razarion-share';

@Component({
  selector: 'babylon-model-pack',
  templateUrl: './babylon-model-pack.component.html'
})
export class BabylonModelPackComponent  implements OnInit {
  @Input("babylonModelPackId")
  babylonModelPackId: number | null = null;
  @Output()
  babylonModelPackIdChange = new EventEmitter<number | null>();
  private babylonModelEditorPackControllerClient: ThreeJsModelPackEditorControllerClient;
  babylonModelPackOptions: { label: string, babylonModelPackId: number }[] = [];

  constructor(httpClient: HttpClient) {
    this.babylonModelEditorPackControllerClient = new ThreeJsModelPackEditorControllerClient(TypescriptGenerator.generateHttpClientAdapter(httpClient))
  }

  ngOnInit(): void {
    this.babylonModelEditorPackControllerClient.getObjectNameIds().then(objectNameIds => {
      this.babylonModelPackOptions = [];
      objectNameIds.forEach(objectNameId => {
        this.babylonModelPackOptions.push({ label: `${objectNameId.internalName} '${objectNameId.id}'`, babylonModelPackId: objectNameId.id });
      });
    });
  }

  onChange() {
    this.babylonModelPackIdChange.emit(this.babylonModelPackId);
  }

}
