import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import {Button} from 'primeng/button';
import {BaseItemTypeComponent} from '../base-item-type/base-item-type.component';
import {TableModule} from 'primeng/table';

@Component({
  selector: 'base-item-types',
  imports: [
    Button,
    BaseItemTypeComponent,
    TableModule
  ],
  templateUrl: './base-item-types.component.html'
})
export class BaseItemTypesComponent implements OnInit {
  @Input("baseItemTypeIds")
  baseItemTypeIds: number[] = [];
  @Output()
  baseItemTypeIdsChange = new EventEmitter<number[]>();
  tableData: { baseItemTypeId?: number }[] = [];

  ngOnInit(): void {
    if (this.baseItemTypeIds) {
      this.baseItemTypeIds.forEach(baseItemTypeId => {
        this.tableData.push({ baseItemTypeId: baseItemTypeId });
      });
    }
  }

  onCreate() {
    this.tableData.push({ baseItemTypeId: undefined });
    this.onChange();
  }

  onDelete(tableEntry: { baseItemTypeId?: number }) {
    this.tableData.splice(this.tableData.indexOf(tableEntry), 1);
    this.onChange();
  }

  onChange() {
    let baseItemTypeIds: number[] = []
    this.tableData.forEach(tableEntry => {
      if (tableEntry.baseItemTypeId || tableEntry.baseItemTypeId === 0) {
        baseItemTypeIds.push(tableEntry.baseItemTypeId);
      }
    });
    this.baseItemTypeIdsChange.emit(baseItemTypeIds);
  }
}
