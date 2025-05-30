import { Component, OnInit } from '@angular/core';
import { EditorPanel } from "../editor-model";
import { HttpClient } from "@angular/common/http";
import { MessageService } from "primeng/api";
import { URL_IMAGE, getUpdateUrl as common_getUpdateUrl} from "../../common";
import { ImageGalleryItem } from "../../gwtangular/GwtAngularFacade";
import {ImageGalleryItemComponent} from './image-gallery-item.component';
import {Button} from 'primeng/button';
import {FileUpload} from 'primeng/fileupload';
import {NgForOf} from '@angular/common';

@Component({
  selector: 'image-editor',
  imports: [
    ImageGalleryItemComponent,
    Button,
    FileUpload,
    NgForOf
  ],
  templateUrl: './image-editor.component.html'
})
export class ImageEditorComponent extends EditorPanel implements OnInit {
  imageGalleryItems: ImageGalleryItem[] = [];

  constructor(private http: HttpClient,
    private messageService: MessageService) {
    super();
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
    this.http.delete<ImageGalleryItem[]>(URL_IMAGE + `/delete/${imageGalleryItem.id}`)
      .subscribe(
        () => this.requestAllImages(),
        error => {
          this.messageService.add({
            severity: 'error',
            summary: `Error deleting image`,
            detail: error.message,
            sticky: true
          });
        });

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
    this.http.get<ImageGalleryItem[]>(URL_IMAGE + '/image-gallery')
      .subscribe(
        imageGalleryItems => this.imageGalleryItems = imageGalleryItems,
        event => {
          this.messageService.add({
            severity: 'error',
            summary: `Error loading image gallery`,
            detail: event.error.message,
            sticky: true
          });
        });
  }
}
