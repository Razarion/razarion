import {Component, ComponentFactoryResolver} from '@angular/core';
import {MessageService} from 'primeng/api';
import {HttpClient} from '@angular/common/http';
import {GeneratedCrudContainerComponent} from '../crud-container/generated-crud-container.component';
import {MaterialSizeInfo, ParticleSystemControllerClient} from 'src/app/generated/razarion-share';
import {TypescriptGenerator} from 'src/app/backend/typescript-generator';
import {MenubarModule} from 'primeng/menubar';
import {TableModule} from 'primeng/table';
import {ButtonModule} from 'primeng/button';
import {Divider} from 'primeng/divider';

@Component({
  selector: 'particle-system-crud-container',
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
          <b>All particle sizes</b>
        </div>
      </p-divider>
      <div class="grid grid-cols-12 gap-4 grid-razarion-editor">
        <span class="col-span-4">Reload sizes</span>
        <div class="col-span-8">
          <p-button icon="pi pi-refresh" (onClick)="loadParticleSizes()" [loading]="particleSizesLoading"></p-button>
        </div>
      </div>
      @if (particleSizes.length > 0) {
        <p-table [value]="particleSizes" [scrollable]="true" scrollHeight="400px"
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
export class ParticleSystemCrudContainerComponent extends GeneratedCrudContainerComponent {
  particleSizes: MaterialSizeInfo[] = [];
  particleSizesLoading = false;
  private particleSystemControllerClient: ParticleSystemControllerClient;

  constructor(messageService: MessageService,
              httpClient: HttpClient,
              resolver: ComponentFactoryResolver) {
    super(messageService, httpClient, resolver);
    this.particleSystemControllerClient = new ParticleSystemControllerClient(TypescriptGenerator.generateHttpClientAdapter(httpClient));
    this.loadParticleSizes();
  }

  loadParticleSizes() {
    this.particleSizesLoading = true;
    this.particleSizes = [];
    this.particleSystemControllerClient.getParticleSizes()
      .then(sizes => {
        this.particleSizes = sizes;
        this.particleSizesLoading = false;
      })
      .catch(e => {
        console.error(e);
        this.particleSizesLoading = false;
      });
  }

  formatSize(bytes: number): string {
    if (bytes < 0) return 'Error';
    if (bytes < 1024) return `${bytes} B`;
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
    return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
  }
}
