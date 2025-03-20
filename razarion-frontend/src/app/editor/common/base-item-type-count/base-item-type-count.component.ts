import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {MessageService} from "primeng/api";

@Component({
    selector: 'base-item-type-count',
    templateUrl: './base-item-type-count.component.html',
    styleUrls: ['./base-item-type-count.component.scss']
})
export class BaseItemTypeCountComponent implements OnInit {
  @Input("baseItemTypeCount")
  baseItemTypeCount?: { [index: string]: number };
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
}
