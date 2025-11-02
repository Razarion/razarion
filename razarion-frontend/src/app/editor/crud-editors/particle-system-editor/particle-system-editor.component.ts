import {Component, ElementRef, ViewChild} from '@angular/core';
import {CrudContainerChild} from '../crud-container/crud-container.component';
import {DecimalPosition, ParticleSystemControllerClient, ParticleSystemEntity} from 'src/app/generated/razarion-share';
import {BabylonRenderServiceAccessImpl} from 'src/app/game/renderer/babylon-render-service-access-impl.service';
import {LocationVisualization} from '../../common/place-config/location-visualization';
import {NodeParticleSystemSet, ParticleSystemSet, Vector3} from '@babylonjs/core';
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
import {SelectModule} from 'primeng/select';
import type {AbstractMesh} from '@babylonjs/core/Meshes/abstractMesh';

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
    CommonModule,
    SelectModule,
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
  particleSystemSet?: ParticleSystemSet;
  particleSystemControllerClient: ParticleSystemControllerClient;
  @ViewChild('fileInput')
  fileInput!: ElementRef<HTMLInputElement>;

  emitterIdentifierOptions: { label: string, identifier: string }[] = [];
  emitterIdentifier: string | null = null;
  emittersFound: AbstractMesh[] = [];
  selectedEmitter: AbstractMesh | null = null;


  constructor(private messageService: MessageService,
              private rendererService: BabylonRenderServiceAccessImpl,
              httpClient: HttpClient) {
    this.particleSystemControllerClient = new ParticleSystemControllerClient(TypescriptGenerator.generateHttpClientAdapter(httpClient));
    this.emitterIdentifierOptions.push({label: "RAZ_P_<X>", identifier: "RAZ_P_"});
    this.emitterIdentifierOptions.push({label: "RAZ_I", identifier: "RAZ_I"});
    this.emitterIdentifierOptions.push({label: "RAZ_M", identifier: "RAZ_M"});
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
    if (this.particleSystemSet) {
      return;
    }
    if (this.terrainPosition) {
      let destination: Vector3 | null = null;
      if (this.length) {
        destination = new Vector3(this.terrainPosition.x + this.length, this.terrainHeight!, this.terrainPosition.y);
      }
      this.rendererService.createParticleSystem(this.particleSystemEntity.id,
        this.particleSystemEntity.imageId,
        new Vector3(this.terrainPosition.x, this.terrainHeight!, this.terrainPosition.y),
        destination,
        this.stretchToDestination).then(particleSystemSet => {
        this.particleSystemSet = particleSystemSet;
        // TODO this.particleSystemSet.name = `Editor: ${this.particleSystemEntity.internalName} (${this.particleSystemEntity.id})`;
        this.particleSystemSet.start();
      });
    }
  }

  disposeParticleSystem() {
    if (this.particleSystemSet) {
      this.particleSystemSet.dispose();
      this.particleSystemSet = undefined;
    }
  }

  uploadParticleSystem() {
    if (this.particleSystemSet) {
      const json = JSON.stringify(this.particleSystemSet.serialize());
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
        //this.particleSystem = ParticleSystem.Parse(jsonData, this.rendererService.getScene(), "");
        //this.particleSystem.name = `Editor: ${this.particleSystemEntity.internalName} (${this.particleSystemEntity.id})`;
        //this.particleSystem.emitter = new Vector3(this.terrainPosition!.x, this.terrainHeight!, this.terrainPosition!.y);
        //this.particleSystem.start();
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

  onScanEmitterIdentifier() {
    this.emittersFound = this.rendererService.getScene().meshes.filter(m => m.name.startsWith(this.emitterIdentifier!));
  }

  onSelectEmitter() {
    if (!this.selectedEmitter) {
      this.selectedEmitter = this.emittersFound[0];
    } else {
      const currentIndex = this.emittersFound.indexOf(this.selectedEmitter);
      const nextIndex = (currentIndex + 1) % this.emittersFound.length;
      this.selectedEmitter = this.emittersFound[nextIndex];
    }
  }

  onNodeParticleEditor() {
    this.particleSystemControllerClient.getData(this.particleSystemEntity.id)
      .then(data => {
        let nodeParticleSystemSet;
        try {
          nodeParticleSystemSet = NodeParticleSystemSet.Parse(data);
        } catch (error) {
          console.warn(error)
          nodeParticleSystemSet = NodeParticleSystemSet.CreateDefault(this.particleSystemEntity.internalName);
        }
        void Promise.all([
          import("@babylonjs/core/Debug/debugLayer"),
          import("@babylonjs/inspector"),
          import("@babylonjs/node-editor"),
          import("@babylonjs/node-particle-editor")
        ]).then((_values) => {
          nodeParticleSystemSet.editAsync(<any>{
            nodeEditorConfig: {
              customSave: {
                label: `Razarion save: '${this.particleSystemEntity.internalName}' ${this.particleSystemEntity.id}`,
                action: (data: any) => {
                  return new Promise((resolve, reject) => {
                    this.particleSystemControllerClient.uploadData(this.particleSystemEntity!.id, data)
                      .then(value => {
                        this.messageService.add({
                          severity: 'success',
                          life: 300,
                          summary: "Particle system uploaded"
                        });
                        resolve(null);
                      })
                      .catch(reason => {
                        console.log(reason);
                        this.messageService.add({
                          severity: 'error',
                          summary: `Exception during particle system upload ${reason}`,
                          sticky: true
                        });
                        reject();
                      });
                  });
                },
              },
            }
          });
        });
        nodeParticleSystemSet.buildAsync(this.rendererService.getScene()).then((nodeParticleSystemSet) => {
          nodeParticleSystemSet.start(this.selectedEmitter!)
        });
      }).catch(err => {
      console.error(`Load Particle System failed (inner). '${this.particleSystemEntity.internalName} (${this.particleSystemEntity.id})' Reason: ${err}`);
    }).catch(err => {
      console.error(`Load Particle System failed (outer). '${this.particleSystemEntity.internalName} (${this.particleSystemEntity.id})' Reason: ${err}`);
    })
    //
    // NodeParticleSystemSet.SnippetUrl = "/rest/editor/particle-system/data";
    // NodeParticleSystemSet.ParseFromSnippetAsync("6").then(npe => {
    //   npe.buildAsync(this.rendererService.getScene()).then(particleSystemSet => {
    //     particleSystemSet.start(this.selectedEmitter!);
    //   });
    //   void Promise.all([
    //     import("@babylonjs/core/Debug/debugLayer"),
    //     import("@babylonjs/inspector"),
    //     import("@babylonjs/node-editor"),
    //     import("@babylonjs/node-particle-editor")
    //   ]).then((_values) => {
    //     npe.editAsync();
    //   });
    // });
  }

  onStopAllParticleSystems() {
    this.rendererService.getScene().particleSystems.forEach((particleSystem) => {
      particleSystem.stop();
      particleSystem.dispose(true);
    })
  }
}
