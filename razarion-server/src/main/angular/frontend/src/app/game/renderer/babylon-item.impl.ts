import {GwtHelper} from "../../gwtangular/GwtHelper";
import {Mesh, MeshBuilder, Tools, TransformNode} from "@babylonjs/core";
import {
  BabylonItem,
  BaseItemType,
  Diplomacy,
  ItemType,
  ResourceItemType,
  Vertex
} from "../../gwtangular/GwtAngularFacade";
import {SimpleMaterial} from "@babylonjs/materials";
import {BabylonModelService} from "./babylon-model.service";
import {ThreeJsRendererServiceImpl} from "./three-js-renderer-service.impl";

export class BabylonItemImpl implements BabylonItem {
  private readonly container: TransformNode;
  private position: Vertex | null = null;
  private angle: number = 0;
  private markerDisc: Mesh | null = null;
  private selectActive: boolean = false;
  private hoverActive: boolean = false;

  constructor(private id: number, private itemType: ItemType, protected diplomacy: Diplomacy, protected rendererService: ThreeJsRendererServiceImpl, protected babylonModelService: BabylonModelService, parent: TransformNode) {
    if (itemType.getThreeJsModelPackConfigId()) {
      this.container = this.babylonModelService.cloneMesh(itemType.getThreeJsModelPackConfigId()!, null);
    } else if (itemType.getMeshContainerId()) {
      if (diplomacy) {
        this.container = this.rendererService.showMeshContainer(this.rendererService.meshContainers,
          GwtHelper.gwtIssueNumber(itemType.getMeshContainerId()),
          diplomacy);
      } else {
        throw new Error("Diplomacy can not be null");
      }
    } else {
      this.container = MeshBuilder.CreateSphere(`No threeJsModelPackConfigId or meshContainerId for ${itemType.getInternalName()} '${itemType.getId()}'`, {diameter: this.getRadius() * 2});
      console.warn(`No MeshContainerId or ThreeJsModelPackConfigId for ${itemType.getInternalName()} '${itemType.getId()}'`)
    }
    this.container.parent = parent;
    this.container.name = `${itemType.getInternalName()} '${id}')`;
    this.container.getChildMeshes().forEach(childMesh => {
      rendererService.shadowGenerator.addShadowCaster(childMesh, true);
    });
  }

  getId(): number {
    return this.id;
  }

  getAngle(): number {
    return this.angle;
  }

  setAngle(angle: number): void {
    this.angle = angle;
  }

  setPosition(position: Vertex): void {
    this.position = position;
  }

  getPosition(): Vertex | null {
    return this.position;
  }

  dispose(): void {
    this.container.getChildMeshes().forEach(childMesh => {
      this.rendererService.shadowGenerator.removeShadowCaster(childMesh, true);
    });
    this.rendererService.getScene().removeTransformNode(this.container);
    this.container.dispose();
  }

  updatePosition(): void {
    if (this.position) {
      this.container.position.x = this.position.getX();
      this.container.position.y = this.position.getZ();
      this.container.position.z = this.position.getY();
    }
  }

  updateAngle(): void {
    this.container.rotation.y = Tools.ToRadians(90) - this.angle;
  }

  select(active: boolean): void {
    this.selectActive = active;
    this.updateMarkedDisk();
  }

  hover(active: boolean): void {
    this.hoverActive = active;
    this.updateMarkedDisk();
  }

  getContainer(): TransformNode {
    return this.container;
  }

  findChildMesh(meshPath: string[]): Mesh {
    for (let childNod of this.getContainer().getChildren()) {
      let found = BabylonModelService.findChildNode(childNod, meshPath);
      if (found) {
        return <Mesh>found;
      }
    }
    throw new Error(`Can not find mesh path '${meshPath}' in '${this.getContainer()}'`);
  }

  private updateMarkedDisk(): void {
    if (this.selectActive || this.hoverActive) {
      if (!this.markerDisc) {
        this.markerDisc = MeshBuilder.CreateDisc("Base Item Marker", {radius: this.getRadius() + 0.1});
        let material = this.rendererService.itemMarkerMaterialCache.get(this.diplomacy);
        if (!material) {
          material = new SimpleMaterial(`Base Item Marker ${this.diplomacy}`, this.rendererService.getScene());
          material.diffuseColor = ThreeJsRendererServiceImpl.color4Diplomacy(this.diplomacy);
          this.rendererService.itemMarkerMaterialCache.set(this.diplomacy, material);
        }
        this.markerDisc.material = material;
        this.markerDisc.position.y = 0.01;
        this.markerDisc.rotation.x = Tools.ToRadians(90);
        this.markerDisc.parent = this.container;
      }
    } else {
      if (this.markerDisc) {
        this.markerDisc.dispose();
        this.markerDisc = null;
      }
    }

    if (this.selectActive) {
      (<SimpleMaterial>this.markerDisc!.material).alpha = 0.6
    } else if (this.hoverActive) {
      (<SimpleMaterial>this.markerDisc!.material).alpha = 0.3
    }
  }

  private getRadius(): number {
    let radius;
    if ((<BaseItemType>this.itemType).getPhysicalAreaConfig !== undefined) {
      return (<BaseItemType>this.itemType).getPhysicalAreaConfig().getRadius();
    } else if ((<ResourceItemType>this.itemType).getRadius !== undefined) {
      return (<ResourceItemType>this.itemType).getRadius();
    } else {
      console.warn(`No radius for ${this.itemType.getInternalName()} '${this.itemType.getId()}'`)
      return 3;
    }
  }
}
