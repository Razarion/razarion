import {Component, OnInit} from '@angular/core';
import {EditorPanel} from "../editor-model";
import {HttpClient} from "@angular/common/http";
import {MessageService} from "primeng/api";
import {getUpdateUrl as common_getUpdateUrl, URL_IMAGE} from "../../common";
import {ImageGalleryItemComponent} from './image-gallery-item.component';
import {Button} from 'primeng/button';
import {NgForOf} from '@angular/common';
import {ImageControllerClient, ImageGalleryItem} from '../../generated/razarion-share';
import {TypescriptGenerator} from '../../backend/typescript-generator';
import {FileUploadModule} from 'primeng/fileupload';
import {CardModule} from 'primeng/card';

@Component({
  selector: 'image-editor',
  imports: [
    ImageGalleryItemComponent,
    Button,
    FileUploadModule,
    CardModule,
    NgForOf
  ],
  templateUrl: './image-editor.component.html'
})
export class ImageEditorComponent extends EditorPanel implements OnInit {
  imageGalleryItems: ImageGalleryItem[] = [];
  private imageControllerClient: ImageControllerClient;

  constructor(http: HttpClient,
              private messageService: MessageService) {
    super();
    this.imageControllerClient = new ImageControllerClient(TypescriptGenerator.generateHttpClientAdapter(http))
  }

  ngOnInit(): void {
    this.requestAllImages();
  }

  getUploadUrl() {
    return `${URL_IMAGE}/upload`
  }

  onUploadError(event: any) {
    this.messageService.add({
      severity: 'error',
      summary: `Error upload image`,
      detail: event.error.message,
      sticky: true
    });
  }

  onDeleteImage(imageGalleryItem: ImageGalleryItem) {
    this.imageControllerClient.delete(imageGalleryItem.id).then(value => {
      this.requestAllImages();
    }).catch((error) => {
      this.messageService.add({
        severity: 'error',
        summary: `Error delete image`,
        detail: error.message,
        sticky: true
      })
    })
  }

  getUpdateUrl(imageGalleryItem: ImageGalleryItem) {
    return common_getUpdateUrl(imageGalleryItem.id);
  }

  onUpdateError(event: any) {
    this.messageService.add({
      severity: 'error',
      summary: `Error update image`,
      detail: event.error.message,
      sticky: true
    });
  }

  requestAllImages(): void {
    this.imageControllerClient.getImageGalleryItems().then(imageGalleryItems => {
      this.imageGalleryItems = imageGalleryItems;
    }).catch(error => {
      this.messageService.add({
        severity: 'error',
        summary: `Error loading image gallery`,
        detail: error.message,
        sticky: true
      });
    })
  }
}
