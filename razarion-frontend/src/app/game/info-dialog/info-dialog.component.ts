import {Component, EventEmitter, Input, Output} from '@angular/core';
import {Dialog} from 'primeng/dialog';
import {Button} from 'primeng/button';

export type InfoDialogMode = 'info' | 'quest-completed';

@Component({
  selector: 'info-dialog',
  templateUrl: 'info-dialog.component.html',
  styleUrls: ['info-dialog.component.scss'],
  imports: [Dialog, Button]
})
export class InfoDialogComponent {
  @Input() visible = false;
  @Input() mode: InfoDialogMode = 'info';
  @Output() visibleChange = new EventEmitter<boolean>();

  get header(): string {
    return this.mode === 'quest-completed' ? 'Congratulations!' : 'About Razarion';
  }

  onHide(): void {
    this.visible = false;
    this.visibleChange.emit(false);
  }
}
