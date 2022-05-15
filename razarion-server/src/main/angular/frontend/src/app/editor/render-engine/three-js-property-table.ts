import {TreeNode} from "primeng/api";
import {AngularTreeNodeData, GwtAngularPropertyTable} from "../../gwtangular/GwtAngularFacade";
import {Group, Object3D, RepeatWrapping, Texture} from "three";

const IGNORED_THREE_JS_OBJECT_PROPERTIES: string[] = ["parent", "children", "up", "_listeners", "_onChangeCallback"];

// todo const READONLY_THREE_JS_OBJECT_PROPERTIES: string[] = ["uuid", "type"];

function setupCreateOptionLabels(createOptions: any[]) {
  let createOptionLabels = []
  for (let i = 0; i < createOptions.length; i++) {
    createOptionLabels.push({"label": createOptions[i].name, "value": i});
  }
  return createOptionLabels;
}

export class ThreeJsPropertyTable {
  createOptions = [
    {
      name: 'Texture',
      exec(): any {
        let texture = new Texture();
        texture.image = new Image();
        texture.wrapS = RepeatWrapping;
        texture.wrapT = RepeatWrapping;
        return texture
      }
    },
    {
      name: 'Group',
      exec(): any {
        return new Group();
      }
    }
  ]

  createOptionLabels = setupCreateOptionLabels(this.createOptions);
  private rootTreeNodes: TreeNode<AngularTreeNodeData>[] = [];

  constructor(object3D: Object3D) {
    this.recursivelyAddProperty(object3D, null, null, this.rootTreeNodes);
  }

  private static getSpecialSelector(property: any): string | null {
    switch (property.constructor.name) {
      case 'HTMLImageElement':
      case 'ImageBitmap':
        return 'image-property-editor';
    }
    return null;
  }

  private static setupPropertyEditorSelector(property: any): string {
    if (property == null) {
      return ''
    }
    switch (typeof property) {
      case 'number':
        return 'double-property-editor';
      case 'boolean':
        return 'boolean-property-editor';
      case 'string':
        return 'string-property-editor';
    }
    return ''
  }

  getRootTreeNodes(): TreeNode<AngularTreeNodeData>[] {
    return this.rootTreeNodes;
  }

  private recursivelyAddProperty(object3D: Object3D, parentProperty: any, parentName: string | null, treeNodes: TreeNode[]) {
    const _this = this;
    Object.keys(object3D).forEach(function (name) {
      if (IGNORED_THREE_JS_OBJECT_PROPERTIES.includes(name)) {
        return;
      }
      const property = (<any>object3D)[name];
      if (typeof property === "object") {
        if (property && (!Array.isArray(property) || property.length > 0)) {
          _this.addObjectProperty(property, object3D, name, parentProperty, parentName, treeNodes);
        } else {
          _this.addNullObjectProperty(object3D, name, treeNodes);
        }
      } else {
        _this.addPrimitiveProperty(property, object3D, name, treeNodes);
      }
    });
  }

  private addObjectProperty(property: any, object3D: Object3D, name: string, parentProperty: any, parentName: string | null, treeNodes: TreeNode[]) {
    const _this = this;
    let specialSelector = ThreeJsPropertyTable.getSpecialSelector(property);
    if (specialSelector != null) {
      treeNodes.push(new class implements TreeNode<AngularTreeNodeData> {
        data = new class implements AngularTreeNodeData {
          canHaveChildren: boolean = false;
          createAllowed: boolean = false;
          deleteAllowed: boolean = true;
          name: string = name;
          nullable: boolean = false;
          options: string[] = [];
          propertyEditorSelector: string = specialSelector != null ? specialSelector : 'Stupid typescript case';
          value: any = property;

          onCreate(gwtAngularPropertyTable: GwtAngularPropertyTable, createOption: any): void {
          }

          onDelete(gwtAngularPropertyTable: GwtAngularPropertyTable): void {
          }

          setValue(value: any): void {
            if ((<any>object3D).isTexture) {
              let image = new Image();
              image.src = value;
              const texture = (<any>object3D).clone();
              texture.image = image;
              texture.needsUpdate = true;
              (<any>parentProperty)[<string>parentName] = texture;
            } else {
              (<any>object3D)[name] = value;
            }
          }

        }
      });
    } else {
      const childTreeNodes: TreeNode[] = [];
      _this.recursivelyAddProperty(property, object3D, name, childTreeNodes);
      treeNodes.push(new class implements TreeNode<AngularTreeNodeData> {
        children = childTreeNodes;
        data = new class implements AngularTreeNodeData {
          canHaveChildren: boolean = true;
          createAllowed: boolean = false;
          deleteAllowed: boolean = true;
          // name: string = `${name} '${property.constructor.name}'`;
          name: string = name;
          nullable: boolean = false;
          options: string[] = [];
          propertyEditorSelector: string = '';
          value: any;

          onCreate(gwtAngularPropertyTable: GwtAngularPropertyTable, createOption: any): void {
          }

          onDelete(gwtAngularPropertyTable: GwtAngularPropertyTable): void {
          }

          setValue(value: any): void {
          }

        }
      });
    }
  }

  private addNullObjectProperty(object3D: Object3D, name: string, treeNodes: TreeNode[]) {
    let _this = this;
    treeNodes.push(new class implements TreeNode<AngularTreeNodeData> {
      data = new class implements AngularTreeNodeData {
        canHaveChildren: boolean = true;
        createAllowed: boolean = true;
        deleteAllowed: boolean = false;
        name: string = name;
        nullable: boolean = false;
        options: string[] = [];
        propertyEditorSelector: string = '';
        value: any;
        createOptions: any = _this.createOptionLabels;

        onCreate(gwtAngularPropertyTable: GwtAngularPropertyTable, createOption: any): void {
          (<any>object3D)[name] = _this.createOptions[createOption].exec();
        }

        onDelete(gwtAngularPropertyTable: GwtAngularPropertyTable): void {
        }

        setValue(value: any): void {
        }

      }
    });
  }

  private addPrimitiveProperty(property: any, object3D: Object3D, name: string, treeNodes: TreeNode[]) {
    treeNodes.push(new class implements TreeNode<AngularTreeNodeData> {
      data = new class implements AngularTreeNodeData {
        canHaveChildren: boolean = false;
        createAllowed: boolean = false;
        deleteAllowed: boolean = false;
        name: string = name;
        nullable: boolean = false;
        options: string[] = [];
        propertyEditorSelector: string = ThreeJsPropertyTable.setupPropertyEditorSelector(property);
        value: any = property;

        onCreate(gwtAngularPropertyTable: GwtAngularPropertyTable, createOption: any): void {
        }

        onDelete(gwtAngularPropertyTable: GwtAngularPropertyTable): void {
        }

        setValue(value: any): void {
          (<any>object3D)[name] = value;
        }

      }
    });
  }
}
