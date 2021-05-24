import {Component, OnInit} from '@angular/core';
import {AngularTreeNodeData} from "../../../gwtangular/GwtAngularFacade";

@Component({
  selector: 'integer-map-property-editor',
  template: `
    <p-button icon="pi pi-sun"
              styleClass="p-button-rounded p-button-text p-button-sm p-button-success"
              (onClick)="onCreate()">
    </p-button>
    <table style="width: auto">
      <tr *ngFor="let row of rows">
        <td>
          <p-inputNumber [inputStyle]="{width: '5em'}" [ngModel]="row.key" (onInput)="onChangeKey($event, row)"></p-inputNumber>
        </td>
        <td>
          <p-inputNumber [inputStyle]="{width: '5em'}" [ngModel]="row.value" (onInput)="onChangeValue($event, row)"></p-inputNumber>
        </td>
        <td>
          <p-button icon="pi pi-times"
                    styleClass="p-button-rounded p-button-text p-button-sm p-button-danger"
                    (onClick)="onDelete(row)">
          </p-button>
        </td>
      </tr>
    </table>
  `
})
export class IntegerMapPropertyEditorComponent implements OnInit {
  angularTreeNodeData!: AngularTreeNodeData;
  rows: Row[] = [];

  ngOnInit(): void {
    const map: Map<number, number> = this.angularTreeNodeData.value;
    map.forEach((value, key) => {
      this.rows.push(new Row(key, value));
    })
  }

  onChangeKey(event:any, row: Row) {
    console.info(event)
    console.info(this.rows)
    row.key = event.value;
    this.setValue();
  }

  onChangeValue(event:any, row: Row) {
    console.info(event)
    console.info(this.rows)
    row.value = event.value;
    this.setValue();
  }

  onDelete(row: Row) {
    this.rows.splice(this.rows.indexOf(row), 1);
    this.setValue();
  }

  onCreate() {
    this.rows.push(new Row(null, null));
  }

  private setValue() {
    const resultMap: Map<number, number> = new Map;
    for (const row of this.rows) {
      if (row.isValid()) {
        resultMap.set(row.key, row.value);
      }
    }
    this.angularTreeNodeData.setValue(resultMap);
  }
}

export class Row {
  key: number;
  value: number;

  constructor(key: any, value: any) {
    this.key = key;
    this.value = value;
  }

  isValid() {
    return this.key !== undefined && this.key !== null && this.value !== undefined && this.value !== null;
  }
}
