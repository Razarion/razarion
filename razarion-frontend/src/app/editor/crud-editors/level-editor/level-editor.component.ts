import {Component} from '@angular/core';
import {EditorPanel} from '../../editor-model';
import {LevelEditorControllerClient, LevelEntity, LevelUnlockConfig} from 'src/app/generated/razarion-share';
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
  levelEditConfigs: LevelEntity[] = [];

  constructor(httpClient: HttpClient) {
    super();
    this.levelEditorControllerClient = new LevelEditorControllerClient(TypescriptGenerator.generateHttpClientAdapter(httpClient))
    this.levelEditorControllerClient.readAll().then(levelConfigs => {
      this.levelEditConfigs = levelConfigs;
      this.sort();
    });

  }

  onCreate() {
    this.levelEditorControllerClient.create().then(levelEditConfig => {
      this.levelEditConfigs.push(levelEditConfig);
    });
  }

  onRemove(levelEditConfig: LevelEntity) {
    this.levelEditorControllerClient.delete(levelEditConfig.id).then(() => {
      this.levelEditConfigs.splice(this.levelEditConfigs.indexOf(levelEditConfig), 1);
    });
  }

  onCreateUnlock(levelEditConfig: LevelEntity) {
    // TODO levelEditConfig.levelUnlockConfigs.push(new class implements LevelUnlockConfig {
    //   id = <any>null;
    //   internalName = <any>null
    //   thumbnail = null;
    //   i18nName = <any>null
    //   i18nDescription = <any>null
    //   baseItemType = null;
    //   baseItemTypeCount = 1;
    //   crystalCost = 1;
    // });
    throw new Error("... TODO ...");
  }

  onRemoveUnlock(levelEditConfig: LevelEntity, levelUnlockConfig: LevelUnlockConfig) {
    // TODO levelEditConfig.levelUnlockConfigs.splice(levelEditConfig.levelUnlockConfigs.indexOf(levelUnlockConfig), 1);
    throw new Error("... TODO ...");
  }

  onSave() {
    this.levelEditConfigs.forEach(levelConfig => {
      this.levelEditorControllerClient.update(levelConfig);
    });
  }

  sort() {
    this.levelEditConfigs.sort((a, b) => a.number - b.number);
    this.levelEditConfigs = [...this.levelEditConfigs];
  }
}
