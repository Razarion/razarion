import { HttpClient } from '@angular/common/http';
import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { TypescriptGenerator } from 'src/app/backend/typescript-generator';
import { ParticleSystemControllerClient } from 'src/app/generated/razarion-share';

@Component({
  selector: 'particle-system',
  templateUrl: './particle-system.component.html'
})
export class ParticleSystemComponent implements OnInit {
  @Input("particleSystemId")
  particleSystemId: number | null = null;
  @Output()
  particleSystemIdChange = new EventEmitter<number | null>();
  private particleSystemControllerClient: ParticleSystemControllerClient;
  particleSystemOptions: { label: string, particleSystemId: number }[] = [];

  constructor(httpClient: HttpClient) {
    this.particleSystemControllerClient = new ParticleSystemControllerClient(TypescriptGenerator.generateHttpClientAdapter(httpClient))
  }

  ngOnInit(): void {
    this.particleSystemControllerClient.getObjectNameIds().then(objectNameIds => {
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
