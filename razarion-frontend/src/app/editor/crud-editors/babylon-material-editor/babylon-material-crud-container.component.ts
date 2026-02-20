import {Component, ComponentFactoryResolver} from '@angular/core';
import {MessageService} from 'primeng/api';
import {HttpClient} from '@angular/common/http';
import {GeneratedCrudContainerComponent} from '../crud-container/generated-crud-container.component';
import {BabylonMaterialControllerClient, MaterialSizeInfo} from 'src/app/generated/razarion-share';
import {TypescriptGenerator} from 'src/app/backend/typescript-generator';
import {MenubarModule} from 'primeng/menubar';
import {TableModule} from 'primeng/table';
import {ButtonModule} from 'primeng/button';
import {Divider} from 'primeng/divider';

@Component({
  selector: 'babylon-material-crud-container',
  imports: [
    MenubarModule,
    TableModule,
    ButtonModule,
    Divider
  ],
  template: `
    <div style="width: 40em; margin-bottom: 1em;">
      <p-divider align="left">
        <div class="inline-flex items-center">
          <i class="pi pi-database mr-2"></i>
          <b>All material sizes</b>
        </div>
      </p-divider>
      <div class="grid grid-cols-12 gap-4 grid-razarion-editor">
        <span class="col-span-4">Reload sizes</span>
        <div class="col-span-8">
          <p-button icon="pi pi-refresh" (onClick)="loadMaterialSizes()" [loading]="materialSizesLoading"></p-button>
        </div>
      </div>
      @if (materialSizes.length > 0) {
        <p-table [value]="materialSizes" [scrollable]="true" scrollHeight="400px"
                 styleClass="p-datatable-sm p-datatable-striped"
                 [sortField]="'size'" [sortOrder]="-1">
          <ng-template #header>
            <tr>
              <th pSortableColumn="id" style="width: 5em">Id <p-sortIcon field="id"/></th>
              <th pSortableColumn="name">Name <p-sortIcon field="name"/></th>
              <th pSortableColumn="size" style="width: 8em">Size <p-sortIcon field="size"/></th>
            </tr>
          </ng-template>
          <ng-template #body let-entry>
            <tr>
              <td>{{ entry.id }}</td>
              <td>{{ entry.name }}</td>
              <td>{{ formatSize(entry.size) }}</td>
            </tr>
          </ng-template>
        </p-table>
      }
    </div>
    <p-menubar class="mb-0" [model]="items"></p-menubar>
    <ng-template #configContainer></ng-template>
  `
})
export class BabylonMaterialCrudContainerComponent extends GeneratedCrudContainerComponent {
  materialSizes: MaterialSizeInfo[] = [];
  materialSizesLoading = false;
  private materialControllerClient: BabylonMaterialControllerClient;

  constructor(messageService: MessageService,
              httpClient: HttpClient,
              resolver: ComponentFactoryResolver) {
    super(messageService, httpClient, resolver);
    this.materialControllerClient = new BabylonMaterialControllerClient(TypescriptGenerator.generateHttpClientAdapter(httpClient));
    this.loadMaterialSizes();
  }

  loadMaterialSizes() {
    this.materialSizesLoading = true;
    this.materialSizes = [];
    this.materialControllerClient.getMaterialSizes()
      .then(sizes => {
        this.materialSizes = sizes;
        this.materialSizesLoading = false;
      })
      .catch(e => {
        console.error(e);
        this.materialSizesLoading = false;
      });
  }

  formatSize(bytes: number): string {
    if (bytes < 0) return 'Error';
    if (bytes < 1024) return `${bytes} B`;
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
    return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
  }
}
