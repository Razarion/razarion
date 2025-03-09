import {Component} from "@angular/core";
import {GwtAngularService} from "../../gwtangular/GwtAngularService";
import {EditorPanel} from "../editor-model";
import {Alarm} from "../../gwtangular/GwtAngularFacade";

@Component({
    selector: 'crash-panel',
    templateUrl: 'crash-panel.component.html',
    standalone: false
})
export class CrashPanelComponent extends EditorPanel {
  clientAlarms!: Alarm[];
  serverAlarms!: Alarm[];

  constructor(private gwtAngularService: GwtAngularService) {
    super();
  }

  onEditorModel() {
    this.clientAlarms = this.gwtAngularService.gwtAngularFacade.statusProvider.getClientAlarms();
    this.gwtAngularService.gwtAngularFacade.statusProvider.requestServerAlarms().then(alarms => this.serverAlarms = alarms);
  }
}
