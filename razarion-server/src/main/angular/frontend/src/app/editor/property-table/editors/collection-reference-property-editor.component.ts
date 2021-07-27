import {Component, OnInit} from '@angular/core';
import {
  AngularTreeNodeData,
  GenericEditorFrontendProvider,
  ImageGalleryItem,
  ObjectNameId
} from "../../../gwtangular/GwtAngularFacade";
import {HttpClient} from "@angular/common/http";
import {MessageService} from "primeng/api";
import {GwtAngularService} from "../../../gwtangular/GwtAngularService";
import {getImageUrl, URL_IMAGE} from "../../../common";

@Component({
  selector: 'collection-reference-property-editor',
  styles: ['.image { width: 100px;height: 100px; background: url(\'/assets/TransparentBg.png\'); cursor: pointer}'],
  template: `
    <div *ngIf="this.angularTreeNodeData.value.collection==='Image'">
      <img *ngIf="this.angularTreeNodeData.value.value !== undefined"
           src="{{getImgUrl()}}" alt="Show Image Gallery"
           class="image"
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
            <div class="p-col-fixed"
                 style="background-color: #304562; margin: 1px; cursor: pointer"
                 (click)="onImageGalleryItemClicked(imageGalleryItem)">
              <image-gallery-item [image-gallery-item]="imageGalleryItem"></image-gallery-item>
            </div>
          </ng-template>
        </p-dataView>
      </p-dialog>
    </div>
    <div *ngIf="this.angularTreeNodeData.value.collection!=='Image'" class="p-inputgroup">
      <div class="p-inputgroup">
        <p-button icon="pi pi-pencil"
                  styleClass="p-button-rounded p-button-text p-button-sm p-button-success"
                  (onClick)="showObjectNameIdGallery = !showObjectNameIdGallery">
        </p-button>
        <span (click)="showObjectNameIdGallery = !showObjectNameIdGallery"
              style="line-height: 2em; background-color: #17212f;border: 1px solid #304562;cursor: pointer; padding-left: 0.5em;padding-right: 0.5em;">
          {{objectNameIdString}}</span>
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
        <p-dataView [value]="objectNameIds" [sortField]="sortField" [sortOrder]="sortOrder">
          <ng-template let-objectNameId pTemplate="listItem">
            <div class="p-col-12" style="cursor: pointer" (click)="onObjectNameIdClicked(objectNameId)">
              {{objectNameId.internalName}} ({{objectNameId.id}})
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
  objectNameIdString: string = '';
  sortField: string = '-'; // Value must change or sort is not triggered
  sortOrder: number = 0; // Value must change or sort is not triggered

  constructor(private messageService: MessageService,
              private http: HttpClient,
              gwtAngularService: GwtAngularService) {
    this.genericEditorFrontendProvider = gwtAngularService.gwtAngularFacade.editorFrontendProvider.getGenericEditorFrontendProvider();
  }

  ngOnInit(): void {
    if (this.angularTreeNodeData.value.collection !== 'Image' && this.angularTreeNodeData.value.value !== undefined) {
      this.genericEditorFrontendProvider.requestObjectNameId(this.angularTreeNodeData.value.collection,
        this.angularTreeNodeData.value.value).then(objectNameId => {
        this.displayObjectNameId(objectNameId);
      }, error => {
        this.messageService.add({
          severity: 'error',
          summary: 'Can not load ObjectNameId from server' + error,
          sticky: true
        })
      })
    }
  }

  getImgUrl(): string {
    return getImageUrl(this.angularTreeNodeData.value.value);
  }

  onShowImageGallery() {
    this.http.get<ImageGalleryItem[]>(URL_IMAGE + "/image-gallery").subscribe(
      value => this.imageGalleryItems = value,
      error => {
        this.messageService.add({
          severity: 'error',
          summary: 'Can not load image gallery from server' + error,
          sticky: true
          })
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
    this.angularTreeNodeData.setValue(objectNameId.id);
    this.showObjectNameIdGallery = false;
    this.angularTreeNodeData.value.value = objectNameId;
    this.displayObjectNameId(objectNameId);
  }

  onObjectNameIdDeleted() {
    this.angularTreeNodeData.setValue(null);
    this.angularTreeNodeData.value.value = undefined;
    this.objectNameIdString = '';
  }

  onShowCollectionGallery() {
    this.genericEditorFrontendProvider.requestObjectNameIds(this.angularTreeNodeData.value.collection)
      .then(value => {
          this.objectNameIds = value;
          this.sortField = 'internalName';
          this.sortOrder = 1;
        },
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

  private displayObjectNameId(objectNameId: ObjectNameId) {
    this.objectNameIdString = `${objectNameId.internalName} (${objectNameId.id})`
  }

}
