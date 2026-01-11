import {Component, EventEmitter, Input, Output} from '@angular/core';
import {MessageService} from 'primeng/api';
import {getImageUrl as common_getImageUrl, getUpdateUrl as common_getUpdateUrl} from 'src/app/common';
import {ButtonModule} from 'primeng/button';
import {ImageGalleryComponent} from './image-gallery.component';
import {FileUploadModule} from 'primeng/fileupload';
import {PopoverModule} from 'primeng/popover';


@Component({
  selector: 'image-item',
  templateUrl: './image-item.component.html',
  styleUrls: ['./image-item.component.scss'],
  imports: [
    ButtonModule,
    PopoverModule,
    ImageGalleryComponent,
    FileUploadModule
],
  inputs: ['imageId']
})
export class ImageItemComponent {
  @Input("imageId")
  _imageId: number | null = null;
  @Output()
  imageIdChange = new EventEmitter<number | null>();

  constructor(private messageService: MessageService) {
  }

  get imageId(): number | null {
    return this._imageId;
  }

  set imageId(value: number | null) {
    this._imageId = value;
    this.onChange();
  }

  onChange() {
    this.imageIdChange.emit(this.imageId);
  }

  getImgUrl(): string {
    if (this.imageId || this.imageId === 0) {
      return common_getImageUrl(this.imageId);
    } else {
      return "";
    }
  }

  onDelete() {
    this.imageId = null;
  }

  getUpdateUrl() {
    return common_getUpdateUrl(this.imageId!);
  }

  onUpdateError(event: any) {
    this.messageService.add({
      severity: 'error',
      summary: `Error upload image`,
      detail: event.error.message,
      sticky: true
    });
  }

  onUploaded() {
    let imageId = this.imageId;
    this.imageId = null;
    this.imageId = imageId;
  }

}
