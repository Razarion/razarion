import {Component, ViewChild} from '@angular/core';
import {CrudContainerChildPreUpdate} from "../crud-container/crud-container.component";
import {
  GltfControllerClient,
  GltfEntity,
  Model3DControllerClient,
  Model3DEntity
} from "../../../generated/razarion-share";
import {FileUpload, FileUploadHandlerEvent} from "primeng/fileupload";
import {MessageService} from "primeng/api";
import {BabylonRenderServiceAccessImpl} from "../../../game/renderer/babylon-render-service-access-impl.service";
import {AbstractMesh, Scene, SceneLoader} from "@babylonjs/core";
import {HttpClient} from "@angular/common/http";
import {TypescriptGenerator} from "../../../backend/typescript-generator";

@Component({
  selector: 'gltf-editor',
  templateUrl: './gltf-editor.component.html'
})
export class GltfEditorComponent implements CrudContainerChildPreUpdate<GltfEntity> {
  static editorControllerClient = GltfControllerClient;
  @ViewChild('fileUploadElement')
  fileUploadElement!: FileUpload;
  gltfEntity!: GltfEntity;
  model3DRows: Model3DRow[] = [];
  private gltfControllerClient: GltfControllerClient;
  private model3DControllerClient: Model3DControllerClient;
  private glbFile?: File;

  constructor(private messageService: MessageService,
              private renderEngine: BabylonRenderServiceAccessImpl,
              protected httpClient: HttpClient) {
    this.gltfControllerClient = new GltfControllerClient(TypescriptGenerator.generateHttpClientAdapter(httpClient));
    this.model3DControllerClient = new Model3DControllerClient(TypescriptGenerator.generateHttpClientAdapter(httpClient));
  }

  init(gltfEntity: GltfEntity): void {
    this.gltfEntity = gltfEntity;
    this.model3DRows = [];
    this.model3DControllerClient.getModel3DsByGltf(gltfEntity.id).then(gltfEntries => {
      gltfEntries.forEach(model3DEntity => this.model3DRows.push(new Model3DRow(model3DEntity)));
    })
  }

  postUpdate(): Promise<void> {
    let promises: Promise<void>[] = [];
    const blob = new Blob([this.glbFile!], {type: 'application/octet-stream'});
    promises.push(this.gltfControllerClient.uploadGlb(this.gltfEntity.id, blob));
    this.model3DRows.forEach(model3DRow => {
      switch (model3DRow.model3DState) {
        case Model3DState.NEW: {
          promises.push(this.model3DControllerClient.create().then(newModel3D => {
            newModel3D.gltfEntityId = model3DRow.model3DEntity.gltfEntityId;
            newModel3D.gltfName = model3DRow.model3DEntity.gltfName;
            newModel3D.internalName = model3DRow.model3DEntity.gltfName;
            return this.model3DControllerClient.update(newModel3D);
          }));
          break;
        }
        case Model3DState.DELETE: {
          promises.push(this.model3DControllerClient.delete(model3DRow.model3DEntity.id));
          break;
        }
      }
    });
    return Promise.all(promises).then();
  }

  onUpdateSuccess(): void {
    this.model3DControllerClient
      .read(this.gltfEntity.id)
      .then(gltfEntity => this.init(gltfEntity))
  }

  exportConfig(): GltfEntity {
    return this.gltfEntity;
  }

  getId(): number {
    return this.gltfEntity.id;
  }

  onImportGlb(event: FileUploadHandlerEvent) {
    try {
      this.fileUploadElement.clear();
      this.loadGltf(event.files[0]);
      this.glbFile = event.files[0];
    } catch (e) {
      this.messageService.add({
        severity: 'error',
        summary: `Exception during GLTF load ${e}`,
        sticky: true
      });
      console.error(e);
    }
  }

  severityByModel3DState(model3DState:Model3DState):any {
    switch (model3DState) {
      case Model3DState.UPDATE:
        return "info"
      case Model3DState.DELETE:
        return "danger"
      case Model3DState.NEW:
        return "warning"
    }
  }

  private loadGltf(gltfFile: File) {
    let addedMesh: AbstractMesh;

    this.renderEngine.getScene().onNewMeshAddedObservable.addOnce((mesh: AbstractMesh) => {
      addedMesh = mesh;
    });

    const result = SceneLoader.Append('', gltfFile, this.renderEngine.getScene(), (scene: Scene) => {
        this.model3DRows.forEach(model3DRow => model3DRow.model3DState = Model3DState.DELETE);
        addedMesh.getChildren().forEach(childNode => {
          let model3DRow = this.findModel3DRowByGltfName(childNode.name);
          if (model3DRow) {
            model3DRow.model3DState = Model3DState.UPDATE;
          } else {
            const gltfEntityId = this.gltfEntity.id;
            let model3DEntity = new class implements Model3DEntity {
              id = <any>null;
              gltfName = childNode.name;
              internalName = childNode.name;
              gltfEntityId = gltfEntityId;
            };
            model3DRow = new Model3DRow(model3DEntity);
            model3DRow.model3DState = Model3DState.NEW;
            this.model3DRows.push(model3DRow);
          }
        })
      },
      progress => {
      },
      (scene: Scene, message: string, exception?: any) => {
        console.error(`Error loading GLB file '${message}'. exception: '${exception}'`);
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

  private findModel3DRowByGltfName(gltfName: string): Model3DRow | undefined {
    return this.model3DRows.find(model3DRow => model3DRow.model3DEntity.gltfName == gltfName);
  }
}

enum Model3DState {
  UPDATE = "Update",
  DELETE = "Delete",
  NEW = "New"
}

class Model3DRow {
  model3DState?: Model3DState;

  constructor(public model3DEntity: Model3DEntity) {
  }

}
