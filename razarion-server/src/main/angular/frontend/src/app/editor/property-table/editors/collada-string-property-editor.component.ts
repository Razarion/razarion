import {Component, OnInit, ViewChild} from '@angular/core';
import {AngularTreeNodeData, GwtAngularPropertyTable} from "../../../gwtangular/GwtAngularFacade";
import {FileUpload} from "primeng/fileupload/fileupload";
import {GwtAngularService} from "../../../gwtangular/GwtAngularService";
import {MessageService} from "primeng/api";

@Component({
  selector: 'collada-string-property-editor',
  template: `
    <div class="p-d-flex p-flex-column">
      <div class="p-mb-2">
        <p-fileUpload #fileUploadElement
                      chooseIcon="pi pi-folder-open"
                      mode="basic"
                      [auto]="true"
                      [customUpload]=true
                      (uploadHandler)="onUpload($event)">
        </p-fileUpload>
      </div>
      <div *ngIf="state !== undefined" class="p-mb-2" style="color: #ff00b6">
        {{state}}
      </div>
      <div *ngIf="lastLoadedDate !== undefined" class="p-mb-2" style="white-space: nowrap">
        File loaded: {{lastLoadedDate | date:'dd.MM.yyyy HH:mm:ss'}}
      </div>
      <div *ngIf="lastModifiedDate !== undefined" class="p-mb-2" style="white-space: nowrap">
        File date:   {{lastModifiedDate | date:'dd.MM.yyyy HH:mm:ss'}}
      </div>
    </div>
  `
})
export class ColladaStringPropertyEditorComponent implements OnInit {
  angularTreeNodeData!: AngularTreeNodeData;
  gwtAngularPropertyTable!: GwtAngularPropertyTable;
  @ViewChild('fileUploadElement')
  fileUploadElement!: FileUpload;
  lastModifiedDate: Date | undefined;
  lastLoadedDate: Date | undefined;
  file?: any;
  state?: string;

  constructor(private messageService: MessageService, private gwtAngularService: GwtAngularService) {
  }

  ngOnInit(): void {
    this.setupPermanentFields();
  }

  onUpload(event: any) {
    this.file = event.files[0];
    this.loadFile();
  }

  private loadFile() {
    if (this.file === undefined) {
      this.state = "No file";
      this.messageService.add({
        severity: 'error',
        summary: 'No file',
        sticky: true
      })
      return;
    }
    this.lastModifiedDate = this.file.lastModifiedDate;
    this.lastLoadedDate = new Date();
    this.setupPermanentFields();
    let reader = new FileReader();
    reader.onerror = () => {
      console.error(reader.error)
      this.state = "Read failed";
      this.messageService.add({
        severity: 'error',
        summary: `Error during file read: ${reader.error}`,
        sticky: true
      });
    }
    reader.onload = progressEvent => {
      if (progressEvent.target !== null) {
        this.fileUploadElement.clear();
        if (typeof progressEvent.target.result === "string") {
          let colladaString = progressEvent.target.result;
          this.state = "Converting";
          this.gwtAngularService.gwtAngularFacade.editorFrontendProvider
            .getGenericEditorFrontendProvider().colladaConvert(this.gwtAngularPropertyTable, colladaString).then(
            () => {
              this.state = undefined;
              this.angularTreeNodeData.setValue(colladaString)
            },
            reason => {
              this.state = "Failed";
              this.messageService.add({
                severity: 'error',
                summary: `Can not process Collada file: ${reason}`,
                detail: reason,
                sticky: true
              });
              console.error(reason);
            });
        } else {
          this.state = "Failed";
          this.messageService.add({
            severity: 'error',
            summary: `Collada content must be a string ${typeof progressEvent.target.result}`,
            sticky: true
          });
        }
      } else {
        this.state = "Failed";
        this.messageService.add({
          severity: 'error',
          summary: 'No Target',
          sticky: true
        });
      }
    }
    reader.readAsText(this.file);
    this.state = "Reading";
  }

  private setupPermanentFields(): void {
    // TODO property is not allowed to have more then one Collada-String property or it will be overridden.
    let holder: any = this.gwtAngularPropertyTable;
    if (holder.permColladaStringFields !== undefined) {
      this.lastModifiedDate = holder.permColladaStringFields.lastModifiedDate;
      this.lastLoadedDate = holder.permColladaStringFields.lastLoadedDate;
      this.file = holder.permColladaStringFields.file;
    }
    holder.permColladaStringFields = this;
  }
}
