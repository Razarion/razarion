import {EditorPanel} from "../editor-model";
import {Component, ViewChild} from "@angular/core";
import * as BABYLON from 'babylonjs';
import {MessageService} from "primeng/api";
import {ThreeJsRendererServiceImpl} from "../../game/renderer/three-js-renderer-service.impl";
import {FileUpload} from "primeng/fileupload/fileupload";
import {environment} from 'src/environments/environment';
import {GameMockService} from "../../game/renderer/game-mock.service";
import {GwtAngularService} from "../../gwtangular/GwtAngularService";
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {URL_THREE_JS_MODEL, URL_THREE_JS_MODEL_EDITOR} from "../../common";
import Mesh = BABYLON.Mesh;

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

  constructor(private gwtAngularService: GwtAngularService,
              private messageService: MessageService,
              private renderEngine: ThreeJsRendererServiceImpl,
              private http: HttpClient,
              gameMockService: GameMockService,
  ) {
    super();
    this.loadBabylonJsLoaders();
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
    const result = BABYLON.SceneLoader.Append('', gltfFile, this.renderEngine.getScene(), (scene: BABYLON.Scene) => {
        console.error(scene)
      },
      progress => {
      },
      (scene: BABYLON.Scene, message: string, exception?: any) => {
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

  private loadBabylonJsLoaders() {
    let script = document.createElement('script');
    script.type = 'text/javascript';
    script.src = "https://preview.babylonjs.com/loaders/babylonjs.loaders.js";
    script.onload = () => {
      console.info("Babylon-Loaders loaded")
    };
    script.onerror = (error: any) => {
      console.error(`error ${error}`)
    };
    document.getElementsByTagName('head')[0].appendChild(script);
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
      const strMesh = this.serializeBabylon(this.selectedBabylon);
      const httpOptions = {
        headers: new HttpHeaders({
          'Content-Type': 'application/octet-stream'
        })
      };
      this.http.put(`${URL_THREE_JS_MODEL_EDITOR}/upload/${this.dropDownBabylonModel.id}`, new Blob([strMesh]), httpOptions)
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
      const strMesh = this.serializeBabylon(this.selectedBabylon);
      const link = document.createElement("a");
      link.href = URL.createObjectURL(new Blob([strMesh]));
      link.setAttribute("download", "dump-selected.babylon");
      link.click();
    } catch (error) {
      console.warn(error);
      this.messageService.add({
        severity: 'error',
        summary: `Can not export BABYLON`,
        detail: String(error),
        sticky: true
      });
    }
  }

  onLoad() {
    const url = `${URL_THREE_JS_MODEL}/${this.dropDownLoadBabylonModel.id}`;
    const result = BABYLON.SceneLoader.Append(url, '', this.renderEngine.getScene(), (scene: BABYLON.Scene) => {
      },
      progress => {
      },
      (scene: BABYLON.Scene, message: string, exception?: any) => {
        console.error(`Error loading Babylon file '${message}'. exception: '${exception}'`);
        this.messageService.add({
          severity: 'error',
          summary: `Exception during Babylon load ${message}`,
          detail: exception,
          sticky: true
        });

      })
    if (result === null) {
      console.error("Error loading Babylon");
      this.messageService.add({
        severity: 'error',
        summary: `Error loading Babylon`,
        sticky: true
      });
    }
  }

  private serializeBabylon(mesh: Mesh) {
    const serializedMesh = BABYLON.SceneSerializer.SerializeMesh(mesh, false, true);
    return JSON.stringify(serializedMesh);
  }
}
