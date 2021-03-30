import {
  Component,
  ComponentFactory,
  ComponentFactoryResolver,
  ComponentRef,
  Input,
  OnInit,
  ViewContainerRef
} from '@angular/core';
import {AngularTreeNodeData} from "../../gwtangular/GwtAngularFacade";
import {EditorService} from "../editor-service";
import {StringPropertyEditorComponent} from "./editors/string-property-editor.component";

@Component({
  selector: 'property-editor',
  template: ''
})
export class PropertyEditorComponent implements OnInit {
  @Input('angular-tree-node-data') angularTreeNodeData!: AngularTreeNodeData;

  constructor(private viewContainerRef: ViewContainerRef,
              private editorService: EditorService,
              private componentFactoryResolver: ComponentFactoryResolver) {
  }

  ngOnInit() {
    let componentFactory: ComponentFactory<StringPropertyEditorComponent> = this.componentFactoryResolver.resolveComponentFactory(
      this.editorService.getPropertyEditorComponent(this.angularTreeNodeData.propertyEditorSelector));

    const componentRef: ComponentRef<StringPropertyEditorComponent> = this.viewContainerRef.createComponent(componentFactory, 0, this.viewContainerRef.injector);
    componentRef.instance.angularTreeNodeData = this.angularTreeNodeData;
  }
}
