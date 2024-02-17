import { Component } from '@angular/core';
import { EditorPanel } from '../../editor-model';
import { I18nString, LevelConfig, LevelEditConfig, LevelEditorControllerClient, LevelUnlockConfig } from 'src/app/generated/razarion-share';
import { TypescriptGenerator } from 'src/app/backend/typescript-generator';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'level-editor',
  templateUrl: './level-editor.component.html'
})
export class LevelEditorComponent extends EditorPanel {
  private levelEditorControllerClient!: LevelEditorControllerClient;
  levelEditConfigs: LevelEditConfig[] = [];

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

  onRemove(levelEditConfig: LevelEditConfig) {
    this.levelEditorControllerClient.delete(levelEditConfig.id).then(() => {
      this.levelEditConfigs.splice(this.levelEditConfigs.indexOf(levelEditConfig), 1);
    });
  }

  onCreateUnlock(levelEditConfig: LevelEditConfig) {
    levelEditConfig.levelUnlockConfigs.push(new class implements LevelUnlockConfig {
      id = <any>null;
      internalName = <any>null
      thumbnail = null;
      i18nName = <any>null
      i18nDescription = <any>null
      baseItemType = null;
      baseItemTypeCount = 1;
      crystalCost = 1;

    });
  }

  onRemoveUnlock(levelEditConfig: LevelEditConfig, levelUnlockConfig: LevelUnlockConfig) {
    levelEditConfig.levelUnlockConfigs.splice(levelEditConfig.levelUnlockConfigs.indexOf(levelUnlockConfig), 1);
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
