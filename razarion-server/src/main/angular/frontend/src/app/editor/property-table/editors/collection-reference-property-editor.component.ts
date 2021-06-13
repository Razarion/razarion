import {Component, OnInit, Pipe, PipeTransform} from '@angular/core';
import {AngularTreeNodeData, ImageGalleryItem} from "../../../gwtangular/GwtAngularFacade";
import {URL_IMAGE} from "../../../common";
import {HttpClient} from "@angular/common/http";
import {MessageService} from "primeng/api";

@Component({
  selector: 'collection-reference-property-editor',
  styles: ['.image-gallery-descr { width: 50px; overflow: hidden; white-space: nowrap; text-overflow:ellipsis }',
    '.image-gallery-img { width: 100px;height: 100px; background: url(\'/assets/TransparentBg.png\'); cursor: pointer}'],
  template: `
    <div *ngIf="this.angularTreeNodeData.value.collection==='image'">
      <img *ngIf="this.angularTreeNodeData.value.value !== undefined"
           src="{{getImageUrl(this.angularTreeNodeData.value.value)}}" alt="Show Image Gallery"
           class="image-gallery-img"
           (click)="showImageGallery = !showImageGallery">
      <p-button icon="pi pi-pencil"
                styleClass="p-button-rounded p-button-text p-button-sm p-button-success"
                (onClick)="showImageGallery = !showImageGallery"
                *ngIf="this.angularTreeNodeData.value.value === undefined">
      </p-button>
      <p-button icon="pi pi-times"
                styleClass="p-button-rounded p-button-text p-button-sm p-button-danger"
                (onClick)="onClearImage()"
                *ngIf="this.angularTreeNodeData.value.value !== undefined">
      </p-button>
      <p-dialog header="Choose Image" [(visible)]="showImageGallery" (onShow)="onShowImageGallery()">
        <p-dataView [value]="imageGalleryItems" [layout]="'grid'">
          <ng-template let-imageGalleryItem pTemplate="gridItem">
            <div class="p-col-fixed" style="background-color: #304562; margin: 1px">
              <table style="width: auto;height: auto">
                <tr>
                  <td colspan="2">
                    <img src="{{getImageUrl(imageGalleryItem.id)}}" alt="Show Image Gallery"
                         class="image-gallery-img"
                         (click)="onImageGalleryItemClicked(imageGalleryItem)">
                  </td>
                </tr>
                <tr>
                  <td>
                    <div class="image-gallery-descr">{{imageGalleryItem.id}}</div>
                  </td>
                  <td>
                    <div class="image-gallery-descr"
                         style="text-align: right;">{{imageGalleryItem.internalName}}</div>
                  </td>
                </tr>
                <tr>
                  <td>
                    <div class="image-gallery-descr">{{imageGalleryItem.size | formatFileSize:false}}</div>
                  </td>
                  <td>
                    <div class="image-gallery-descr"
                         style="text-align: right;">{{imageGalleryItem.type | typeTransform}}</div>
                  </td>
                </tr>
              </table>
            </div>
          </ng-template>
        </p-dataView>
      </p-dialog>
    </div>
    <div *ngIf="this.angularTreeNodeData.value.collection!=='image'" class="p-inputgroup">
      <button type="button" pButton label="Search"></button>
      <input type="text" pInputText placeholder="Keyword">
    </div>
  `
})
export class CollectionReferencePropertyEditorComponent implements OnInit {
  angularTreeNodeData!: AngularTreeNodeData;
  collection!: string;
  showImageGallery: boolean = false;
  imageGalleryItems: ImageGalleryItem[] = [];

  constructor(private messageService: MessageService,
              private http: HttpClient) {
  }

  ngOnInit(): void {
    this.angularTreeNodeData.value.collection;
  }

  getImageUrl(id: number): string {
    return `${URL_IMAGE}/${id}`
  }

  onShowImageGallery() {
    this.http.get<ImageGalleryItem[]>(URL_IMAGE + "/image-gallery").subscribe(
      value => this.imageGalleryItems = value,
      error => {
        console.error(error);
        try {
          this.messageService.add({
            severity: 'error',
            summary: 'Can not load image gallery from server',
            sticky: true
          })
        } catch (innerError) {
          console.error(innerError);
        }
      });
  }

  onImageGalleryItemClicked(imageGalleryItem: ImageGalleryItem) {
    this.angularTreeNodeData.setValue(imageGalleryItem.id);
    this.showImageGallery = false;
    this.angularTreeNodeData.value.value = imageGalleryItem.id;
  }

  onClearImage() {
    this.angularTreeNodeData.setValue(null);
    this.angularTreeNodeData.value.value = undefined;
  }
}

@Pipe({name: 'typeTransform'})
export class ImageTypePipe implements PipeTransform {
  transform(value: any): any {
    return value.replace("image/", '');
  }

}
