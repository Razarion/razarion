import {ComponentFactoryResolver, Injectable, Type} from "@angular/core";

@Injectable()
export class EditorService {
  private propertyEditorComponents = new Map();

  constructor(private componentFactoryResolver: ComponentFactoryResolver) {
  }

  getPropertyEditorComponent<T>(selector: string): Type<T> {
    let propertyEditorComponent = this.propertyEditorComponents.get(selector);
    if (propertyEditorComponent == null) {
      throw new TypeError(`No PropertyEditorComponent for ${selector}`);
    }
    return propertyEditorComponent;
  }

  registerPropertyEditorComponents<T>(propertyEditorComponents: Type<T>[]): void {
    propertyEditorComponents.forEach(propertyEditorComponent =>
      this.registerPropertyEditorComponent(propertyEditorComponent)
    )
  }

  registerPropertyEditorComponent<T>(propertyEditorComponent: Type<T>): void {
    let factory = this.componentFactoryResolver.resolveComponentFactory(propertyEditorComponent);
    this.propertyEditorComponents.set(factory.selector, propertyEditorComponent);
  }
}
