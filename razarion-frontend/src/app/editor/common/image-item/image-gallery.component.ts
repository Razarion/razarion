import { HttpClient } from "@angular/common/http";
import { Component, EventEmitter, Input, OnInit, Output } from "@angular/core";
import { TypescriptGenerator } from "src/app/backend/typescript-generator";
import { getImageUrl } from "src/app/common";
import { ImageGalleryItem, ImageProviderClient } from "src/app/generated/razarion-share";

@Component({
    selector: 'image-gallery',
    templateUrl: './image-gallery.component.html',
    styleUrls: ['./image-gallery.component.scss'],
    standalone: false
})
export class ImageGalleryComponent implements OnInit {
    @Input("selectedImageId")
    selectedImageId: number | null = null;
    @Output()
    selectedImageIdChange = new EventEmitter<number | null>();
    @Output()
    closeRequest = new EventEmitter<void>();
    imageGalleryItems: ImageGalleryItem[] = [];
    private imageProviderClient: ImageProviderClient;

    constructor(httpClient: HttpClient) {
        this.imageProviderClient = new ImageProviderClient(TypescriptGenerator.generateHttpClientAdapter(httpClient));
    }

    ngOnInit(): void {
        this.imageProviderClient.getImageGalleryItems().then(imageGalleryItems => {
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
