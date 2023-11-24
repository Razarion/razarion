import { Component } from '@angular/core';
import { EditorPanel } from '../../editor-model';
import { LevelConfig, LevelEditorControllerClient } from 'src/app/generated/razarion-share';
import { TypescriptGenerator } from 'src/app/backend/typescript-generator';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'level-editor',
  templateUrl: './level-editor.component.html'
})
export class LevelEditorComponent extends EditorPanel {
  private levelEditorControllerClient!: LevelEditorControllerClient;
  levelConfigs: LevelConfig[] = [];

  constructor(httpClient: HttpClient) {
    super();
    this.levelEditorControllerClient = new LevelEditorControllerClient(TypescriptGenerator.generateHttpClientAdapter(httpClient))
    this.levelEditorControllerClient.readAll().then(levelConfigs => {
      this.levelConfigs = levelConfigs;
      this.sort();
    });

  }

  onCreate() {
    this.levelEditorControllerClient.create().then(levelConfig => {
      this.levelConfigs.push(levelConfig);
    });
  }

  onRemove(level: LevelConfig) {
    this.levelEditorControllerClient.delete(level.id).then(() => {
      this.levelConfigs.splice(this.levelConfigs.indexOf(level), 1);
    });
  }

  onSave() {
    this.levelConfigs.forEach(levelConfig => {
      this.levelEditorControllerClient.update(levelConfig);
    });
  }

  sort() {
    this.levelConfigs.sort((a, b) => a.number - b.number);
    this.levelConfigs = [...this.levelConfigs];
  }
}
