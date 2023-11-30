import { HttpClient } from '@angular/common/http';
import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { TypescriptGenerator } from 'src/app/backend/typescript-generator';
import { ParticleSystemEditorControllerClient } from 'src/app/generated/razarion-share';

@Component({
  selector: 'particle-system',
  templateUrl: './particle-system.component.html'
})
export class ParticleSystemComponent implements OnInit {
  @Input("particleSystemId")
  particleSystemId: number | null = null;
  @Output()
  particleSystemIdChange = new EventEmitter<number | null>();
  private particleSystemEditorControllerClient: ParticleSystemEditorControllerClient;
  particleSystemOptions: { label: string, particleSystemId: number }[] = [];

  constructor(httpClient: HttpClient) {
    this.particleSystemEditorControllerClient = new ParticleSystemEditorControllerClient(TypescriptGenerator.generateHttpClientAdapter(httpClient))
  }

  ngOnInit(): void {
    this.particleSystemEditorControllerClient.getObjectNameIds().then(objectNameIds => {
      this.particleSystemOptions = [];
      objectNameIds.forEach(objectNameId => {
        this.particleSystemOptions.push({ label: `${objectNameId.internalName} '${objectNameId.id}'`, particleSystemId: objectNameId.id });
      });
    });
  }

  onChange() {
    this.particleSystemIdChange.emit(this.particleSystemId);
  }

}
