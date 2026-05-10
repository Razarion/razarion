import {Component} from '@angular/core';
import {Checkbox} from 'primeng/checkbox';
import {FormsModule} from '@angular/forms';
import {UiSettingsService} from '../../ui-settings.service';

@Component({
  selector: 'settings',
  templateUrl: 'settings.component.html',
  imports: [Checkbox, FormsModule],
})
export class SettingsComponent {
  constructor(public uiSettingsService: UiSettingsService) {}

  get showUnitNames(): boolean {
    return this.uiSettingsService.unitNamesVisible;
  }

  set showUnitNames(value: boolean) {
    this.uiSettingsService.unitNamesVisible = value;
  }
}
