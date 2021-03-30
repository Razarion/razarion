import {ComponentFactoryResolver, Injectable, Type} from "@angular/core";
import {StringPropertyEditorComponent} from "./property-table/editors/string-property-editor.component";

@Injectable()
export class EditorService {
  private propertyEditorComponents = new Map();

  constructor(private componentFactoryResolver: ComponentFactoryResolver) {
    this.registerPropertyEditorComponent(StringPropertyEditorComponent);
  }

  getPropertyEditorComponent<T>(selector: string): Type<T> {
    let propertyEditorComponent = this.propertyEditorComponents.get(selector);
    if (propertyEditorComponent == null) {
      throw new TypeError(`No PropertyEditorComponent for ${selector}`);
    }
    return propertyEditorComponent;
  }

  private registerPropertyEditorComponent<T>(propertyEditorComponent: Type<T>): void {
    let factory = this.componentFactoryResolver.resolveComponentFactory(propertyEditorComponent);
    this.propertyEditorComponents.set(factory.selector, propertyEditorComponent);
    console.info(`Register PropertyEditorComponent for selector "${factory.selector}"`);
  }
}
