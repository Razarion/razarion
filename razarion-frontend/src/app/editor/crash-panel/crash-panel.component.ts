import {Component} from "@angular/core";
import {GwtAngularService} from "../../gwtangular/GwtAngularService";
import {EditorPanel} from "../editor-model";
import {Alarm} from "../../gwtangular/GwtAngularFacade";
import {TableModule} from 'primeng/table';
import {Button} from 'primeng/button';
import {DatePipe} from '@angular/common';

@Component({
  selector: 'crash-panel',
  imports: [
    TableModule,
    Button,
    DatePipe
  ],
  templateUrl: 'crash-panel.component.html'
})
export class CrashPanelComponent extends EditorPanel {
  clientAlarms!: Alarm[];
  serverAlarms!: Alarm[];

  constructor(private gwtAngularService: GwtAngularService) {
    super();
  }

  override onEditorModel() {
    this.clientAlarms = this.gwtAngularService.gwtAngularFacade.statusProvider.getClientAlarms();
    this.gwtAngularService.gwtAngularFacade.statusProvider.requestServerAlarms().then(alarms => this.serverAlarms = alarms);
  }
}
