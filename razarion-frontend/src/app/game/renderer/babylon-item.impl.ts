import {
  AbstractMesh,
  ActionManager,
  Animation,
  Color3,
  ExecuteCodeAction,
  LinesMesh,
  Matrix,
  Mesh,
  MeshBuilder,
  NodeMaterial,
  Nullable,
  Tools,
  TransformNode,
  Vector3
} from "@babylonjs/core";
import {Observer} from "@babylonjs/core/Misc/observable";
import {
  BabylonItem,
  BaseItemType,
  BoxItemType,
  Diplomacy,
  ItemType,
  MarkerConfig,
  ResourceItemType,
  TerrainType,
  Vertex
} from "../../gwtangular/GwtAngularFacade";
import {BabylonModelService} from "./babylon-model.service";
import {BabylonRenderServiceAccessImpl, RazarionMetadataType} from "./babylon-render-service-access-impl.service";
import {ActionService, SelectionInfo} from "../action.service";
import {UiConfigCollectionService} from "../ui-config-collection.service";
import {SelectionService as TsSelectionService} from "../selection.service";
import {GwtInstance} from '../../gwtangular/GwtInstance';
import {RenderObject} from './render-object';
import {PressMouseVisualization} from './press-mouse-visualization';
import {AdvancedDynamicTexture, StackPanel} from '@babylonjs/gui';
import {Image} from '@babylonjs/gui/2D/controls/image';
import {GwtHelper} from '../../gwtangular/GwtHelper';

export class BabylonItemImpl implements BabylonItem {
  private static readonly HIGHLIGHT_HOVER_INTENSITY = 0.55;
  // Bracket arm length is a fraction of the AABB's smallest extent (clamped) so brackets stay
  // visually balanced across tiny boxes and large buildings.
  private static readonly BRACKET_ARM_FRACTION = 0.22;
  private static readonly BRACKET_ARM_MIN = 0.35;
  private static readonly BRACKET_ARM_MAX = 1.5;
  private readonly renderObject: RenderObject;
  private position: Vertex | null = null;
  private angle: number = 0;
  private bracketMesh: LinesMesh | null = null;
  private highlightActive: 'select' | 'hover' | null = null;
  private buildStateObserver: Nullable<Observer<boolean>> = null;
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
              protected tsSelectionService: TsSelectionService,
              parent: TransformNode,
              protected disposeCallback: ((permanent: boolean) => void) | null) {
    if (itemType.getModel3DId()) {
      this.renderObject = this.babylonModelService.cloneModel3D(itemType.getModel3DId()!, parent, diplomacy);
    } else {
      this.renderObject = new RenderObject(rendererService);
      this.renderObject.setModel3D(MeshBuilder.CreateSphere(`No threeJsModelPackConfigId or meshContainerId for ${itemType.getInternalName()} '${itemType.getId()}'`, {diameter: this.getRadius() * 2}))
      console.warn(`No MeshContainerId or ThreeJsModelPackConfigId for ${itemType.getInternalName()} '${itemType.getId()}'`)
    }
    this.renderObject.setParent(parent);
    this.renderObject.setName(`${itemType.getInternalName()} '${id}')`);
    // Water units (ships, water buildings) skip shadow casting — the only
    // surface the shadow could land on is the underwater seabed, where it
    // looks unnatural and hides the bow-wave foam halo overlay.
    if (!this.isWaterUnit()) {
      this.renderObject.addAllShadowCasters(rendererService);
    }

    let actionManager = new ActionManager(rendererService.getScene());
    actionManager.registerAction(
      new ExecuteCodeAction(
        ActionManager.OnPickDownTrigger,
        () => {
          if (this.itemClickCallback) {
            this.itemClickCallback();
          }
          actionService.onItemClicked(itemType, id, diplomacy, this);
        }
      )
    );
    this.itemCursorTypeHandler = (selectionInfo: SelectionInfo) => {
      if (diplomacy === Diplomacy.OWN) {
        if (this.tsSelectionService.canContain(this as any)) {
          actionManager.hoverCursor = "url(\"cursors/load.png\") 15 15, auto"
        } else if (this.tsSelectionService.canBeFinalizeBuild(this as any)) {
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
    this.buildStateObserver = this.renderObject.onBuildAnimationActiveChanged.add(() => {
      this.updateHighlight();
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
    this.disposeInternal(true);
  }

  removeFromView(): void {
    this.disposeInternal(false);
  }

  stopAllAnimations(): void {
    this.renderObject.stopAllAnimations();
  }

  private disposeInternal(permanent: boolean): void {
    if (this.disposeCallback) {
      this.disposeCallback(permanent);
    }
    this.actionService.removeCursorHandler(this.itemCursorTypeHandler);
    if (this.buildStateObserver) {
      this.renderObject.onBuildAnimationActiveChanged.remove(this.buildStateObserver);
      this.buildStateObserver = null;
    }
    this.removeHighlight();
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
      this.renderObject.setRotation(rotation3D);
    }
    this.position = position;
  }

  protected isWaterUnit(): boolean {
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
    this.updateHighlight();
    if (this.selectionCallback) {
      this.selectionCallback(active);
    }
  }

  hover(active: boolean): void {
    this.hoverActive = active;
    this.updateHighlight();
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

  showSelectPromptVisualization(text: string = "Click to select", labelWidth: string = "150px", containerWidth: string = "200px"): void {
    this.selectTipTexture = AdvancedDynamicTexture.CreateFullscreenUI("Select tip");
    this.selectTipTexture.disablePicking = true; // Prevent mouse down on terrain cursor change
    let pressMouseVisualization = new PressMouseVisualization(true, this.rendererService);
    pressMouseVisualization.label.text = text;
    pressMouseVisualization.label.width = labelWidth;
    pressMouseVisualization.container.width = containerWidth;
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

  private updateHighlight(): void {
    // Suppress while a build animation is running — partial mesh visibility during the
    // intro/outro phase produces a wrong AABB and visually noisy bracket positions.
    const buildActive = this.renderObject.isBuildAnimationActive();
    const desired: 'select' | 'hover' | null = buildActive ? null
      : (this.selectActive ? 'select' : (this.hoverActive ? 'hover' : null));
    if (desired === this.highlightActive) {
      return;
    }
    this.removeHighlight();
    if (!desired) {
      return;
    }
    const baseColor = BabylonRenderServiceAccessImpl.color4Diplomacy(this.diplomacy);
    const color: Color3 = desired === 'select'
      ? baseColor
      : baseColor.scale(BabylonItemImpl.HIGHLIGHT_HOVER_INTENSITY);
    this.createBrackets(color);
    this.highlightActive = desired;
  }

  private createBrackets(color: Color3): void {
    const root = this.renderObject.getModel3D();
    const aabb = this.computeLocalAABB(root);
    if (!aabb) {
      return;
    }
    const {min, max} = aabb;
    const sizeX = max.x - min.x;
    const sizeY = max.y - min.y;
    const sizeZ = max.z - min.z;
    if (sizeX <= 0 || sizeY <= 0 || sizeZ <= 0) {
      return;
    }
    const minDim = Math.min(sizeX, sizeY, sizeZ);
    const arm = Math.max(BabylonItemImpl.BRACKET_ARM_MIN,
      Math.min(BabylonItemImpl.BRACKET_ARM_MAX, minDim * BabylonItemImpl.BRACKET_ARM_FRACTION));

    const lines: Vector3[][] = [];
    for (let i = 0; i < 8; i++) {
      const cx = (i & 1) ? max.x : min.x;
      const cy = (i & 2) ? max.y : min.y;
      const cz = (i & 4) ? max.z : min.z;
      // Each arm grows from the corner toward the inside of the box.
      const sx = (i & 1) ? -1 : 1;
      const sy = (i & 2) ? -1 : 1;
      const sz = (i & 4) ? -1 : 1;
      const corner = new Vector3(cx, cy, cz);
      lines.push([corner, new Vector3(cx + sx * arm, cy, cz)]);
      lines.push([corner, new Vector3(cx, cy + sy * arm, cz)]);
      lines.push([corner, new Vector3(cx, cy, cz + sz * arm)]);
    }
    const brackets = MeshBuilder.CreateLineSystem(`Selection brackets '${this.id}'`,
      {lines, updatable: false}, this.rendererService.getScene());
    brackets.color = color;
    brackets.isPickable = false;
    brackets.parent = root;
    // Render after the regular scene so brackets stay visible against the unit and terrain bumps.
    brackets.renderingGroupId = 1;
    this.bracketMesh = brackets;
  }

  // Local-space AABB of the model hierarchy: for each descendant mesh, take its local bounding
  // box corners, transform them into root-local space, and accumulate min/max. Walking corners
  // (rather than relying on world-space vectorsWorld) keeps the box tight regardless of any
  // sub-node rotations.
  private computeLocalAABB(root: TransformNode): { min: Vector3, max: Vector3 } | null {
    root.computeWorldMatrix(true);
    const rootInverse = Matrix.Invert(root.getWorldMatrix());
    let minX = Infinity, minY = Infinity, minZ = Infinity;
    let maxX = -Infinity, maxY = -Infinity, maxZ = -Infinity;
    let any = false;
    const meshes: AbstractMesh[] = [];
    if (root instanceof AbstractMesh) {
      meshes.push(root);
    }
    root.getChildMeshes(false).forEach(m => meshes.push(m));
    meshes.forEach(mesh => {
      if (!(mesh instanceof Mesh) || mesh.getTotalVertices() === 0) {
        return;
      }
      mesh.computeWorldMatrix(true);
      const bb = mesh.getBoundingInfo().boundingBox;
      const meshToRoot = mesh.getWorldMatrix().multiply(rootInverse);
      for (let i = 0; i < 8; i++) {
        const lx = (i & 1) ? bb.maximum.x : bb.minimum.x;
        const ly = (i & 2) ? bb.maximum.y : bb.minimum.y;
        const lz = (i & 4) ? bb.maximum.z : bb.minimum.z;
        const corner = Vector3.TransformCoordinates(new Vector3(lx, ly, lz), meshToRoot);
        if (corner.x < minX) minX = corner.x;
        if (corner.y < minY) minY = corner.y;
        if (corner.z < minZ) minZ = corner.z;
        if (corner.x > maxX) maxX = corner.x;
        if (corner.y > maxY) maxY = corner.y;
        if (corner.z > maxZ) maxZ = corner.z;
        any = true;
      }
    });
    return any ? {min: new Vector3(minX, minY, minZ), max: new Vector3(maxX, maxY, maxZ)} : null;
  }

  private removeHighlight(): void {
    if (this.bracketMesh) {
      this.bracketMesh.dispose();
      this.bracketMesh = null;
    }
    this.highlightActive = null;
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
