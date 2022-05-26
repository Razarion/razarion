import {TreeNode} from "primeng/api";
import {AngularTreeNodeData, GwtAngularPropertyTable} from "../../gwtangular/GwtAngularFacade";
import {Group, Object3D, RepeatWrapping, Texture, TextureLoader} from "three";

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
        texture.image.onload = function () {
          texture.needsUpdate = true;
        };
        texture.image.src = "/assets/no-icon.png";
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

  constructor(object3D: Object3D, private updateHandler: () => {}) {
    this.recursivelyAddProperties(object3D, this.rootTreeNodes);
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

  private createObject(name: string, parentProperty: any, createOption: any, treeNode: TreeNode) {
    const property = this.createOptions[createOption].exec();
    parentProperty[name] = property;
    treeNode.children = [];
    this.recursivelyAddProperties(property, treeNode.children);
    parentProperty.needsUpdate = true;
    treeNode.data.createAllowed = false;
    treeNode.data.deleteAllowed = true;
    this.updateHandler();
  }

  private deleteObject(name: string, parentProperty: any, treeNode: TreeNode) {
    parentProperty[name] = null;
    parentProperty.needsUpdate = true
    treeNode.expanded = false;
    treeNode.children = [];
    treeNode.data.createAllowed = true;
    treeNode.data.deleteAllowed = false;
    this.updateHandler();
  }

  private recursivelyAddProperties(property: any, treeNodes: TreeNode[]) {
    const _this = this;
    Object.keys(property).forEach(function (childName) {
      if (IGNORED_THREE_JS_OBJECT_PROPERTIES.includes(childName)) {
        return;
      }
      const childProperty = property[childName];
      if (typeof childProperty === "object") {
        if (childProperty && (!Array.isArray(childProperty) || childProperty.length > 0)) {
          _this.addObjectProperty(childName, childProperty, property, treeNodes);
        } else {
          _this.addNullObjectProperty(childName, property, treeNodes);
        }
      } else {
        _this.addPrimitiveProperty(childName, childProperty, property, treeNodes);
      }
    });
  }

  private addObjectProperty(name: string, property: any, parentProperty: any, treeNodes: TreeNode[]) {
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
            if (name === "image") {
              let image = new Image();
              image.src = value;
              value = image;
            }
            (<any>parentProperty)[name] = value;
            parentProperty.needsUpdate = true;
          }

        }
      });
    } else {
      const childTreeNodes: TreeNode[] = [];
      _this.recursivelyAddProperties(property, childTreeNodes);
      const treeNode = new class implements TreeNode<AngularTreeNodeData> {
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
          createOptions: any = _this.createOptionLabels;

          onCreate(gwtAngularPropertyTable: GwtAngularPropertyTable, createOption: any): void {
            _this.createObject(name, parentProperty, createOption, treeNode);
          }

          onDelete(gwtAngularPropertyTable: GwtAngularPropertyTable): void {
            _this.deleteObject(name, parentProperty, treeNode);
          }

          setValue(value: any): void {
          }

        }
      }
      treeNodes.push(treeNode);
    }
  }

  private addNullObjectProperty(name: string, parentProperty: any, treeNodes: TreeNode[]) {
    let _this = this;
    const treeNode = new class implements TreeNode<AngularTreeNodeData> {
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
          _this.createObject(name, parentProperty, createOption, treeNode);
        }

        onDelete(gwtAngularPropertyTable: GwtAngularPropertyTable): void {
          _this.deleteObject(name, parentProperty, treeNode);
        }

        setValue(value: any): void {
        }

      }
    }
    treeNodes.push(treeNode);
  }

  private addPrimitiveProperty(name: string, property: any, propertyProperty: any, treeNodes: TreeNode[]) {
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
          propertyProperty[name] = value;
        }
      }
    });
  }
}
