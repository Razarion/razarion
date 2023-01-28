import {EditorPanel} from "../editor-model";
import {Component, ViewChild} from "@angular/core";
import {MessageService} from "primeng/api";
import {ThreeJsRendererServiceImpl} from "../../game/renderer/three-js-renderer-service.impl";
import {FileUpload} from "primeng/fileupload/fileupload";
import {environment} from 'src/environments/environment';
import {GameMockService} from "../../game/renderer/game-mock.service";
import {GwtAngularService} from "../../gwtangular/GwtAngularService";
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {URL_THREE_JS_MODEL, URL_THREE_JS_MODEL_EDITOR} from "../../common";
import {Mesh, Scene, SceneLoader} from "@babylonjs/core";
import {GLTF2Export} from "@babylonjs/serializers";
import {BabylonModelService} from "../../game/renderer/babylon-model.service";

@Component({
  selector: 'render-engine',
  templateUrl: './render-engine.component.html'
})
export class RenderEngineComponent extends EditorPanel {
  @ViewChild('fileUploadElement')
  fileUploadElement!: FileUpload;
  dropDownBabylonModels: any[] = [];
  dropDownBabylonModel: any = null;
  selectedBabylon: any;
  selectedBabylonName: any;
  selectedBabylonId: any;
  selectedBabylonClass: any;
  dropDownLoadBabylonModel: any = null;
  allBabylonModels!: Blob;

  constructor(private gwtAngularService: GwtAngularService,
              private messageService: MessageService,
              private renderEngine: ThreeJsRendererServiceImpl,
              private http: HttpClient,
              gameMockService: GameMockService,
              private babylonModelService: BabylonModelService
  ) {
    super();
    renderEngine.getScene().debugLayer.onSelectionChangedObservable.add((selectedBabylonObject: any) => {
      this.setupSavePanel(selectedBabylonObject)
    });
    if (environment.gwtMock) {
      this.dropDownBabylonModels = gameMockService.threeJsModels;
    } else {
      gwtAngularService.gwtAngularFacade.editorFrontendProvider.getGenericEditorFrontendProvider().requestObjectNameIds("Three.js Model")
        .then((value: any) => this.dropDownBabylonModels = value,
          (reason: any) => {
            console.error(reason);
            this.messageService.add({
              severity: 'error',
              summary: `Can not load THREE_JS_MODEL configs`,
              detail: reason,
              sticky: true
            });
          });
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
    const result = SceneLoader.Append('', gltfFile, this.renderEngine.getScene(), (scene: Scene) => {
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
      this.serializeGltfBlob(this.selectedBabylon).then((blob) => {
        const httpOptions = {
          headers: new HttpHeaders({
            'Content-Type': 'application/octet-stream'
          })
        };
        this.http.put(`${URL_THREE_JS_MODEL_EDITOR}/upload/${this.dropDownBabylonModel.id}`, blob, httpOptions)
          .subscribe({
            complete: () => this.messageService.add({
              severity: 'success',
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
        summary: `Can not export Babylon Model`,
        detail: String(error),
        sticky: true
      });
    }
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
      allBabylonModelsZip.generateAsync({type: "blob"})
        .then(blob => this.allBabylonModels = blob);
    });
  }

  onDumpAll() {
    const link = document.createElement("a");
    link.href = URL.createObjectURL(this.allBabylonModels);
    link.setAttribute("download", "BabylonJsModels.zip");
    link.click();
  }

  onLoad() {
    const url = `${URL_THREE_JS_MODEL}/${this.dropDownLoadBabylonModel.id}`;
    const result = SceneLoader.Append(url, '', this.renderEngine.getScene(), (scene: Scene) => {
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

}
