import {Component, Input} from '@angular/core';
import {AngularTreeNodeData, GwtAngularPropertyTable} from "../../gwtangular/GwtAngularFacade";
import {SortEvent, TreeNode} from "primeng/api";

@Component({
  selector: 'property-table',
  templateUrl: './property-table.component.html',
  styleUrls: ['./property-table.component.scss']
})
export class PropertyTableComponent {
  @Input("angular-tree-node-data")
  gwtAngularPropertyTable: GwtAngularPropertyTable | null = null;
  cols = [
    {field: 'name', header: 'Name'},
    {field: 'value', header: 'Value'}
  ];

  constructor() {
  }

  sortPropertyNames(event: SortEvent) {
    if (event.data == undefined) {
      return;
    }
    event.data.sort((node1: TreeNode, node2: TreeNode) => {
      const data1: AngularTreeNodeData = node1.data
      const data2: AngularTreeNodeData = node2.data

      if (!data1.canHaveChildren && data2.canHaveChildren) {
        return -1;
      } else if (data1.canHaveChildren && !data2.canHaveChildren) {
        return 1;
      }
      return (data1.name < data2.name ? -1 : (data1.name > data2.name ? 1 : 0));
    });
  }
}
