import { HttpClient } from '@angular/common/http';
import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { TypescriptGenerator } from 'src/app/backend/typescript-generator';
import { InventoryArtifactEditorControllerClient } from 'src/app/generated/razarion-share';
import {Select} from 'primeng/select';
import {FormsModule} from '@angular/forms';

@Component({
  selector: 'inventory-artifact',
  imports: [
    Select,
    FormsModule
  ],
  templateUrl: './inventory-artifact.component.html'
})
export class InventoryArtifactComponent implements OnInit {
  @Input("inventoryArtifactId")
  inventoryArtifactId: number | null = null;
  @Output()
  inventoryArtifactIdChange = new EventEmitter<number | null>();
  private inventoryArtifactEditorControllerClient: InventoryArtifactEditorControllerClient;
  options: { label: string, inventoryArtifactId: number }[] = [];

  constructor(httpClient: HttpClient) {
    this.inventoryArtifactEditorControllerClient = new InventoryArtifactEditorControllerClient(TypescriptGenerator.generateHttpClientAdapter(httpClient))
  }

  ngOnInit(): void {
    this.inventoryArtifactEditorControllerClient.getObjectNameIds().then(objectNameIds => {
      this.options = [];
      objectNameIds.forEach(objectNameId => {
        this.options.push({ label: `${objectNameId.internalName} '${objectNameId.id}'`, inventoryArtifactId: objectNameId.id });
      });
    });
  }

  onChange() {
    this.inventoryArtifactIdChange.emit(this.inventoryArtifactId);
  }

}
