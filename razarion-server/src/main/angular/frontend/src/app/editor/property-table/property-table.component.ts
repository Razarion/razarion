import {Component, Input} from '@angular/core';
import {GwtAngularPropertyTable} from "../../gwtangular/GwtAngularFacade";

@Component({
  selector: 'property-table',
  templateUrl: './property-table.component.html',
  styleUrls: ['./property-table.component.scss']
})
export class PropertyTableComponent {
  @Input('gwt-angular-property-table') gwtAngularPropertyTable!: GwtAngularPropertyTable;
}
