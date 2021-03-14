import {Component, Input} from '@angular/core';
import {TreeNode} from "primeng/api";

@Component({
  selector: 'property-table',
  templateUrl: './property-table.component.html',
  styleUrls: ['./property-table.component.scss']
})
export class PropertyTableComponent {
  @Input('tree-nodes') treeNodes: TreeNode[] = [];
}
