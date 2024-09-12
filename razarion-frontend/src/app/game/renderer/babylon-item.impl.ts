import { GwtHelper } from "../../gwtangular/GwtHelper";
import {
  AbstractMesh,
  ActionManager,
  ExecuteCodeAction,
  InputBlock,
  Mesh,
  MeshBuilder,
  NodeMaterial,
  Tools,
  TransformNode,
  Vector3
} from "@babylonjs/core";
import {
  BabylonItem,
  BaseItemType,
  BoxItemType,
  DecimalPosition,
  Diplomacy,
  ItemType,
  MarkerConfig,
  ResourceItemType
} from "../../gwtangular/GwtAngularFacade";
import { SimpleMaterial } from "@babylonjs/materials";
import { BabylonModelService } from "./babylon-model.service";
import { BabylonRenderServiceAccessImpl } from "./babylon-render-service-access-impl.service";
import { ActionService, SelectionInfo } from "../action.service";
import { UiConfigCollectionService } from "../ui-config-collection.service";

export class BabylonItemImpl implements BabylonItem {
  static readonly SELECT_ALPHA: number = 0.3;
  static readonly HOVER_ALPHA: number = 0.6;
  private readonly container: TransformNode;
  private position: DecimalPosition | null = null;
  private angle: number = 0;
  private diplomacyMarkerDisc: Mesh | null = null;
  private visualizationMarkerDisc: Mesh | null = null;
  private selectActive: boolean = false;
  private hoverActive: boolean = false;
  private readonly itemCursorTypeHandler: (selectionInfo: SelectionInfo) => void;
  private lastNormal: Vector3 | null = null;

  constructor(private id: number,
    private itemType: ItemType,
    protected diplomacy: Diplomacy,
    protected rendererService: BabylonRenderServiceAccessImpl,
    protected babylonModelService: BabylonModelService,
    protected uiConfigCollectionService: UiConfigCollectionService,
    protected actionService: ActionService,
    parent: TransformNode) {
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
      this.container = MeshBuilder.CreateSphere(`No threeJsModelPackConfigId or meshContainerId for ${itemType.getInternalName()} '${itemType.getId()}'`, { diameter: this.getRadius() * 2 });
      console.warn(`No MeshContainerId or ThreeJsModelPackConfigId for ${itemType.getInternalName()} '${itemType.getId()}'`)
    }
    this.container.parent = parent;
    this.container.name = `${itemType.getInternalName()} '${id}')`;
    this.container.getChildMeshes().forEach(childMesh => {
      rendererService.shadowGenerator.addShadowCaster(childMesh, true);
    });

    let actionManager = new ActionManager(rendererService.getScene());
    actionManager.registerAction(
      new ExecuteCodeAction(
        ActionManager.OnPickTrigger,
        () => {
          actionService.onItemClicked(itemType, id, diplomacy);
        }
      )
    );
    this.itemCursorTypeHandler = (selectionInfo: SelectionInfo) => {
      if (diplomacy === Diplomacy.OWN) {
        actionManager.hoverCursor = "pointer"
        return;
      }

      if (diplomacy === Diplomacy.BOX) {
        if (selectionInfo.hasOwnMovable) {
          actionManager.hoverCursor = "url(\"/assets/cursors/pick.png\") 15 15, auto"
        } else {
          actionManager.hoverCursor = "pointer"
        }
        return;
      }

      if (diplomacy === Diplomacy.ENEMY) {
        if (selectionInfo.hasAttackers) {
          actionManager.hoverCursor = "url(\"/assets/cursors/attack.png\") 15 15, auto"
        } else {
          actionManager.hoverCursor = "pointer"
        }
        return;
      }

      if (diplomacy === Diplomacy.RESOURCE) {
        if (selectionInfo.hasHarvesters) {
          actionManager.hoverCursor = "url(\"/assets/cursors/collect.png\") 15 15, auto"
        } else {
          actionManager.hoverCursor = "pointer"
        }
        return;
      }
    }
    actionService.addCursoHandler(this.itemCursorTypeHandler);

    this.container.getChildMeshes().forEach(function (childMesh) {
      childMesh.actionManager = actionManager;
    });
    if (this.container.hasOwnProperty('actionManager')) {
      (<AbstractMesh>this.container).actionManager = actionManager;
    }

    this.itemCursorTypeHandler(this.actionService.setupSelectionInfo());
  }

  getId(): number {
    return this.id;
  }

  getAngle(): number {
    return this.angle;
  }

  setAngle(angle: number): void {
    this.angle = angle;
    let rotation3D: Vector3;
    if (this.lastNormal) {
      rotation3D = this.calculateRotation(this.lastNormal);
    } else {
      rotation3D = new Vector3(0, Tools.ToRadians(90) - this.angle, 0)
    }
    if (this.onRotation3D(rotation3D)) {
      this.container.rotation = rotation3D;
    }
  }

  getPosition(): DecimalPosition | null {
    return this.position;
  }

  dispose(): void {
    this.actionService.removeCursoHandler(this.itemCursorTypeHandler);
    this.container.getChildMeshes().forEach(childMesh => {
      this.rendererService.shadowGenerator.removeShadowCaster(childMesh, true);
    });
    this.rendererService.getScene().removeTransformNode(this.container);
    this.container.dispose();
  }

  setPosition(position: DecimalPosition): void {
    this.position = position;
    if (this.position) {
      let position3D = new Vector3(position.getX(), 0, position.getY());
      let rotation3D: Vector3;

      let pickingInfo = this.rendererService.setupTerrainPickPointFromPosition(this.position);

      if (pickingInfo && pickingInfo.hit) {
        // Position
        position3D.y = pickingInfo.pickedPoint!.y;

        // Rotation
        let normal = pickingInfo.getNormal(true)!;
        this.lastNormal = normal;
        rotation3D = this.calculateRotation(normal);
      } else {
        // Position
        position3D.y = 0;

        // Rotation
        rotation3D = new Vector3(0, Tools.ToRadians(90) - this.angle, 0)
      }

      if (this.onPosition3D(position3D)) {
        this.container.position = position3D;
      }
      if (this.onRotation3D(rotation3D)) {
        this.container.rotation = rotation3D;
      }
    }
  }

  onPosition3D(position3D: Vector3): boolean {
    return true;
  }

  onRotation3D(rotation3D: Vector3): boolean {
    return true;
  }

  private calculateRotation(normal: Vector3): Vector3 {
    let direction = new Vector3(Math.cos(this.angle), 0, Math.sin(this.angle));
    const forward = direction.normalize();
    const right = Vector3.Cross(normal, forward).normalize();
    const correctedForward = Vector3.Cross(right, normal).normalize();

    const pitch = Math.asin(-correctedForward.y);
    const yaw = Math.atan2(correctedForward.x, correctedForward.z);
    const roll = Math.atan2(right.y, normal.y);

    const correctedRoll = (normal.y < 0) ? roll + Math.PI : roll;

    return new Vector3(pitch, yaw, correctedRoll);
  }

  isEnemy(): boolean {
    return this.diplomacy == Diplomacy.ENEMY;
  }

  select(active: boolean): void {
    this.selectActive = active;
    this.updateMarkedDisk();
  }

  hover(active: boolean): void {
    this.hoverActive = active;
    this.updateMarkedDisk();
  }

  mark(markerConfig: MarkerConfig | null): void {
    if (markerConfig) {
      if (!markerConfig.nodesMaterialId) {
        console.warn("markerConfig.babylonModelId == null");
        return;
      }
      if (this.visualizationMarkerDisc) {
        this.visualizationMarkerDisc.dispose();
        console.warn("this.visualizationMarkerDisc != null")
      }
      this.visualizationMarkerDisc = MeshBuilder.CreateDisc("Visualization item marker", { radius: markerConfig.radius });
      let nodeMaterial = this.babylonModelService.getNodeMaterial(markerConfig.nodesMaterialId);
      this.visualizationMarkerDisc.material = nodeMaterial.clone(`${nodeMaterial.name} '${this.getId()}'`);
      this.visualizationMarkerDisc.position.y = 0.01;
      this.visualizationMarkerDisc.rotation.x = Tools.ToRadians(90);
      this.visualizationMarkerDisc.isPickable = false;
      this.visualizationMarkerDisc.parent = this.container;
      (<NodeMaterial>this.visualizationMarkerDisc.material).ignoreAlpha = false; // Can not be saved in the NodeEditor
    } else {
      if (!this.visualizationMarkerDisc) {
        console.warn("!this.visualizationMarkerDisc")
      } else {
        this.visualizationMarkerDisc.dispose();
        this.visualizationMarkerDisc = null;
      }
    }
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

  isSelectOrHove(): boolean {
    return this.selectActive || this.hoverActive;
  }

  private updateMarkedDisk(): void {
    if (this.isSelectOrHove()) {
      if (!this.diplomacyMarkerDisc) {
        this.diplomacyMarkerDisc = MeshBuilder.CreatePlane("Item Selection", {
          size: (this.getRadius() * 2) + 0.3,
        });
        let nodeMaterial = this.rendererService.itemMarkerMaterialCache.get(this.diplomacy);
        if (!nodeMaterial) {
          nodeMaterial = this.babylonModelService.getNodeMaterial(this.uiConfigCollectionService.getSelectionItemMaterialId());
          nodeMaterial = nodeMaterial.clone(`${nodeMaterial.name}  ${this.diplomacy}`);
          nodeMaterial.ignoreAlpha = false; // Can not be saved in the NodeEditor
          let diplomacyColor = <InputBlock>nodeMaterial.getBlockByName("diplomacyColor");
          if (diplomacyColor) {
            diplomacyColor.value = BabylonRenderServiceAccessImpl.color4Diplomacy(this.diplomacy);
          } else {
            console.warn(`'diplomacyColor' block not found in NodeMaterial ${this.uiConfigCollectionService.getSelectionItemMaterialId()}`)
          }
          this.rendererService.itemMarkerMaterialCache.set(this.diplomacy, nodeMaterial);
        }
        this.diplomacyMarkerDisc.material = nodeMaterial;
        this.diplomacyMarkerDisc.position.y = 0.2;
        this.diplomacyMarkerDisc.rotation.x = Tools.ToRadians(90);
        this.diplomacyMarkerDisc.parent = this.container;
      }
    } else {
      if (this.diplomacyMarkerDisc) {
        this.diplomacyMarkerDisc.dispose();
        this.diplomacyMarkerDisc = null;
      }
    }

    if (this.selectActive) {
      (<SimpleMaterial>this.diplomacyMarkerDisc!.material).alpha = BabylonItemImpl.HOVER_ALPHA;
    } else if (this.hoverActive) {
      (<SimpleMaterial>this.diplomacyMarkerDisc!.material).alpha = BabylonItemImpl.SELECT_ALPHA;
    }
  }

  private getRadius(): number {
    if ((<BaseItemType>this.itemType).getPhysicalAreaConfig !== undefined) {
      return (<BaseItemType>this.itemType).getPhysicalAreaConfig().getRadius();
    } else if ((<ResourceItemType>this.itemType).getRadius !== undefined) {
      return (<ResourceItemType>this.itemType).getRadius();
    } else if ((<BoxItemType>this.itemType).getRadius !== undefined) {
      return (<BoxItemType>this.itemType).getRadius();
    } else {
      console.warn(`No radius for ${this.itemType.getInternalName()} '${this.itemType.getId()}'`)
      return 3;
    }
  }
}
