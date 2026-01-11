import {Component, OnInit} from '@angular/core';
import {EditorPanel} from '../editor-model';
import {DbPropertiesEditorControllerClient, DbPropertyConfig, DbPropertyType} from 'src/app/generated/razarion-share';
import {HttpClient} from '@angular/common/http';
import {TypescriptGenerator} from 'src/app/backend/typescript-generator';
import {MessageService} from 'primeng/api';
import {Button} from 'primeng/button';
import {ImageItemComponent} from '../common/image-item/image-item.component';
import {InputNumber} from 'primeng/inputnumber';
import {BabylonMaterialComponent} from '../common/babylon-material/babylon-material.component';
import {TableModule} from 'primeng/table';
import {FormsModule} from '@angular/forms';


@Component({
  selector: 'property-editor',
  imports: [
    Button,
    ImageItemComponent,
    InputNumber,
    BabylonMaterialComponent,
    TableModule,
    FormsModule
],
  templateUrl: './property-editor.component.html'
})
export class PropertyEditorComponent extends EditorPanel implements OnInit {
  protected readonly AUDIO = DbPropertyType.AUDIO;
  protected readonly BABYLON_MATERIAL = DbPropertyType.BABYLON_MATERIAL;
  protected readonly COLOR = DbPropertyType.COLOR;
  protected readonly INTEGER = DbPropertyType.INTEGER;
  protected readonly DOUBLE = DbPropertyType.DOUBLE;
  protected readonly IMAGE = DbPropertyType.IMAGE;
  protected readonly UNKNOWN = DbPropertyType.UNKNOWN;
  private dbPropertiesEditorControllerClient: DbPropertiesEditorControllerClient;
  dbPropertyConfigs: DbPropertyConfig[] = [];

  constructor(httpClient: HttpClient,
              private messageService: MessageService) {
    super();
    this.dbPropertiesEditorControllerClient = new DbPropertiesEditorControllerClient(TypescriptGenerator.generateHttpClientAdapter(httpClient));
  }

  ngOnInit(): void {
    this.dbPropertiesEditorControllerClient.readAllProperties()
      .then(dbPropertyConfigs => this.dbPropertyConfigs = dbPropertyConfigs)
      .catch((error) => this.messageService.add({
        severity: 'error',
        summary: `${error.name}: ${error.status}`,
        detail: `${error.statusText}`,
        sticky: true
      }));
  }

  onSave(dbPropertyConfig: DbPropertyConfig) {
    this.dbPropertiesEditorControllerClient.updateProperty(dbPropertyConfig)
      .then(() => {
        this.messageService.add({
          severity: 'success',
          life: 300,
          summary: 'Saved successful'
        });
      })
      .catch((error) => this.messageService.add({
        severity: 'error',
        summary: `${error.name}: ${error.status}`,
        detail: `${error.statusText}`,
        sticky: true
      }));
  }
}
