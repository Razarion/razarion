import {Component, OnInit, Pipe, PipeTransform} from '@angular/core';
import {
  AngularTreeNodeData,
  GenericEditorFrontendProvider,
  ImageGalleryItem,
  ObjectNameId
} from "../../../gwtangular/GwtAngularFacade";
import {URL_IMAGE} from "../../../common";
import {HttpClient} from "@angular/common/http";
import {MessageService} from "primeng/api";
import {GwtAngularService} from "../../../gwtangular/GwtAngularService";

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
      <div class="p-inputgroup">
        <p-button icon="pi pi-pencil"
                  styleClass="p-button-rounded p-button-text p-button-sm p-button-success"
                  (onClick)="showObjectNameIdGallery = !showObjectNameIdGallery">
        </p-button>
        <span (click)="showObjectNameIdGallery = !showObjectNameIdGallery"
          style="width: 7em; line-height: 2em; background-color: #17212f;border: 1px solid #304562;cursor: pointer">
          {{this.angularTreeNodeData.value.value}}</span>
        <p-button icon="pi pi-times"
                  styleClass="p-button-rounded p-button-text p-button-sm p-button-danger"
                  (onClick)="onObjectNameIdDeleted()"
                  *ngIf="this.angularTreeNodeData.value.value !== undefined">
        </p-button>
      </div>
      <p-dialog header="Choose {{this.angularTreeNodeData.value.collection}}"
                [(visible)]="showObjectNameIdGallery"
                [style]="{width: '20em'}"
                (onShow)="onShowCollectionGallery()">
        <p-dataView [value]="objectNameIds">
          <ng-template let-objectNameId pTemplate="listItem">
            <div class="p-col-7" style="cursor: pointer" (click)="onObjectNameIdClicked(objectNameId)">
              {{objectNameId.getInternalName()}} ({{objectNameId.getId()}})
            </div>
          </ng-template>
        </p-dataView>
      </p-dialog>
    </div>
  `
})
export class CollectionReferencePropertyEditorComponent implements OnInit {
  angularTreeNodeData!: AngularTreeNodeData;
  genericEditorFrontendProvider: GenericEditorFrontendProvider;
  collection!: string;
  showImageGallery: boolean = false;
  showObjectNameIdGallery: boolean = false;
  imageGalleryItems: ImageGalleryItem[] = [];
  objectNameIds: ObjectNameId[] = [];

  constructor(private messageService: MessageService,
              private http: HttpClient,
              gwtAngularService: GwtAngularService) {
    this.genericEditorFrontendProvider = gwtAngularService.gwtAngularFacade.editorFrontendProvider.getGenericEditorFrontendProvider();
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

  onObjectNameIdClicked(objectNameId: ObjectNameId) {
    this.angularTreeNodeData.setValue(objectNameId.getId());
    this.showObjectNameIdGallery = false;
    this.angularTreeNodeData.value.value = objectNameId.getId();
  }

  onObjectNameIdDeleted() {
    this.angularTreeNodeData.setValue(null);
    this.angularTreeNodeData.value.value = undefined;
  }

  onShowCollectionGallery() {
    this.genericEditorFrontendProvider.requestObjectNameIds(this.angularTreeNodeData.value.collection)
      .then(value => this.objectNameIds = value,
        reason => {
          this.messageService.add({
            severity: 'error',
            summary: `Can not load ObjectNameIds for: ${this.angularTreeNodeData.value.collection}`,
            detail: reason,
            sticky: true
          });
          console.error(reason);
        });

  }
}

@Pipe({name: 'typeTransform'})
export class ImageTypePipe implements PipeTransform {
  transform(value: any): any {
    return value.replace("image/", '');
  }

}
