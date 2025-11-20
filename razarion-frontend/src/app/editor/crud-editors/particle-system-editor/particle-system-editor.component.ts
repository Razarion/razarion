import {Component} from '@angular/core';
import {CrudContainerChild} from '../crud-container/crud-container.component';
import {ParticleSystemControllerClient, ParticleSystemEntity} from 'src/app/generated/razarion-share';
import {BabylonRenderServiceAccessImpl} from 'src/app/game/renderer/babylon-render-service-access-impl.service';
import {NodeParticleSystemSet} from '@babylonjs/core';
import {TypescriptGenerator} from "../../../backend/typescript-generator";
import {HttpClient} from "@angular/common/http";
import {MessageService} from "primeng/api";
import {ButtonModule} from 'primeng/button';
import {FormsModule} from '@angular/forms';
import {InputNumber} from 'primeng/inputnumber';
import {Divider} from 'primeng/divider';
import {ImageItemComponent} from '../../common/image-item/image-item.component';
import {CommonModule} from '@angular/common';
import {SelectModule} from 'primeng/select';
import type {AbstractMesh} from '@babylonjs/core/Meshes/abstractMesh';
import {BabylonModelService} from '../../../game/renderer/babylon-model.service';

@Component({
  selector: 'particle-system-editor',
  imports: [
    ButtonModule,
    FormsModule,
    InputNumber,
    Divider,
    ImageItemComponent,
    CommonModule,
    SelectModule,
  ],
  templateUrl: './particle-system-editor.component.html'
})
export class ParticleSystemEditorComponent implements CrudContainerChild<ParticleSystemEntity> {
  static editorControllerClient = ParticleSystemControllerClient;
  particleSystemEntity!: ParticleSystemEntity;
  length?: number;
  particleSystemControllerClient: ParticleSystemControllerClient;

  emitterIdentifierOptions: { label: string, identifier: string }[] = [];
  emitterIdentifier: string | null = null;
  emittersFound: AbstractMesh[] = [];
  selectedEmitter: AbstractMesh | null = null;


  constructor(private messageService: MessageService,
              private rendererService: BabylonRenderServiceAccessImpl,
              httpClient: HttpClient) {
    this.particleSystemControllerClient = new ParticleSystemControllerClient(TypescriptGenerator.generateHttpClientAdapter(httpClient));
    this.emitterIdentifierOptions.push({label: "RAZ_P_<N>", identifier: BabylonModelService.RAZ_P_});
    this.emitterIdentifierOptions.push({label: "RAZ_I", identifier: BabylonModelService.RAZ_I});
    this.emitterIdentifierOptions.push({label: "RAZ_M_P_<N>", identifier: BabylonModelService.RAZ_M_P_});
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

  onScanEmitterIdentifier() {
    this.emittersFound = this.rendererService.getScene().meshes.filter(m => m.name.startsWith(this.emitterIdentifier!));
    this.onSelectEmitter();
  }

  onSelectEmitter() {
    if (!this.emittersFound.length) {
      this.selectedEmitter = null;
      return;
    }

    if (this.emittersFound.length == 1) {
      this.selectedEmitter = this.emittersFound[0];
    } else {
      if (this.selectedEmitter) {
        const currentIndex = this.emittersFound.indexOf(this.selectedEmitter);
        const nextIndex = (currentIndex + 1) % this.emittersFound.length;
        this.selectedEmitter = this.emittersFound[nextIndex];
      } else {
        this.selectedEmitter = this.emittersFound[0];
      }
    }
  }

  onNodeParticleEditor() {
    this.particleSystemControllerClient.getData(this.particleSystemEntity.id)
      .then(data => {
        let nodeParticleSystemSet;
        try {
          nodeParticleSystemSet = NodeParticleSystemSet.Parse(data);
        } catch (error) {
          this.messageService.add({
            severity: 'error',
            summary: `Exception during particle system start ${error}. Default is used`,
            sticky: true
          });
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
                action: () => {
                  return new Promise((resolve, reject) => {
                    this.particleSystemControllerClient.uploadData(this.particleSystemEntity!.id, nodeParticleSystemSet.serialize())
                      .then(() => {
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
  }

  onStopAllParticleSystems() {
    this.rendererService.getScene().particleSystems.forEach((particleSystem) => {
      particleSystem.stop();
      particleSystem.dispose(true);
    })
  }
}
