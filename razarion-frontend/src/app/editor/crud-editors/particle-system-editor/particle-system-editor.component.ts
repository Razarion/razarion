import {Component, ElementRef, ViewChild} from '@angular/core';
import {CrudContainerChild} from '../crud-container/crud-container.component';
import {DecimalPosition, ParticleSystemControllerClient, ParticleSystemEntity} from 'src/app/generated/razarion-share';
import {BabylonRenderServiceAccessImpl} from 'src/app/game/renderer/babylon-render-service-access-impl.service';
import {LocationVisualization} from '../../common/place-config/location-visualization';
import {ParticleSystem, Vector3} from '@babylonjs/core';
import {TypescriptGenerator} from "../../../backend/typescript-generator";
import {HttpClient} from "@angular/common/http";
import {MessageService} from "primeng/api";
import {ButtonModule} from 'primeng/button';
import {Checkbox} from 'primeng/checkbox';
import {FormsModule} from '@angular/forms';
import {InputNumber} from 'primeng/inputnumber';
import {DecimalPositionComponent} from '../../common/decimal-position/decimal-position.component';
import {Divider} from 'primeng/divider';
import {ImageItemComponent} from '../../common/image-item/image-item.component';
import {VertexEditorComponent} from '../../common/vertex-editor/vertex-editor.component';
import {CommonModule} from '@angular/common';

@Component({
  selector: 'particle-system-editor',
  imports: [
    ButtonModule,
    Checkbox,
    FormsModule,
    InputNumber,
    DecimalPositionComponent,
    Divider,
    ImageItemComponent,
    VertexEditorComponent,
    CommonModule
  ],
  templateUrl: './particle-system-editor.component.html'
})
export class ParticleSystemEditorComponent implements CrudContainerChild<ParticleSystemEntity> {
  static editorControllerClient = ParticleSystemControllerClient;
  particleSystemEntity!: ParticleSystemEntity;
  terrainPosition: DecimalPosition | null = null;
  terrainHeight: number | null = null;
  length?: number;
  stretchToDestination = false;
  particleSystem?: ParticleSystem;
  particleSystemControllerClient: ParticleSystemControllerClient;
  @ViewChild('fileInput')
  fileInput!: ElementRef<HTMLInputElement>;

  constructor(private messageService: MessageService,
              private rendererService: BabylonRenderServiceAccessImpl,
              httpClient: HttpClient) {
    this.particleSystemControllerClient = new ParticleSystemControllerClient(TypescriptGenerator.generateHttpClientAdapter(httpClient))
  }

  init(particleSystemEntity: ParticleSystemEntity): void {
    this.particleSystemEntity = particleSystemEntity;
  }

  exportConfig(): ParticleSystemEntity {
    return this.particleSystemEntity;
  }

  getId(): number {
    return this.particleSystemEntity.id;
  }

  startParticleSystem() {
    if (this.particleSystem) {
      return;
    }
    if (this.terrainPosition) {
      let destination: Vector3 | null = null;
      if (this.length) {
        destination = new Vector3(this.terrainPosition.x + this.length, this.terrainHeight!, this.terrainPosition.y);
      }
      this.particleSystem = this.rendererService.createParticleSystem(this.particleSystemEntity.id,
        this.particleSystemEntity.imageId,
        new Vector3(this.terrainPosition.x, this.terrainHeight!, this.terrainPosition.y),
        destination,
        this.stretchToDestination);
      this.particleSystem.name = `Editor: ${this.particleSystemEntity.internalName} (${this.particleSystemEntity.id})`;
      this.particleSystem.start();
    }
  }

  disposeParticleSystem() {
    if (this.particleSystem) {
      this.particleSystem.dispose();
      this.particleSystem = undefined;
    }
  }

  uploadParticleSystem() {
    if (this.particleSystem) {
      const json = JSON.stringify(this.particleSystem.serialize());
      this.upload(json);
    }
  }

  uploadImport() {
    this.fileInput.nativeElement.click();
  }

  onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    if (!input.files || input.files.length === 0) {
      return;
    }
    const file = input.files[0];

    const reader = new FileReader();
    reader.onload = (e) => {
      try {
        const text = e.target?.result as string;
        const jsonData = JSON.parse(text);
        this.particleSystem = ParticleSystem.Parse(jsonData, this.rendererService.getScene(), "");
        this.particleSystem.name = `Editor: ${this.particleSystemEntity.internalName} (${this.particleSystemEntity.id})`;
        this.particleSystem.emitter = new Vector3(this.terrainPosition!.x, this.terrainHeight!, this.terrainPosition!.y);
        this.particleSystem.start();
      } catch (error) {
        this.messageService.add({
          severity: 'error',
          summary: `Exception during particle system import ${error}`,
          sticky: true
        });
        console.error(error);
      }
    };
    reader.readAsText(file);
  }


  onTerrainPosition() {
    if (this.terrainPosition) {
      this.terrainHeight = LocationVisualization.getHeightFromTerrain(this.terrainPosition.x, this.terrainPosition.y, this.rendererService);
    }
  }

  private upload(data: any) {
    try {
      const blob = new Blob([data], {type: 'application/octet-stream'});
      this.particleSystemControllerClient.uploadData(this.particleSystemEntity!.id, blob)
        .then(() => {
          this.messageService.add({
            severity: 'success',
            life: 300,
            summary: "Particle system uploaded"
          });
        })
        .catch(err => {
          this.messageService.add({
            severity: 'error',
            summary: `Exception during particle system upload ${err}`,
            sticky: true
          });
          console.error(err);
        });
    } catch (e) {
      this.messageService.add({
        severity: 'error',
        summary: `Exception during particle system upload ${e}`,
        sticky: true
      });
      console.error(e);
    }
  }

}
