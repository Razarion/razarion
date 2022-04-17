import {ComponentFactoryResolver, Injectable, Type} from "@angular/core";
import {StringPropertyEditorComponent} from "./property-table/editors/string-property-editor.component";
import {IntegerPropertyEditorComponent} from "./property-table/editors/integer-property-editor.component";
import {EnumPropertyEditorComponent} from "./property-table/editors/enum-property-editor.component";
import {IntegerMapPropertyEditorComponent} from "./property-table/editors/integer-map-property-editor.component";
import {DecimalPositionPropertyEditorComponent} from "./property-table/editors/decimal-position-property-editor.component";
import {DoublePropertyEditorComponent} from "./property-table/editors/double-property-editor.component";
import {CollectionReferencePropertyEditorComponent} from "./property-table/editors/collection-reference-property-editor.component";
import {BooleanPropertyEditorComponent} from "./property-table/editors/boolean-property-editor.component";
import {PlaceConfigPropertyEditorComponent} from "./property-table/editors/place-config-property-editor.component";
import {Rectangle2dPropertyEditorComponent} from "./property-table/editors/rectangle-2d-property-editor.component";
import {RectanglePropertyEditorComponent} from "./property-table/editors/rectangle-property-editor.component";
import {VertexPropertyEditorComponent} from "./property-table/editors/vertex-property-editor.component";
import {IndexPropertyEditorComponent} from "./property-table/editors/index-property-editor.component";
import {ColladaStringPropertyEditorComponent} from "./property-table/editors/collada-string-property-editor.component";
import {I18nStringPropertyEditorComponent} from "./property-table/editors/i18n-string-property-editor.component";
import {Polygon2dPropertyEditorComponent} from "./property-table/editors/polygon-2d-property-editor.component";
import {ImagePropertyEditorComponent} from "./property-table/editors/image-property-editor.component";

@Injectable()
export class EditorService {
  private propertyEditorComponents = new Map();

  constructor(private componentFactoryResolver: ComponentFactoryResolver) {
    this.registerPropertyEditorComponent([
      StringPropertyEditorComponent,
      IntegerPropertyEditorComponent,
      EnumPropertyEditorComponent,
      IntegerMapPropertyEditorComponent,
      DecimalPositionPropertyEditorComponent,
      IndexPropertyEditorComponent,
      DoublePropertyEditorComponent,
      CollectionReferencePropertyEditorComponent,
      BooleanPropertyEditorComponent,
      PlaceConfigPropertyEditorComponent,
      Rectangle2dPropertyEditorComponent,
      RectanglePropertyEditorComponent,
      VertexPropertyEditorComponent,
      ColladaStringPropertyEditorComponent,
      I18nStringPropertyEditorComponent,
      Polygon2dPropertyEditorComponent,
      ImagePropertyEditorComponent]);
  }

  getPropertyEditorComponent<T>(selector: string): Type<T> {
    let propertyEditorComponent = this.propertyEditorComponents.get(selector);
    if (propertyEditorComponent == null) {
      throw new TypeError(`No PropertyEditorComponent for ${selector}`);
    }
    return propertyEditorComponent;
  }

  private registerPropertyEditorComponent<T>(propertyEditorComponents: Type<T>[]): void {
    propertyEditorComponents.forEach(propertyEditorComponent => {
      let factory = this.componentFactoryResolver.resolveComponentFactory(propertyEditorComponent);
      this.propertyEditorComponents.set(factory.selector, propertyEditorComponent);
    })
  }
}
