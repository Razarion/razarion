import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {MessageService} from "primeng/api";
import {TableModule} from 'primeng/table';
import {BaseItemTypeComponent} from '../base-item-type/base-item-type.component';
import {FormsModule} from '@angular/forms';
import {ButtonModule} from 'primeng/button';

import {BadgeModule} from 'primeng/badge';

@Component({
  selector: 'base-item-type-count',
  templateUrl: './base-item-type-count.component.html',
  imports: [
    TableModule,
    BaseItemTypeComponent,
    FormsModule,
    ButtonModule,
    BadgeModule
],
  styleUrls: ['./base-item-type-count.component.scss']
})
export class BaseItemTypeCountComponent implements OnInit {
  @Input("baseItemTypeCount")
  baseItemTypeCount?: { [index: string]: number };
  @Input("previousBaseItemTypeCount")
  previousBaseItemTypeCount?: { [index: string]: number };
  // Aggregated max count buildable via unlock per baseItemTypeId. Read-only, only shown when set.
  @Input("unlockBaseItemTypeCount")
  unlockBaseItemTypeCount?: { [index: string]: number };
  @Output()
  baseItemTypeCountChange = new EventEmitter<{ [index: string]: number }>();
  baseItemTypeCountArray: { baseItemTypeId: number, count: number, readonly: boolean }[] = [];

  constructor(private messageService: MessageService) {
  }

  ngOnInit(): void {
    this.baseItemTypeCountArray = [];
    if (this.baseItemTypeCount) {
      Object.keys(this.baseItemTypeCount).forEach(id => {
        this.baseItemTypeCountArray.push({baseItemTypeId: parseInt(id), count: this.baseItemTypeCount![id], readonly: false});
      })
    }
    this.appendUnlockOnlyRows();
  }

  onChange() {
    let baseItemTypeCountChange: { [index: string]: number } = {};
    this.baseItemTypeCountArray.forEach(value => {
      if (value.readonly) {
        return;
      }
      let intValue = value.count;
      if (isNaN(intValue)) {
        this.messageService.add({
          severity: 'Invalid number',
          summary: `Invalid number: ${value.count}`,
          sticky: true
        });
        return;
      }
      if (intValue > 0 && value.baseItemTypeId) {
        baseItemTypeCountChange[value.baseItemTypeId] = intValue;
      }
    })
    this.baseItemTypeCountChange.emit(baseItemTypeCountChange);
  }

  onDelete(baseItemTypeId: number) {
    this.baseItemTypeCountArray.splice(this.baseItemTypeCountArray.findIndex(e => e.baseItemTypeId === baseItemTypeId), 1);
    this.onChange();
  }

  onCreate() {
    this.baseItemTypeCountArray.push({baseItemTypeId: NaN, count: 1, readonly: false})
    this.onChange();
  }

  onImport() {
    const merged = {...this.baseItemTypeCount, ...this.previousBaseItemTypeCount};
    this.baseItemTypeCountArray = Object.keys(merged).map(id => ({
      baseItemTypeId: parseInt(id, 10),
      count: merged[id],
      readonly: false
    }));
    this.appendUnlockOnlyRows();

    this.onChange();
  }

  // Adds read-only rows for baseItemTypes that are only reachable via unlock and have no
  // editable itemTypeLimitation entry yet, so their unlock-buildable count is still visible.
  private appendUnlockOnlyRows() {
    if (!this.unlockBaseItemTypeCount) {
      return;
    }
    const existing = new Set(
      this.baseItemTypeCountArray.filter(e => !e.readonly).map(e => e.baseItemTypeId));
    Object.keys(this.unlockBaseItemTypeCount).forEach(id => {
      const baseItemTypeId = parseInt(id, 10);
      if (!existing.has(baseItemTypeId)) {
        this.baseItemTypeCountArray.push({baseItemTypeId, count: 0, readonly: true});
      }
    });
  }

  getUnlockCount(baseItemTypeId: number): number {
    if (!this.unlockBaseItemTypeCount) {
      return 0;
    }
    return this.unlockBaseItemTypeCount[baseItemTypeId] ?? 0;
  }

  // Total units buildable at this level: the level limitation plus all accumulated unlocks.
  getTotalCount(typeCount: { baseItemTypeId: number, count: number }): number {
    const limitation = Number(typeCount.count) || 0;
    return limitation + this.getUnlockCount(typeCount.baseItemTypeId);
  }

  getPreviousCount(baseItemTypeId: number): number {
    let found = Object.keys(this.previousBaseItemTypeCount!)
      .filter(typeIdString => baseItemTypeId === parseInt(typeIdString));

    if (found && this.previousBaseItemTypeCount![found[0]]) {
      return this.previousBaseItemTypeCount![found[0]];
    }

    return 0;
  }

}
