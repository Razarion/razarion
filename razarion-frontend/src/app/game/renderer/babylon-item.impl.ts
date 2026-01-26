import {
  ActionManager,
  Animation,
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
  Diplomacy,
  ItemType,
  MarkerConfig,
  ResourceItemType,
  SelectionService,
  TerrainType,
  Vertex
} from "../../gwtangular/GwtAngularFacade";
import {SimpleMaterial} from "@babylonjs/materials";
import {BabylonModelService} from "./babylon-model.service";
import {BabylonRenderServiceAccessImpl, RazarionMetadataType} from "./babylon-render-service-access-impl.service";
import {ActionService, SelectionInfo} from "../action.service";
import {UiConfigCollectionService} from "../ui-config-collection.service";
import {GwtInstance} from '../../gwtangular/GwtInstance';
import {RenderObject} from './render-object';
import {PressMouseVisualization} from './press-mouse-visualization';
import {AdvancedDynamicTexture, StackPanel} from '@babylonjs/gui';
import {Image} from '@babylonjs/gui/2D/controls/image';
import {GwtHelper} from '../../gwtangular/GwtHelper';

export class BabylonItemImpl implements BabylonItem {
  static readonly SELECT_ALPHA: number = 0.3;
  static readonly HOVER_ALPHA: number = 0.6;
  private readonly renderObject: RenderObject;
  private position: Vertex | null = null;
  private angle: number = 0;
  private diplomacyMarkerDisc: Mesh | null = null;
  private visualizationMarkerDisc: Mesh | null = null;
  private selectActive: boolean = false;
  private hoverActive: boolean = false;
  private readonly itemCursorTypeHandler: (selectionInfo: SelectionInfo) => void;
  private lastNormal: Vector3 | null = null;
  private selectionCallback: ((active: boolean) => void) | null = null;
  private itemClickCallback: (() => void) | null = null;
  private selectTipTexture: AdvancedDynamicTexture | null = null;

  constructor(private id: number,
              public readonly itemType: ItemType,
              public readonly diplomacy: Diplomacy,
              protected rendererService: BabylonRenderServiceAccessImpl,
              protected babylonModelService: BabylonModelService,
              protected uiConfigCollectionService: UiConfigCollectionService,
              protected actionService: ActionService,
              protected selectionService: SelectionService,
              parent: TransformNode,
              protected disposeCallback: (() => void) | null) {
    if (itemType.getModel3DId()) {
      this.renderObject = this.babylonModelService.cloneModel3D(itemType.getModel3DId()!, parent, diplomacy);
    } else {
      this.renderObject = new RenderObject(rendererService);
      this.renderObject.setModel3D(MeshBuilder.CreateSphere(`No threeJsModelPackConfigId or meshContainerId for ${itemType.getInternalName()} '${itemType.getId()}'`, {diameter: this.getRadius() * 2}))
      console.warn(`No MeshContainerId or ThreeJsModelPackConfigId for ${itemType.getInternalName()} '${itemType.getId()}'`)
    }
    this.renderObject.setParent(parent);
    this.renderObject.setName(`${itemType.getInternalName()} '${id}')`);
    this.renderObject.addAllShadowCasters(rendererService);

    let actionManager = new ActionManager(rendererService.getScene());
    actionManager.registerAction(
      new ExecuteCodeAction(
        ActionManager.OnPickTrigger,
        () => {
          if (this.itemClickCallback) {
            this.itemClickCallback();
          }
          actionService.onItemClicked(itemType, id, diplomacy);
        }
      )
    );
    this.itemCursorTypeHandler = (selectionInfo: SelectionInfo) => {
      if (diplomacy === Diplomacy.OWN) {
        if (selectionService.canContain(this.id)) {
          actionManager.hoverCursor = "url(\"cursors/load.png\") 15 15, auto"
        } else if (selectionService.canBeFinalizeBuild(this.id)) {
          actionManager.hoverCursor = "url(\"cursors/finalize-build.png\") 15 15, auto"
        } else {
          actionManager.hoverCursor = "pointer"
        }
        return;
      }

      if (diplomacy === Diplomacy.BOX) {
        if (selectionInfo.hasOwnMovable) {
          actionManager.hoverCursor = "url(\"cursors/pick.png\") 15 15, auto"
        } else {
          actionManager.hoverCursor = "pointer"
        }
        return;
      }

      if (diplomacy === Diplomacy.ENEMY) {
        if (selectionInfo.hasAttackers) {
          actionManager.hoverCursor = "url(\"cursors/attack.png\") 15 15, auto"
        } else {
          actionManager.hoverCursor = "pointer"
        }
        return;
      }

      if (diplomacy === Diplomacy.RESOURCE) {
        if (selectionInfo.hasHarvesters) {
          actionManager.hoverCursor = "url(\"cursors/collect.png\") 15 15, auto"
        } else {
          actionManager.hoverCursor = "pointer"
        }
        return;
      }
    }
    actionService.addCursorHandler(this.itemCursorTypeHandler);
    this.renderObject.setActionManager(actionManager);
    this.updateItemCursor();
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
      this.renderObject.setRotation(rotation3D);
    }
  }

  getPosition(): Vertex | null {
    return this.position;
  }

  dispose(): void {
    if (this.disposeCallback) {
      this.disposeCallback();
    }
    this.actionService.removeCursorHandler(this.itemCursorTypeHandler);
    this.renderObject.removeAllShadowCasters(this.rendererService)
    this.rendererService.getScene().removeTransformNode(this.renderObject.getModel3D());
    this.renderObject.dispose();
  }

  setPosition(position: Vertex): void {
    if (position) {
      // Position
      let position3D = new Vector3(position.getX(), position.getZ(), position.getY());
      if (this.onPosition3D(position3D)) {
        this.renderObject.setPosition(position3D);
      }
      // Rotation
      let normal: Vector3;
      // Water units use flat normal (0, 1, 0) instead of terrain normal
      if (this.isWaterUnit()) {
        normal = new Vector3(0, 1, 0);
      } else {
        let pickingInfo = this.rendererService.setupTerrainPickPointFromPosition(GwtInstance.newDecimalPosition(position.getX(), position.getY()));
        if (pickingInfo && pickingInfo.hit) {
          let razarionMetadata = pickingInfo.pickedMesh && BabylonRenderServiceAccessImpl.getRazarionMetadata(pickingInfo.pickedMesh);
          let pickedNormal: Vector3 | undefined;
          if (razarionMetadata) {
            if (razarionMetadata.type == RazarionMetadataType.BOT_GROUND) {
              pickedNormal = razarionMetadata.botGroundNorm;
            }
          }
          if (!pickedNormal) {
            pickedNormal = pickingInfo.getNormal(true)!;
          }
          normal = pickedNormal;
        } else {
          normal = new Vector3(0, 1, 0);
        }
      }
      this.lastNormal = normal;
      let rotation3D = this.calculateRotation(normal);
      if (this.onRotation3D(rotation3D)) {
        this.renderObject.setRotation(rotation3D);
      }
    }
    this.position = position;
  }

  private isWaterUnit(): boolean {
    if ((<BaseItemType>this.itemType).getPhysicalAreaConfig !== undefined) {
      const terrainType = (<BaseItemType>this.itemType).getPhysicalAreaConfig().getTerrainType();
      return GwtHelper.gwtIssueStringEnum(terrainType, TerrainType) === TerrainType.WATER;
    }
    return false;
  }

  onPosition3D(position3D: Vector3): boolean {
    return true;
  }

  onRotation3D(rotation3D: Vector3): boolean {
    return true;
  }

  setItemClickCallback(callback: (() => void) | null) {
    this.itemClickCallback = callback;
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
    if (this.selectionCallback) {
      this.selectionCallback(active);
    }
  }

  hover(active: boolean): void {
    this.hoverActive = active;
    this.updateMarkedDisk();
  }

  mark(markerConfig: MarkerConfig | null): void {
    if (markerConfig) {
      if (!markerConfig.nodesMaterialId) {
        console.warn("markerConfig.nodesMaterialId == null");
        return;
      }
      if (this.visualizationMarkerDisc) {
        this.visualizationMarkerDisc.dispose();
        console.warn("this.visualizationMarkerDisc != null")
      }
      this.visualizationMarkerDisc = MeshBuilder.CreateDisc("Visualization item marker", {radius: markerConfig.radius});
      let nodeMaterial = this.babylonModelService.getBabylonMaterial(markerConfig.nodesMaterialId);
      this.visualizationMarkerDisc.material = nodeMaterial.clone(`${nodeMaterial.name} '${this.getId()}'`);
      this.visualizationMarkerDisc.position.y = 0.01;
      this.visualizationMarkerDisc.rotation.x = Tools.ToRadians(90);
      this.visualizationMarkerDisc.isPickable = false;
      this.visualizationMarkerDisc.parent = this.renderObject.getModel3D();
      (<NodeMaterial>this.visualizationMarkerDisc.material).ignoreAlpha = false; // Can not be saved in the NodeEditor
    } else {
      if (this.visualizationMarkerDisc) {
        this.visualizationMarkerDisc.dispose();
        this.visualizationMarkerDisc = null;
      }
    }
  }

  showSelectPromptVisualization(text: string = "Click to select", labelWidth: string = "150px"): void {
    this.selectTipTexture = AdvancedDynamicTexture.CreateFullscreenUI("Select tip");
    this.selectTipTexture.disablePicking = true; // Prevent mouse down on terrain cursor change
    let pressMouseVisualization = new PressMouseVisualization(true, this.rendererService);
    pressMouseVisualization.label.text = text;
    pressMouseVisualization.label.width = labelWidth;
    pressMouseVisualization.container.width = "200px";
    let stackPanel = new StackPanel();
    stackPanel.spacing = 10;
    stackPanel.addControl(pressMouseVisualization.getContainer());

    const mouse = new Image();
    mouse.source = "babylon-gui/arrow-down.svg";
    mouse.width = "65px";
    mouse.height = "110px";

    stackPanel.addControl(mouse)
    this.selectTipTexture.addControl(stackPanel)
    stackPanel.linkWithMesh(this.getContainer());

    const frameRate = 100;
    const xSlide = new Animation("xSlide", "linkOffsetY", frameRate, Animation.ANIMATIONTYPE_FLOAT, Animation.ANIMATIONLOOPMODE_CYCLE);
    const keyFrames = [];

    keyFrames.push({
      frame: 0,
      value: -150,
    });

    keyFrames.push({
      frame: 2 * frameRate,
      value: -200,
    });

    keyFrames.push({
      frame: 3 * frameRate,
      value: -150,
    });

    xSlide.setKeys(keyFrames);

    stackPanel.animations = [];
    stackPanel.animations.push(xSlide);

    this.rendererService.getScene().beginAnimation(stackPanel, 0, 3 * frameRate, true, 4);
  }

  hideSelectPromptVisualization(): void {
    if (this.selectTipTexture) {
      this.selectTipTexture.dispose();
      this.selectTipTexture = null;
    }
  }

  setSelectionCallback(selectionCallback: ((active: boolean) => void) | null) {
    this.selectionCallback = selectionCallback;
  }

  getRenderObject(): RenderObject {
    return this.renderObject;
  }

  getContainer(): TransformNode {
    return this.renderObject.getModel3D();
  }

  findChildMesh(nodeId: string): Mesh {
    let nodesFound = this.getContainer().getDescendants(false, node => node.id === nodeId);
    if (nodesFound.length > 0) {
      if (nodesFound.length > 1) {
        console.warn(`more then 1 node found in nodeId:${nodeId} babylon-item id ${this.id}`)
      }
      return <Mesh>nodesFound[0];
    }
    throw new Error(`Can not find mesh path '${nodeId}' in '${this.getContainer()}'`);
  }

  isSelectOrHove(): boolean {
    return this.selectActive || this.hoverActive;
  }

  protected updateItemCursor() {
    this.itemCursorTypeHandler(this.actionService.setupSelectionInfo());
  }

  private updateMarkedDisk(): void {
    if (this.isSelectOrHove()) {
      if (!this.diplomacyMarkerDisc) {
        this.diplomacyMarkerDisc = MeshBuilder.CreatePlane("Item Selection", {
          size: (this.getRadius() * 2) + 0.3,
        });
        let nodeMaterial = this.rendererService.itemMarkerMaterialCache.get(this.diplomacy);
        if (!nodeMaterial) {
          nodeMaterial = <NodeMaterial>this.babylonModelService.getBabylonMaterial(this.uiConfigCollectionService.getSelectionItemMaterialId());
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
        this.diplomacyMarkerDisc.parent = this.renderObject.getModel3D();
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
