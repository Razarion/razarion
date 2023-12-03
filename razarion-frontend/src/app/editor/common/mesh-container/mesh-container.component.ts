import { HttpClient } from '@angular/common/http';
import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { TypescriptGenerator } from 'src/app/backend/typescript-generator';
import { MeshContainerEditorControllerClient } from 'src/app/generated/razarion-share';

@Component({
  selector: 'mesh-container',
  templateUrl: './mesh-container.component.html'
})
export class MeshContainerComponent implements OnInit {
  @Input("meshContainerId")
  meshContainerId: number | null = null;
  @Output()
  meshContainerIdChange = new EventEmitter<number | null>();
  private meshContainerEditorControllerClient: MeshContainerEditorControllerClient;
  meshContainerOptions: { label: string, meshContainerId: number }[] = [];

  constructor(httpClient: HttpClient) {
    this.meshContainerEditorControllerClient = new MeshContainerEditorControllerClient(TypescriptGenerator.generateHttpClientAdapter(httpClient))
  }

  ngOnInit(): void {
    this.meshContainerEditorControllerClient.getObjectNameIds().then(objectNameIds => {
      this.meshContainerOptions = [];
      objectNameIds.forEach(objectNameId => {
        this.meshContainerOptions.push({ label: `${objectNameId.internalName} '${objectNameId.id}'`, meshContainerId: objectNameId.id });
      });
    });
  }

  onChange() {
    this.meshContainerIdChange.emit(this.meshContainerId);
  }

}
