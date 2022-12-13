import {EditorPanel} from "../editor-model";
import {Component, ViewChild} from "@angular/core";
import * as BABYLON from 'babylonjs';
import {MessageService} from "primeng/api";
import {ThreeJsRendererServiceImpl} from "../../game/renderer/three-js-renderer-service.impl";
import {FileUpload} from "primeng/fileupload/fileupload";

@Component({
  selector: 'render-engine',
  templateUrl: './render-engine.component.html'
})
export class RenderEngineComponent extends EditorPanel {
  @ViewChild('fileUploadElement')
  fileUploadElement!: FileUpload;

  constructor(private messageService: MessageService, private renderEngine: ThreeJsRendererServiceImpl) {
    super();
    this.loadBabylonJsLoaders();
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

}
