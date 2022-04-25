import {AfterViewInit, ChangeDetectorRef, Component, ElementRef, ViewChild} from '@angular/core';
import {AngularTreeNodeData} from "../../../gwtangular/GwtAngularFacade";
import {MessageService} from "primeng/api";

@Component({
  selector: 'image-property-editor',
  template:
    `
      <img *ngIf="imageUrl" style="height: 50px; width: 50px;" src="{{imageUrl}}" alt="Image">
      <canvas #canvas *ngIf="showCanvas" width="50" height="50"></canvas>
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
  showCanvas: boolean = false;
  @ViewChild('canvas')
  canvas!: ElementRef<HTMLCanvasElement>;

  constructor(private messageService: MessageService, private changeDetector: ChangeDetectorRef) {
  }

  ngAfterViewInit(): void {
    if (this.angularTreeNodeData.value !== undefined && this.angularTreeNodeData.value !== null) {
      if (this.angularTreeNodeData.value.constructor.name === 'ImageBitmap') {
        this.showCanvas = true;
        this.changeDetector.detectChanges();
        let context = this.canvas.nativeElement.getContext('2d');
        if (context != null) {
          const scale = this.canvas.nativeElement.width / this.angularTreeNodeData.value.width;
          context.drawImage(this.angularTreeNodeData.value,
            0,
            0,
            this.angularTreeNodeData.value.width * scale,
            this.angularTreeNodeData.value.height * scale);
        }
      } else {
        this.imageUrl = this.angularTreeNodeData.value.src;
      }
    }
  }

  onImport(event: any) {
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
          this.imageUrl = <any>reader.result;
          this.showCanvas = false;
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
