import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { I18nString } from 'src/app/generated/razarion-share';

@Component({
    selector: 'i18n',
    templateUrl: './i18n.component.html'
})
export class I18nComponent implements OnInit {
  @Input("i18n")
  i18n: I18nString | null = null;
  @Output()
  i18nChange = new EventEmitter<I18nString>();

  displayString: string = ""

  constructor() { }

  ngOnInit(): void {
    if (this.i18n) {
      this.displayString = this.i18n.string;
    }
  }

  onChange() {
    this.i18n = {
      string: this.displayString
    };
    this.i18nChange.emit(this.i18n);

  }
}
