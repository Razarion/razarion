import {AfterViewInit, Component} from '@angular/core';
import {AngularTreeNodeData} from "../../../gwtangular/GwtAngularFacade";
import {MessageService} from "primeng/api";

@Component({
  selector: 'image-property-editor',
  template:
    `
      <img style="height: 50px; width: 50px;" src="{{imageUrl}}" alt="Show Image Gallery">
      <p-fileUpload #fileUploadElement
                    chooseIcon="pi pi-folder-open"
                    mode="basic"
                    [auto]="true"
                    [customUpload]=true
                    (uploadHandler)="onImport($event)">
      </p-fileUpload>
    `
})
export class ImagePropertyEditorComponent implements AfterViewInit {
  angularTreeNodeData!: AngularTreeNodeData;
  imageUrl!: string;

  constructor(private messageService: MessageService) {
  }

  ngAfterViewInit(): void {
    this.imageUrl = this.angularTreeNodeData.value.src;
  }

  onImport(event: any) {
    const _this = this;
    try {
      let reader = new FileReader();
      reader.onerror = () => {
        console.error(reader.error)
        this.messageService.add({
          severity: 'error',
          summary: `Error reading file`,
          detail: `${reader.error}`,
          sticky: true
        });
      }
      reader.onload = () => {
        try {
          _this.imageUrl = <any>reader.result;
          this.angularTreeNodeData.setValue(reader.result);
        } catch (error) {
          console.log(error);
          this.messageService.add({
            severity: 'error',
            summary: `Error presenting image`,
            detail: `${error}`,
            sticky: true
          });
        }
      }
      reader.readAsDataURL(event.files[0]);
    } catch (error) {
      console.log(error);
      this.messageService.add({
        severity: 'error',
        summary: `Error importing image`,
        detail: `${error}`,
        sticky: true
      });
    }
  }
}
