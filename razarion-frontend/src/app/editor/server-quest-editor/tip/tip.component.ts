import {Component, EventEmitter, Input, Output} from '@angular/core';
import {Tip} from '../../../gwtangular/GwtAngularFacade';
import {TipConfig} from '../../../generated/razarion-share';
import {FormsModule} from '@angular/forms';
import {BaseItemTypeComponent} from '../../common/base-item-type/base-item-type.component';
import {Select} from 'primeng/select';
import {Checkbox} from 'primeng/checkbox';


@Component({
  selector: 'tip',
  imports: [
    FormsModule,
    BaseItemTypeComponent,
    Select,
    Checkbox
],
  templateUrl: './tip.component.html'
})
export class TipComponent {
  tipOptions = Object.values(Tip);
  @Output()
  tipConfigChange = new EventEmitter<TipConfig | null>();
  tipString: string | null = null;
  actorItemTypeId: number | null = null;
  group = false;

  @Input()
  set tipConfig(tipConfig: TipConfig | null) {
    if (tipConfig) {
      this.tipString = tipConfig.tipString;
      this.actorItemTypeId = tipConfig.actorItemTypeId;
      this.group = !!tipConfig.group;
    } else {
      this.tipString = null;
      this.actorItemTypeId = null;
      this.group = false;
    }
  }

  onTipChange() {
    this.onChange();
  }

  onActorItemTypeIdChange() {
    this.onChange();
  }

  onGroupChange() {
    this.onChange();
  }

  private onChange() {
    if (this.tipString) {
      this.tipConfigChange.emit({
        tipString: this.tipString,
        actorItemTypeId: this.actorItemTypeId,
        group: this.group
      });
    } else {
      this.tipConfigChange.emit(null);
    }
  }

}

