import {Component} from '@angular/core';
import {SLOPE_EDITOR_PATH} from "../../../common";
import {SlopeConfig} from "../../../generated/razarion-share";


@Component({
  selector: 'slope-editor',
  templateUrl: './slope-editor.component.html'
})
export class SlopeEditorComponent {
  public static readonly editorUrl = SLOPE_EDITOR_PATH;
  configObject!: SlopeConfig;

  init(configObject: any) {
    this.configObject = configObject;
  }
}
