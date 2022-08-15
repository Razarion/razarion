import {TreeNode} from "primeng/api";
import {Object3D} from "three";
import {ThreeJsRendererServiceImpl} from "../../game/renderer/three-js-renderer-service.impl";

export class ThreeJsTree {
  private rootTreeNodes: TreeNode<Object3D>[] = [];

  static createFromRendererService(threeJsRendererServiceImpl: ThreeJsRendererServiceImpl): ThreeJsTree {
    let threeJsTree: ThreeJsTree = new ThreeJsTree()
    let camera = threeJsRendererServiceImpl.camera;
    threeJsTree.rootTreeNodes.push(new class implements TreeNode {
      label = camera.name;
      icon = 'pi pi-video';
      data = camera;
    });
    threeJsTree.rootTreeNodes.push(threeJsTree.recursivelyAddTreeNodes(threeJsRendererServiceImpl.scene));
    threeJsTree.rootTreeNodes.push(threeJsTree.recursivelyAddTreeNodes(threeJsRendererServiceImpl.slopeScene));
    return threeJsTree;
  }

  static createFromThreeJsModel(object3D: Object3D): ThreeJsTree {
    let threeJsTree: ThreeJsTree = new ThreeJsTree()
    threeJsTree.rootTreeNodes.push(threeJsTree.recursivelyAddTreeNodes(object3D));
    return threeJsTree;
  }

  public findTreeNode(object3D: Object3D): TreeNode<Object3D> | undefined {
    return this.findTreeNodeRecursively(object3D, this.rootTreeNodes);
  }

  getRootTreeNodes(): TreeNode<Object3D>[] {
    return this.rootTreeNodes;
  }

  public expandParent(threeNode: TreeNode | undefined) {
    if (threeNode) {
      threeNode.expanded = true;
      this.expandParent(threeNode.parent);
    }
  }

  private recursivelyAddTreeNodes(object3D: Object3D, parentTreeNode?: TreeNode<Object3D>): TreeNode<Object3D> {
    let treeNode = new class implements TreeNode<Object3D> {
      label = object3D.name;
      icon = 'pi pi-globe';
      // noinspection JSMismatchedCollectionQueryUpdate
      children: TreeNode<Object3D>[] = [];
      data = object3D;
      parent = parentTreeNode;
    };

    for (let i = 0, l = object3D.children.length; i < l; i++) {
      const child = object3D.children[i];
      treeNode.children.push(this.recursivelyAddTreeNodes(child, treeNode));
    }

    return treeNode;
  }

  private findTreeNodeRecursively(object3D: Object3D, threeNodes: TreeNode<Object3D>[]): TreeNode<Object3D> | undefined {
    for (const threeNode of threeNodes) {
      if (threeNode.data?.id === object3D.id) {
        return threeNode;
      }
      if (threeNode.children) {
        let childObject3D = this.findTreeNodeRecursively(object3D, threeNode.children);
        if (childObject3D) {
          return childObject3D;
        }
      }
    }
    return undefined;
  }
}
