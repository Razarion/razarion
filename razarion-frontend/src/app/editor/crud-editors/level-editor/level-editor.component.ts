import {Component} from '@angular/core';
import {EditorPanel} from '../../editor-model';
import {LevelEditorControllerClient, LevelEntity, LevelUnlockEntity} from 'src/app/generated/razarion-share';
import {TypescriptGenerator} from 'src/app/backend/typescript-generator';
import {HttpClient} from '@angular/common/http';
import {Button} from 'primeng/button';
import {InputNumber} from 'primeng/inputnumber';
import {FormsModule} from '@angular/forms';
import {ImageItemComponent} from '../../common/image-item/image-item.component';
import {BaseItemTypeComponent} from '../../common/base-item-type/base-item-type.component';
import {BaseItemTypeCountComponent} from '../../common/base-item-type-count/base-item-type-count.component';
import {TableModule} from 'primeng/table';

@Component({
  selector: 'level-editor',
  imports: [
    Button,
    InputNumber,
    FormsModule,
    ImageItemComponent,
    BaseItemTypeComponent,
    BaseItemTypeCountComponent,
    TableModule
  ],
  templateUrl: './level-editor.component.html'
})
export class LevelEditorComponent extends EditorPanel {
  private levelEditorControllerClient!: LevelEditorControllerClient;
  levelEntities: LevelEntity[] = [];

  constructor(httpClient: HttpClient) {
    super();
    this.levelEditorControllerClient = new LevelEditorControllerClient(TypescriptGenerator.generateHttpClientAdapter(httpClient))
    this.levelEditorControllerClient.readAll().then(levelConfigs => {
      this.levelEntities = levelConfigs;
      this.sort();
    });

  }

  onCreate() {
    this.levelEditorControllerClient.create().then(levelEditConfig => {
      this.levelEntities.push(levelEditConfig);
    });
  }

  onRemove(levelEditConfig: LevelEntity) {
    this.levelEditorControllerClient.delete(levelEditConfig.id).then(() => {
      this.levelEntities.splice(this.levelEntities.indexOf(levelEditConfig), 1);
    });
  }

  onCreateUnlock(levelEntity: LevelEntity) {
    if (!levelEntity.levelUnlockEntities) {
      levelEntity.levelUnlockEntities = [];
    }
    levelEntity.levelUnlockEntities.push(new class implements LevelUnlockEntity {
      id = <any>null;
      internalName = <any>null;
      baseItemType = <any>null;
      baseItemTypeCount = 1;
      crystalCost = 1;
      thumbnail = <any>null;
    });
  }

  onRemoveUnlock(levelEntity: LevelEntity, levelUnlockEntity: LevelUnlockEntity) {
    levelEntity.levelUnlockEntities.splice(levelEntity.levelUnlockEntities.indexOf(levelUnlockEntity), 1);
  }

  onSave() {
    this.levelEntities.forEach(levelConfig => {
      this.levelEditorControllerClient.update(levelConfig);
    });
  }

  sort() {
    this.levelEntities.sort((a, b) => a.number - b.number);
    this.levelEntities = [...this.levelEntities];
  }
}
