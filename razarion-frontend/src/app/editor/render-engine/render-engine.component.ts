import {EditorPanel} from "../editor-model";
import {Component, OnDestroy, ViewChild} from "@angular/core";
import {MessageService} from "primeng/api";
import {BabylonRenderServiceAccessImpl} from "../../game/renderer/babylon-render-service-access-impl.service";
import {FileUpload} from "primeng/fileupload/fileupload";
import {environment} from 'src/environments/environment';
import {GameMockService} from "../../game/renderer/game-mock.service";
import {GwtAngularService} from "../../gwtangular/GwtAngularService";
import { HttpClient, HttpHeaders } from "@angular/common/http";
import {URL_THREE_JS_MODEL, URL_THREE_JS_MODEL_EDITOR, URL_THREE_JS_MODEL_PACK_EDITOR} from "../../common";
import {
  AbstractMesh,
  Mesh,
  Nullable,
  Observer,
  ParticleSystem,
  PointerEventTypes,
  PointerInfo,
  Scene,
  SceneLoader,
  Vector3
} from "@babylonjs/core";
import {GLTF2Export} from "@babylonjs/serializers";
import {BabylonModelService} from "../../game/renderer/babylon-model.service";
import {ThreeJsModelConfig, ThreeJsModelPackConfig} from "../../gwtangular/GwtAngularFacade";
import {GwtHelper} from "../../gwtangular/GwtHelper";

@Component({
  selector: 'render-engine',
  templateUrl: './render-engine.component.html'
})
export class RenderEngineComponent extends EditorPanel implements OnDestroy {
  @ViewChild('fileUploadElement')
  fileUploadElement!: FileUpload;
  dropDownBabylonModels: any[] = [];
  dropDownBabylonModel: any = null;
  selectedBabylon: any;
  selectedBabylonName: any;
  selectedBabylonId: any;
  selectedBabylonClass: any;
  dropDownLoadBabylonModel: any = null;
  threeJsModelPackConfigs: any[] = [];
  threeJsModelPackThreeJsModelId: number | null = null;
  threeJsModelPackMesh: AbstractMesh | null = null;
  allBabylonModels!: Blob;
  terrainCursorXPosition: number | undefined;
  terrainCursorYPosition: number | undefined;
  terrainCursorZPosition: number | undefined;
  private pointerInfoObservable: Nullable<Observer<PointerInfo>>;

  constructor(private gwtAngularService: GwtAngularService,
              private messageService: MessageService,
              private renderEngine: BabylonRenderServiceAccessImpl,
              private httpClient: HttpClient,
              gameMockService: GameMockService,
              private babylonModelService: BabylonModelService
  ) {
    super();
    void Promise.all([
      import("@babylonjs/core/Debug/debugLayer"),
      import("@babylonjs/inspector"),
      import("@babylonjs/node-editor")
    ]).then((_values) => {
      renderEngine.getScene().debugLayer.onSelectionChangedObservable.add((selectedBabylonObject: any) => {
        this.setupSavePanel(selectedBabylonObject)
      });
    });

    if (environment.gwtMock) {
      this.dropDownBabylonModels = gameMockService.getThreeJsModels();
    } else {
      throw new Error("Not Implemented getThreeJsModels()");
      // gwtAngularService.gwtAngularFacade.editorFrontendProvider.getGenericEditorFrontendProvider().requestObjectNameIds("Three.js Model")
      //   .then((value: any) => this.dropDownBabylonModels = value,
      //     (reason: any) => {
      //       console.error(reason);
      //       this.messageService.add({
      //         severity: 'error',
      //         summary: `Can not load THREE_JS_MODEL configs`,
      //         detail: reason,
      //         sticky: true
      //       });
      //     });
    }
    this.pointerInfoObservable = renderEngine.getScene().onPointerObservable.add((pointerInfo: PointerInfo) => {
      if (!this.gwtAngularService.gwtAngularFacade.inputService) {
        this.terrainCursorXPosition = undefined;
        this.terrainCursorYPosition = undefined;
        this.terrainCursorZPosition = undefined;
        return;
      }
      if (pointerInfo.type === PointerEventTypes.POINTERMOVE) {
        let pickingInfo = renderEngine.setupMeshPickPoint();
        if (pickingInfo.hit) {
          this.terrainCursorXPosition = pickingInfo.pickedPoint!.x;
          this.terrainCursorYPosition = pickingInfo.pickedPoint!.z;
          this.terrainCursorZPosition = pickingInfo.pickedPoint!.y;
        }
      }
    });
  }

  ngOnDestroy(): void {
    if (this.pointerInfoObservable) {
      this.renderEngine.getScene().onPointerObservable.remove(this.pointerInfoObservable);
      this.pointerInfoObservable = null;
    }
  }


  onImport(event: any) {
    try {
      this.fileUploadElement.clear();
      this.loadGltf(event.files[0]);
    } catch (e) {
      this.messageService.add({
        severity: 'error',
        summary: `Exception during GLTF load ${e}`,
        sticky: true
      });
      console.error(e);
    }
  }

  private loadGltf(gltfFile: File) {
    let addedMesh: any;
    let onNewMeshAdded = (mesh: AbstractMesh) => {
      addedMesh = mesh;
    };

    this.renderEngine.getScene().onNewMeshAddedObservable.addOnce(onNewMeshAdded);

    const result = SceneLoader.Append('', gltfFile, this.renderEngine.getScene(), (scene: Scene) => {
        // Set position here, it gets overridden before
        let position = this.renderEngine.setupCenterGroundPosition();
        addedMesh.position = new Vector3(position.x, position.y, position.z);
      },
      progress => {
      },
      (scene: Scene, message: string, exception?: any) => {
        console.error(`Error loading GLTF file '${message}'. exception: '${exception}'`);
        this.messageService.add({
          severity: 'error',
          summary: `Exception during GLTF load ${message}`,
          detail: exception,
          sticky: true
        });

      })
    if (result === null) {
      console.error("Error loading GLTF");
      this.messageService.add({
        severity: 'error',
        summary: `Error loading GLTF`,
        sticky: true
      });
    }
  }

  private setupSavePanel(selectedBabylonObject: any) {
    if (selectedBabylonObject) {
      this.selectedBabylon = selectedBabylonObject;
      this.selectedBabylonName = selectedBabylonObject.name;
      this.selectedBabylonClass = selectedBabylonObject.getClassName();
      this.selectedBabylonId = selectedBabylonObject.id;
    } else {
      this.selectedBabylon = null;
      this.selectedBabylonName = '';
      this.selectedBabylonClass = ''
      this.selectedBabylonId = ''
    }
  }

  onSaveSelected() {
    try {
      const type = GwtHelper.gwtIssueStringEnum(this.babylonModelService.getThreeJsModelConfig(this.dropDownBabylonModel.id).getType(), ThreeJsModelConfig.Type);
      switch (type) {
        case ThreeJsModelConfig.Type.GLTF:
          this.serializeGltfBlob(this.selectedBabylon).then((blob) => {
            const httpOptions = {
              headers: new HttpHeaders({
                'Content-Type': 'application/octet-stream'
              })
            };
            this.saveThreeJsModel(blob, httpOptions);
          }).catch(reason => {
            console.warn(reason);
            this.messageService.add({
              severity: 'error',
              summary: `Can not export GLTF`,
              detail: String(reason),
              sticky: true
            });
          });
          break;
        case ThreeJsModelConfig.Type.NODES_MATERIAL:
          this.messageService.add({
            severity: 'error',
            summary: `Can not export NodeMaterial`,
            detail: "Use Babylon.Js NodeMaterial Editor",
            sticky: true
          });
          break;
        case ThreeJsModelConfig.Type.PARTICLE_SYSTEM_JSON:
          const str = JSON.stringify(this.selectedBabylon.serialize());
          const httpOptions = {
            headers: new HttpHeaders({
              'Content-Type': 'application/octet-stream'
            })
          };
          this.saveThreeJsModel(new Blob([str]), httpOptions);
          break;
        default:
          this.messageService.add({
            severity: 'error',
            summary: `Can not export`,
            detail: `Unknown Type ${type}`,
            sticky: true
          });
      }
    } catch (error) {
      console.warn(error);
      this.messageService.add({
        severity: 'error',
        summary: `Can not export Babylon Model`,
        detail: String(error),
        sticky: true
      });
    }
  }

  private saveThreeJsModel(blob: Blob, httpOptions: { headers: HttpHeaders }) {
    this.httpClient.put(`${URL_THREE_JS_MODEL_EDITOR}/upload/${this.dropDownBabylonModel.id}`, blob, httpOptions)
      .subscribe({
        complete: () => this.messageService.add({
          severity: 'success',
          life: 300,
          summary: 'Save successful'
        }),
        error: (error: any) => {
          this.messageService.add({
            severity: 'error',
            summary: `Save failed ${error.statusText}`,
            detail: `${error.statusText}: ${error.status}`,
            sticky: true
          });
        }
      })
  }

  onDumpSelected() {
    try {
      this.serializeGltfBlob(this.selectedBabylon).then((blob) => {
        const link = document.createElement("a");
        link.href = URL.createObjectURL(blob);
        link.setAttribute("download", "dump-selected.glb");
        link.click();
      }).catch(reason => {
        console.warn(reason);
        this.messageService.add({
          severity: 'error',
          summary: `Can not export GLTF`,
          detail: String(reason),
          sticky: true
        });
      });
    } catch (error) {
      console.warn(error);
      this.messageService.add({
        severity: 'error',
        summary: `onDumpSelected() failed`,
        detail: String(error),
        sticky: true
      });
    }
  }

  onCollectAll() {
    this.babylonModelService.dumpAll().then(allBabylonModelsZip => {
      allBabylonModelsZip.generateAsync({type: "blob"}, metadata => console.log(`${metadata.percent}%`))
        .then(blob => this.allBabylonModels = blob)
        .catch(error => this.messageService.add({
          severity: 'error',
          summary: `Exception during Babylon load ${error}`,
          detail: error,
          sticky: true
        }));
    });
  }

  onDumpAll() {
    const link = document.createElement("a");
    link.href = URL.createObjectURL(this.allBabylonModels);
    link.setAttribute("download", "BabylonJsModels.zip");
    link.click();
  }

  onLoad() {
    const type: ThreeJsModelConfig.Type = GwtHelper.gwtIssueStringEnum(this.babylonModelService.getThreeJsModelConfig(this.dropDownLoadBabylonModel.id).getType(), ThreeJsModelConfig.Type);
    switch (type) {
      case ThreeJsModelConfig.Type.GLTF:
        this.loadGltfFromServer();
        break;
      case ThreeJsModelConfig.Type.NODES_MATERIAL:
        this.messageService.add({
          severity: 'error',
          summary: `Can not handle NodeMaterial`,
          sticky: true
        });
        break;
      case ThreeJsModelConfig.Type.PARTICLE_SYSTEM_JSON:
        this.loadParticleFromServer();
        break;
      default:
        this.messageService.add({
          severity: 'error',
          summary: `Can not load ${type}`,
          sticky: true
        });
        break;


    }
  }

  private loadGltfFromServer() {
    let importedMesh: AbstractMesh | null = null;
    let onNewMeshAdded = (abstractMesh: AbstractMesh) => {
      importedMesh = abstractMesh;
    };
    this.renderEngine.getScene().onNewMeshAddedObservable.addOnce(onNewMeshAdded);

    const url = `${URL_THREE_JS_MODEL}/${this.dropDownLoadBabylonModel.id}`;
    const result = SceneLoader.Append(url, '', this.renderEngine.getScene(), (scene: Scene) => {
        let position = this.renderEngine.setupCenterGroundPosition();
        importedMesh!.position = new Vector3(position.x, position.y, position.z);
      },
      progress => {
      },
      (scene: Scene, message: string, exception?: any) => {
        console.error(`Error loading Babylon file '${message}'. exception: '${exception}'`);
        this.messageService.add({
          severity: 'error',
          summary: `Exception during Babylon load ${message}`,
          detail: exception,
          sticky: true
        });

      },
      ".glb")
    if (result === null) {
      console.error("Error loading Babylon");
      this.messageService.add({
        severity: 'error',
        summary: `Error loading Babylon`,
        sticky: true
      });
    }
  }

  private loadParticleFromServer() {
    const url = `${URL_THREE_JS_MODEL}/${this.dropDownLoadBabylonModel.id}`;
    this.httpClient.get(url).subscribe({
      next: (json) => {
        let particleSystem = ParticleSystem.Parse(json, this.renderEngine.getScene(), "");
        let position = this.renderEngine.setupCenterGroundPosition();
        particleSystem.emitter = new Vector3(position.x, position.y, position.z);
        particleSystem.createPointEmitter(new Vector3(0.1, 1, 0.1), new Vector3(-0.1, 1, -0.1));

      },
      error: (error: any) => {
        console.error(`Load Particle System failed: ${this.dropDownLoadBabylonModel.internalName} (${this.dropDownLoadBabylonModel.id}) '${url}'`);
        this.messageService.add({
          severity: 'error',
          summary: `Load Particle System failed: ${this.dropDownLoadBabylonModel.internalName} (${this.dropDownLoadBabylonModel.id}) '${url}' ${error}`,
          sticky: true
        });
      }
    })
  }

  private serializeGltfBlob(mesh: Mesh): Promise<Blob> {
    let options = {
      shouldExportNode: function (node: any) {
        return node === mesh || (node.isDescendantOf && node.isDescendantOf(mesh));
      },
    };

    return new Promise((resolve, reject) => {
      return GLTF2Export.GLBAsync(this.renderEngine.getScene(), "fileName", options)
        .then((glb) => {
          const gltFiles = Object.values(glb)[0]
          const glbBlob = <Blob>Object.values(gltFiles)[0]
          resolve(glbBlob);
        }).catch(reason => reject(reason));
    })
  }

  onThreeJsModelPackConfigEvent(threeJsModel: any) {
    this.threeJsModelPackThreeJsModelId = threeJsModel.id;
    this.httpClient.post(`${URL_THREE_JS_MODEL_PACK_EDITOR}/findByThreeJsModelId/${threeJsModel.id}`, {})
      .subscribe({
        next: (threeJsModelPackConfigs: any) => {
          this.threeJsModelPackConfigs = threeJsModelPackConfigs;
          // threeJsModelPackConfigs.forEach((threeJsModelPackConfig: any) => {
          //   console.log(`"${threeJsModelPackConfig.internalName}" (${threeJsModelPackConfig.id}) ThreeJsModelId: ${threeJsModelPackConfig.threeJsModelId}`)
          // });

          this.threeJsModelPackMesh = null;
          let onNewMeshAdded = (abstractMesh: AbstractMesh) => {
            this.threeJsModelPackMesh = abstractMesh;
          };

          this.renderEngine.getScene().onNewMeshAddedObservable.addOnce(onNewMeshAdded);


          const url = `${URL_THREE_JS_MODEL}/${threeJsModel.id}`;
          const result = SceneLoader.Append(url, '', this.renderEngine.getScene(), (scene: Scene) => {
              let position = this.renderEngine.setupCenterGroundPosition();
              this.threeJsModelPackMesh!.position = new Vector3(position.x, position.y, position.z);
            },
            progress => {
            },
            (scene: Scene, message: string, exception?: any) => {
              console.error(`Error loading Babylon file '${message}'. exception: '${exception}'`);
              this.messageService.add({
                severity: 'error',
                summary: `Exception during Babylon load onEditorThreeJsModelPack() ${message}`,
                detail: exception,
                sticky: true
              });

            },
            ".glb")
          if (result === null) {
            console.error("Error loading Babylon");
            this.messageService.add({
              severity: 'error',
              summary: `Error loading Babylon`,
              sticky: true
            });
          }

        },
        error: (error: any) => {
          console.error(error);
          this.messageService.add({
            severity: 'error',
            summary: `${error.name}: ${error.status}`,
            detail: `${error.statusText}`,
            sticky: true
          });
        }
      })


  }

  onGenerateThreeJsModelPack() {
    const rootName = this.threeJsModelPackMesh!.name;
    this.threeJsModelPackMesh!.getChildren().forEach(node => {
      let namePath: string[] = [rootName, node.name];
      if (!this.findThreeJsModelPack4NamePath(namePath)) {
        this.httpClient.post(`${URL_THREE_JS_MODEL_PACK_EDITOR}/create`, {})
          .subscribe({
            next: (threeJsModelPackConfig: any) => {
              threeJsModelPackConfig.internalName = node.name;
              threeJsModelPackConfig.namePath = namePath;
              threeJsModelPackConfig.threeJsModelId = this.threeJsModelPackThreeJsModelId;
              this.onThreeJsModelPackConfigSave(threeJsModelPackConfig);
              this.threeJsModelPackConfigs.push(threeJsModelPackConfig);
            }
          })
      }
    })
  }

  private findThreeJsModelPack4NamePath(namePath: string[]) {
    const namePathString = namePath.toString();
    return this.threeJsModelPackConfigs.find(threeJsModelPackConfig => threeJsModelPackConfig.toString() === namePathString);
  }

  onThreeJsModelPackConfigSave(threeJsModelPackConfig: ThreeJsModelPackConfig) {
    this.httpClient.post(`${URL_THREE_JS_MODEL_PACK_EDITOR}/update`, threeJsModelPackConfig)
      .subscribe({
        error: (error: any) => {
          this.messageService.add({
            severity: 'error',
            summary: `Save failed ${error.statusText}`,
            detail: `${error.statusText}: ${error.status}`,
            sticky: true
          });
        }
      });
  }
}
