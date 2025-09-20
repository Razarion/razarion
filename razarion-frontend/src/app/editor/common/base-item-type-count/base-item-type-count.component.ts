import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {MessageService} from "primeng/api";
import {TableModule} from 'primeng/table';
import {BaseItemTypeComponent} from '../base-item-type/base-item-type.component';
import {FormsModule} from '@angular/forms';
import {ButtonModule} from 'primeng/button';
import {CommonModule} from '@angular/common';
import {BadgeModule} from 'primeng/badge';

@Component({
  selector: 'base-item-type-count',
  templateUrl: './base-item-type-count.component.html',
  imports: [
    TableModule,
    BaseItemTypeComponent,
    FormsModule,
    ButtonModule,
    CommonModule,
    BadgeModule,
  ],
  styleUrls: ['./base-item-type-count.component.scss']
})
export class BaseItemTypeCountComponent implements OnInit {
  @Input("baseItemTypeCount")
  baseItemTypeCount?: { [index: string]: number };
  @Input("previousBaseItemTypeCount")
  previousBaseItemTypeCount?: { [index: string]: number };
  @Output()
  baseItemTypeCountChange = new EventEmitter<{ [index: string]: number }>();
  baseItemTypeCountArray: { baseItemTypeId: number, count: number }[] = [];

  constructor(private messageService: MessageService) {
  }

  ngOnInit(): void {
    this.baseItemTypeCountArray = [];
    if (this.baseItemTypeCount) {
      Object.keys(this.baseItemTypeCount).forEach(id => {
        this.baseItemTypeCountArray.push({baseItemTypeId: parseInt(id), count: this.baseItemTypeCount![id]});
      })
    }
  }

  onChange() {
    let baseItemTypeCountChange: { [index: string]: number } = {};
    this.baseItemTypeCountArray.forEach(value => {
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
    this.baseItemTypeCountArray.push({baseItemTypeId: NaN, count: 1})
    this.onChange();
  }

  onImport() {
    const merged = {...this.baseItemTypeCount, ...this.previousBaseItemTypeCount};
    this.baseItemTypeCountArray = Object.keys(merged).map(id => ({
      baseItemTypeId: parseInt(id, 10),
      count: merged[id]
    }));

    this.onChange();
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
