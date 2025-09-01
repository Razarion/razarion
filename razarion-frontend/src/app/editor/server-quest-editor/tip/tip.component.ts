import {Component, EventEmitter, Input, Output} from '@angular/core';
import {Tip} from '../../../gwtangular/GwtAngularFacade';
import {DropdownModule} from 'primeng/dropdown';
import {TipConfig} from '../../../generated/razarion-share';
import {FormsModule} from '@angular/forms';
import {BaseItemTypeComponent} from '../../common/base-item-type/base-item-type.component';
import {CommonModule} from '@angular/common';

@Component({
  selector: 'tip',
  imports: [
    DropdownModule,
    FormsModule,
    BaseItemTypeComponent,
    CommonModule
  ],
  templateUrl: './tip.component.html'
})
export class TipComponent {
  tipOptions = Object.values(Tip);
  @Output()
  tipConfigChange = new EventEmitter<TipConfig | null>();
  tipString: string | null = null;
  actorItemTypeId: number | null = null;

  @Input()
  set tipConfig(tipConfig: TipConfig | null) {
    if (tipConfig) {
      this.tipString = tipConfig.tipString;
      this.actorItemTypeId = tipConfig.actorItemTypeId;
    } else {
      this.tipString = null;
      this.actorItemTypeId = null;
    }
  }

  onTipChange() {
    this.onChange();
  }

  onActorItemTypeIdChange() {
    this.onChange();
  }

  private onChange() {
    if (this.tipString) {
      this.tipConfigChange.emit({
        tipString: this.tipString,
        actorItemTypeId: this.actorItemTypeId
      });
    } else {
      this.tipConfigChange.emit(null);
    }
  }

}

