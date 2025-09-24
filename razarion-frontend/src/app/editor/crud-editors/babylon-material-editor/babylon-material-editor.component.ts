import {Component, ViewChild} from '@angular/core';
import {CrudContainerChild} from '../crud-container/crud-container.component';
import {BabylonMaterialControllerClient, BabylonMaterialEntity, ObjectNameId} from 'src/app/generated/razarion-share';
import {FileUpload} from 'primeng/fileupload';
import {MessageService} from 'primeng/api';
import {TypescriptGenerator} from 'src/app/backend/typescript-generator';
import {HttpClient} from '@angular/common/http';
import {BabylonRenderServiceAccessImpl} from 'src/app/game/renderer/babylon-render-service-access-impl.service';
import {Material, NodeMaterial} from '@babylonjs/core';
import {ButtonModule} from 'primeng/button';
import {Divider} from 'primeng/divider';
import {FormsModule} from '@angular/forms';
import {Checkbox} from 'primeng/checkbox';
import {InputNumber} from 'primeng/inputnumber';
import JSZip from 'jszip';
import {BabylonModelService} from '../../../game/renderer/babylon-model.service';

@Component({
  selector: 'babylon-material-editor',
  imports: [
    ButtonModule,
    Divider,
    FileUpload,
    FormsModule,
    Checkbox,
    InputNumber
  ],
  templateUrl: './babylon-material-editor.component.html'
})
export class BabylonMaterialEditorComponent implements CrudContainerChild<BabylonMaterialEntity> {
  static editorControllerClient = BabylonMaterialControllerClient;
  @ViewChild('fileUploadElement')
  fileUploadElement!: FileUpload;
  selectedMaterial?: Material;

  babylonMaterialEntity!: BabylonMaterialEntity;
  private babylonMaterialControllerClient: BabylonMaterialControllerClient;
  allBabylonMaterials: Blob | null = null;

  constructor(private messageService: MessageService,
              private httpClient: HttpClient,
              renderEngine: BabylonRenderServiceAccessImpl,
              private babylonModelService: BabylonModelService) {
    this.babylonMaterialControllerClient = new BabylonMaterialControllerClient(TypescriptGenerator.generateHttpClientAdapter(this.httpClient));

    void Promise.all([
      import("@babylonjs/core/Debug/debugLayer"),
      import("@babylonjs/inspector"),
      import("@babylonjs/node-editor")
    ]).then((_values) => {
      renderEngine.getScene().debugLayer.onSelectionChangedObservable.add((selectedBabylonObject: any) => {
        this.selectedMaterial = undefined;
        if (selectedBabylonObject instanceof Material) {
          this.selectedMaterial = selectedBabylonObject;
        }
      });
    });

  }

  init(babylonMaterialEntity: BabylonMaterialEntity): void {
    this.babylonMaterialEntity = babylonMaterialEntity;
  }

  exportConfig(): BabylonMaterialEntity {
    return this.babylonMaterialEntity!;
  }

  getId(): number {
    return this.babylonMaterialEntity!.id;
  }

  onImportMaterial(event: any) {
    this.fileUploadElement.clear();
    this.upload(event.files[0]);
  }

  private upload(data: any) {
    try {
      const blob = new Blob([data], {type: 'application/octet-stream'});
      this.babylonMaterialControllerClient.uploadData(this.babylonMaterialEntity!.id, blob)
        .then(() => {
          this.messageService.add({
            severity: 'success',
            life: 300,
            summary: "Babylon material uploaded"
          });
        })
        .catch(err => {
          this.messageService.add({
            severity: 'error',
            summary: `Exception during babylon material upload ${err}`,
            sticky: true
          });
          console.error(err);
        });
    } catch (e) {
      this.messageService.add({
        severity: 'error',
        summary: `Exception during babylon material upload ${e}`,
        sticky: true
      });
      console.error(e);
    }
  }

  uploadSelectedMaterial() {
    if (this.selectedMaterial) {
      try {
        const str = JSON.stringify(this.selectedMaterial.serialize());
        this.upload(str);
      } catch (error) {
        this.messageService.add({
          severity: 'error',
          summary: `Exception during JSON.stringify ${error}`,
          sticky: true
        });
      }
    } else {
      this.messageService.add({
        severity: 'error',
        summary: `No material selected.`,
        sticky: true
      });
    }
  }

  onCollectAll() {
    this.allBabylonMaterials = null;
    this.babylonMaterialControllerClient.getObjectNameIds()
      .then(objectNameIds => {
        this.dumpAll(objectNameIds).then(allBabylonModelsZip => {
          allBabylonModelsZip.generateAsync({type: "blob"}, metadata => console.log(`${metadata.percent}%`))
            .then(blob => this.allBabylonMaterials = blob)
            .catch(error => this.messageService.add({
              severity: 'error',
              summary: `Exception during babylon material load ${error}`,
              detail: error,
              sticky: true
            }));
        });
      }).catch((e) => {
      this.messageService.add({
        severity: 'error',
        summary: `getObjectNameIds failed ${e.message}`,
        sticky: true
      });
      console.error(e);
    })
  }

  onDownloadAll() {
    const link = document.createElement("a");
    link.href = URL.createObjectURL(this.allBabylonMaterials!);
    link.setAttribute("download", "BabylonMaterials.zip");
    link.click();
  }

  dumpAll(objectNameIds: ObjectNameId[]): Promise<JSZip> {
    return new Promise<JSZip>((resolve) => {
      const zip = new JSZip();
      let pending = objectNameIds.length;
      objectNameIds.forEach((objectNameId) => {
        this.babylonMaterialControllerClient.getData(objectNameId.id)
          .then(data => {
            zip.file(`id_${objectNameId.id}`, JSON.stringify(data));
            pending--;
            if (pending == 0) {
              resolve(zip);
            }
          })
          .catch(e => {
            this.messageService.add({
              severity: 'error',
              summary: `download material failed ${e.message}`,
              sticky: true
            });
            console.error(e);
            pending--;
            if (pending == 0) {
              resolve(zip);
            }
          })
      })
    });

  }

  openMaterialNodeEditor() {
    let mat = this.babylonModelService.getBabylonMaterial(this.babylonMaterialEntity!.id);
    this.selectedMaterial = undefined;
    if (mat instanceof NodeMaterial) {
      this.selectedMaterial = mat;
      mat.edit();
    } else {
      this.messageService.add({
        severity: 'error',
        summary: "No node material",
        detail: `The material is not a node material ${this.babylonMaterialEntity!.internalName} '${this.babylonMaterialEntity!.id}'`,
        sticky: true
      })
    }
  }
}
