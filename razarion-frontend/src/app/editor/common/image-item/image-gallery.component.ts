import {HttpClient} from "@angular/common/http";
import {Component, EventEmitter, Input, OnInit, Output} from "@angular/core";
import {TypescriptGenerator} from "src/app/backend/typescript-generator";
import {getImageUrl} from "src/app/common";
import {ImageControllerClient, ImageGalleryItem} from "src/app/generated/razarion-share";
import {NgClass, NgForOf} from '@angular/common';

@Component({
  selector: 'image-gallery',
  templateUrl: './image-gallery.component.html',
  imports: [
    NgClass,
    NgForOf
  ],
  styleUrls: ['./image-gallery.component.scss']
})
export class ImageGalleryComponent implements OnInit {
  @Input("selectedImageId")
  selectedImageId: number | null = null;
  @Output()
  selectedImageIdChange = new EventEmitter<number | null>();
  @Output()
  closeRequest = new EventEmitter<void>();
  imageGalleryItems: ImageGalleryItem[] = [];
  private imageControllerClient: ImageControllerClient;

  constructor(httpClient: HttpClient) {
    this.imageControllerClient = new ImageControllerClient(TypescriptGenerator.generateHttpClientAdapter(httpClient));
  }

  ngOnInit(): void {
    this.imageControllerClient.getImageGalleryItems().then(imageGalleryItems => {
      this.imageGalleryItems = imageGalleryItems;
    });
  }

  getImgUrl(imageGalleryItem: ImageGalleryItem): string {
    return getImageUrl(imageGalleryItem.id);
  }

  isSelected(imageGalleryItem: ImageGalleryItem): boolean {
    return this.selectedImageId === imageGalleryItem.id;
  }

  onSelect(imageGalleryItem: ImageGalleryItem) {
    this.selectedImageId = imageGalleryItem.id;
    this.selectedImageIdChange.emit(this.selectedImageId);
    this.closeRequest.emit();
  }
}
