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
    this.recursivelyAddProperties(new PropertyPath(null, object3D, null), this.rootTreeNodes);
  }

  private static getSpecialSelector(name: string, property: any): string | null {
    if (property.isVector3) {
      return 'vector3-property-editor'
    }
    if (property.isEuler) {
      return 'euler-property-editor'
    }
    // Do not user three.js or own classes here. Webpack uses minifier property.constructor.name is not working.
    switch (property.constructor.name) {
      case 'HTMLImageElement':
      case 'ImageBitmap':
        return 'image-property-editor';
    }
    if (name === 'userData') {
      return 'userdata-property-editor'
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

  private createObject(propertyPath: PropertyPath, createOption: any, treeNode: TreeNode) {
    const property = this.createOptions[createOption].exec();
    propertyPath.setInParent(property);
    treeNode.children = [];
    this.recursivelyAddProperties(propertyPath, treeNode.children);
    propertyPath.updateParent();
    treeNode.data.createAllowed = false;
    treeNode.data.deleteAllowed = true;
    this.updateHandler();
  }

  private deleteObject(propertyPath: PropertyPath, treeNode: TreeNode) {
    propertyPath.setInParent(null);
    propertyPath.updateParent()
    treeNode.expanded = false;
    treeNode.children = [];
    treeNode.data.createAllowed = true;
    treeNode.data.deleteAllowed = false;
    this.updateHandler();
  }

  private recursivelyAddProperties(propertyPath: PropertyPath, treeNodes: TreeNode[]) {
    const _this = this;
    Object.keys(propertyPath.getProperty()).forEach(childName => {
      if (IGNORED_THREE_JS_OBJECT_PROPERTIES.includes(childName)) {
        return;
      }
      const childProperty = propertyPath.getProperty()[childName];
      if (typeof childProperty === "object") {
        if (childProperty && (!Array.isArray(childProperty) || childProperty.length > 0)) {
          _this.addObjectProperty(propertyPath.generateChild(childProperty, childName), treeNodes);
        } else {
          _this.addNullObjectProperty(propertyPath.generateChild(childProperty, childName), treeNodes);
        }
      } else {
        _this.addPrimitiveProperty(propertyPath.generateChild(childProperty, childName), treeNodes);
      }
    });
  }

  private addObjectProperty(propertyPath: PropertyPath, treeNodes: TreeNode[]) {
    const _this = this;
    let specialSelector = ThreeJsPropertyTable.getSpecialSelector(propertyPath.getPropertyName(), propertyPath.getProperty());
    if (specialSelector != null) {
      treeNodes.push(new class implements TreeNode<AngularTreeNodeData> {
        data = new class implements AngularTreeNodeData {
          canHaveChildren: boolean = false;
          createAllowed: boolean = false;
          deleteAllowed: boolean = true;
          name: string = propertyPath.getPropertyName();
          nullable: boolean = false;
          options: string[] = [];
          propertyEditorSelector: string = specialSelector != null ? specialSelector : 'Stupid typescript case';
          value: any = propertyPath.getProperty();

          onCreate(gwtAngularPropertyTable: GwtAngularPropertyTable, createOption: any): void {
          }

          onDelete(gwtAngularPropertyTable: GwtAngularPropertyTable): void {
          }

          setValue(value: any): void {
            if (propertyPath.getParentProperty().isTexture) {
              const newTexture = propertyPath.getParentProperty().clone();
              let image = new Image();
              image.onload = () => {
                newTexture.needsUpdate = true;
                propertyPath.getParent().updateParent();
              };
              image.src = value;
              newTexture.image = image;
              propertyPath.setInGrandParent(newTexture);
            } else if (propertyPath.getGrandParentProperty().isTexture && propertyPath.getParentProperty().isSource) {
              const origTexture = propertyPath.getGrandParentProperty();
              const newTexture = new TextureLoader().load(value);
              newTexture.wrapS = origTexture.wrapS;
              newTexture.wrapT = origTexture.wrapT;
              newTexture.repeat.set(origTexture.repeat.x, origTexture.repeat.y);
              propertyPath.setInGreatGrandParent(newTexture);
            } else {
              propertyPath.setInParent(value);
              propertyPath.updateParent();
            }
          }

        }
      });
    } else {
      const childTreeNodes: TreeNode[] = [];
      _this.recursivelyAddProperties(propertyPath, childTreeNodes);
      const treeNode = new class implements TreeNode<AngularTreeNodeData> {
        children = childTreeNodes;
        data = new class implements AngularTreeNodeData {
          canHaveChildren: boolean = true;
          createAllowed: boolean = false;
          deleteAllowed: boolean = true;
          name: string = propertyPath.getPropertyName();
          nullable: boolean = false;
          options: string[] = [];
          propertyEditorSelector: string = '';
          value: any;
          createOptions: any = _this.createOptionLabels;

          onCreate(gwtAngularPropertyTable: GwtAngularPropertyTable, createOption: any): void {
            _this.createObject(propertyPath, createOption, treeNode);
          }

          onDelete(gwtAngularPropertyTable: GwtAngularPropertyTable): void {
            _this.deleteObject(propertyPath, treeNode);
          }

          setValue(value: any): void {
          }

        }
      }
      treeNodes.push(treeNode);
    }
  }

  private addNullObjectProperty(propertyPath: PropertyPath, treeNodes: TreeNode[]) {
    let _this = this;
    const treeNode = new class implements TreeNode<AngularTreeNodeData> {
      data = new class implements AngularTreeNodeData {
        canHaveChildren: boolean = true;
        createAllowed: boolean = true;
        deleteAllowed: boolean = false;
        name: string = propertyPath.getPropertyName();
        nullable: boolean = false;
        options: string[] = [];
        propertyEditorSelector: string = '';
        value: any;
        createOptions: any = _this.createOptionLabels;

        onCreate(gwtAngularPropertyTable: GwtAngularPropertyTable, createOption: any): void {
          _this.createObject(propertyPath, createOption, treeNode);
        }

        onDelete(gwtAngularPropertyTable: GwtAngularPropertyTable): void {
          _this.deleteObject(propertyPath, treeNode);
        }

        setValue(value: any): void {
        }

      }
    }
    treeNodes.push(treeNode);
  }

  private addPrimitiveProperty(propertyPath: PropertyPath, treeNodes: TreeNode[]) {
    treeNodes.push(new class implements TreeNode<AngularTreeNodeData> {
      data = new class implements AngularTreeNodeData {
        canHaveChildren: boolean = false;
        createAllowed: boolean = false;
        deleteAllowed: boolean = false;
        name: string = propertyPath.getPropertyName();
        nullable: boolean = false;
        options: string[] = [];
        propertyEditorSelector: string = ThreeJsPropertyTable.setupPropertyEditorSelector(propertyPath.getProperty());
        value: any = propertyPath.getProperty();

        onCreate(gwtAngularPropertyTable: GwtAngularPropertyTable, createOption: any): void {
        }

        onDelete(gwtAngularPropertyTable: GwtAngularPropertyTable): void {
        }

        setValue(value: any): void {
          propertyPath.setInParent(value);
          propertyPath.updateParent();
        }
      }
    });
  }
}

class PropertyPath {
  private parent: PropertyPath | null;
  private property: any
  private propertyName: string | null;

  constructor(parent: PropertyPath | null, property: any, propertyName: string | null) {
    this.parent = parent;
    this.propertyName = propertyName;
    this.property = property;
  }

  generateChild(property: any, propertyName: string | null): PropertyPath {
    return new PropertyPath(this, property, propertyName);
  }

  setInParent(value: any) {
    this.getParent().property[this.propertyName!] = value;
  }

  setInGrandParent(value: any) {
    this.getGrandParent().property[this.getParent().propertyName!] = value;
  }

  setInGreatGrandParent(value: any) {
    this.getParent().getGrandParent().property[this.getGrandParent().propertyName!] = value;
  }

  getParent(): PropertyPath {
    return this.parent!;
  }

  getGrandParent(): PropertyPath {
    return this.getParent().getParent();
  }

  getGreatGrandParent(): PropertyPath {
    return this.getGrandParent().getParent();
  }

  getProperty(): any {
    return this.property;
  }

  getParentProperty(): any {
    return this.getParent().getProperty();
  }

  getGrandParentProperty(): any {
    return this.getGrandParent().getProperty();
  }

  getPropertyName(): string {
    return this.propertyName!;
  }

  updateParent() {
    this.getParent().property.needsUpdate = true;
  }

}
