import {AfterViewInit, Component} from '@angular/core';
import {AngularTreeNodeData} from "../../../gwtangular/GwtAngularFacade";

@Component({
  selector: 'image-property-editor',
  template:
    `
      <img style="height: 50px; width: 50px;" src="{{imageUrl}}" alt="Show Image Gallery">
    `
})
export class ImagePropertyEditorComponent implements AfterViewInit {
  angularTreeNodeData!: AngularTreeNodeData;
  imageUrl!: string;

  ngAfterViewInit(): void {
    this.imageUrl = this.angularTreeNodeData.value.src;
  }

}
