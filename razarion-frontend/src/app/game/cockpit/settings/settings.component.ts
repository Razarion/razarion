import {Component} from '@angular/core';
import {Checkbox} from 'primeng/checkbox';
import {FormsModule} from '@angular/forms';
import {Dialog} from 'primeng/dialog';
import {Button} from 'primeng/button';
import {UiSettingsService} from '../../ui-settings.service';
import {GwtAngularService} from '../../../gwtangular/GwtAngularService';
import {CockpitDisplayService} from '../cockpit-display.service';

@Component({
  selector: 'settings',
  templateUrl: 'settings.component.html',
  imports: [Checkbox, FormsModule, Dialog, Button],
})
export class SettingsComponent {
  showSurrenderWarning = false;

  constructor(public uiSettingsService: UiSettingsService,
              private gwtAngularService: GwtAngularService,
              private cockpitDisplayService: CockpitDisplayService) {}

  get showUnitNames(): boolean {
    return this.uiSettingsService.unitNamesVisible;
  }

  set showUnitNames(value: boolean) {
    this.uiSettingsService.unitNamesVisible = value;
  }

  get showTips(): boolean {
    return this.uiSettingsService.tipsVisible;
  }

  set showTips(value: boolean) {
    this.uiSettingsService.tipsVisible = value;
  }

  get showQuestVisualization(): boolean {
    return this.uiSettingsService.questVisualizationVisible;
  }

  set showQuestVisualization(value: boolean) {
    this.uiSettingsService.questVisualizationVisible = value;
  }

  surrenderBase(): void {
    this.showSurrenderWarning = false;
    this.cockpitDisplayService.showSettingsDialog = false;
    this.gwtAngularService.gwtAngularFacade.itemCockpitBridge.surrenderBase();
  }
}
