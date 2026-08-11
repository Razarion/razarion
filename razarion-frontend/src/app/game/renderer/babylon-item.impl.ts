import {
  AbstractMesh,
  ActionEvent,
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
  private selectTipVisibilityObserver: Nullable<Observer<any>> = null;
  /**
   * The same dispatch the mesh's own pick triggers use. Kept reachable so a click that landed on
   * the ground inside this item's footprint can be routed here instead of becoming a move onto a
   * spot the item is standing on. Assigned in the constructor.
   */
  private fireItemClick!: () => void;

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
    const fireItemClick = () => {
      if (this.itemClickCallback) {
        this.itemClickCallback();
      }
      actionService.onItemClicked(itemType, id, diplomacy, this);
    };
    this.fireItemClick = fireItemClick;
    const isTouch = (event: ActionEvent) => (event?.sourceEvent as PointerEvent)?.pointerType === 'touch';
    /**
     * Babylon does not only dispatch the pick-up trigger from a release: it was measured firing
     * with a pointermove as its source event, 5ms after the press and 750ms before the finger
     * actually lifted. Requiring the real event is what makes the gesture check below meaningful,
     * because at that bogus moment no gesture has happened yet and every flag still reads false.
     */
    const isTouchRelease = (event: ActionEvent) => {
      const source = event?.sourceEvent as PointerEvent;
      return source?.pointerType === 'touch' && source.type === 'pointerup';
    };
    actionManager.registerAction(
      new ExecuteCodeAction(
        ActionManager.OnPickDownTrigger,
        event => {
          // A finger that starts a pan has to start it somewhere, and on a crowded screen that is
          // usually on top of something. Acting on the press would select that unit before the
          // player has moved a pixel - or, with a selection already made, fire the command that
          // this handler turns a click into: attack the enemy touched, harvest the resource,
          // load units into the container. Touch therefore waits for the release.
          if (isTouch(event)) {
            return;
          }
          fireItemClick();
        }
      )
    );
    actionManager.registerAction(
      new ExecuteCodeAction(
        // OnPickUpTrigger, deliberately not OnPickTrigger. OnPickTrigger looks like the right one -
        // "down and up on the same mesh, no swipe" - but Babylon dispatches it from the delayed
        // click timer that guards its double-click window, so it was measured firing 266ms after
        // the press, before the finger had lifted or moved at all. That is the same defect this
        // whole change is about: a press that lingers becomes a click. OnPickUpTrigger has no such
        // timer - it needs the source-event check below because Babylon reaches it from stray
        // events, but never on a clock.
        ActionManager.OnPickUpTrigger,
        event => {
          if (!isTouchRelease(event)) {
            return;
          }
          // The release runs before TouchCameraControl's own pointerup listener, and the flag is
          // only cleared by the next pointerdown - so a pan that ends here still reads as a
          // gesture. Do not rely on Babylon's swipe suppression instead: its _isSwiping stayed
          // false across a measured 160px drag.
          if (rendererService.touchCameraControl?.isGesturing()) {
            return;
          }
          fireItemClick();
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
    // The prompt lives in a fullscreen GUI texture of its own, linked to this item's node. Losing
    // the item does not take the texture with it: the panel stayed on screen, stuck at the border
    // where the link last put it, long after the thing it pointed at was gone.
    this.hideSelectPromptVisualization();
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
      // Water units and fixed-vertical items (e.g. buildings) use a flat normal
      // (0, 1, 0) so they stand upright instead of tilting to the terrain slope.
      if (this.isWaterUnit() || this.isFixVerticalNorm()) {
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

  // Items flagged fixVerticalNorm (buildings, resources, boxes) stand upright on the
  // vertical axis instead of tilting to the terrain normal like vehicles do.
  protected isFixVerticalNorm(): boolean {
    const baseItemType = <BaseItemType>this.itemType;
    if (baseItemType.getPhysicalAreaConfig !== undefined) {
      return baseItemType.getPhysicalAreaConfig().isFixVerticalNorm();
    }
    const flaggedItemType = <ResourceItemType | BoxItemType>this.itemType;
    if (flaggedItemType.isFixVerticalNorm !== undefined) {
      return flaggedItemType.isFixVerticalNorm();
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
    // Overwriting the fields would leave the previous texture on screen with an observer nobody
    // can reach any more - a second prompt that never goes away.
    this.hideSelectPromptVisualization();
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
    this.watchSelectPromptOnScreen(stackPanel);
  }

  /**
   * A GUI control linked to a mesh does not disappear when the mesh leaves the screen - Babylon
   * clamps it to the border. The prompt then sits at the bottom edge pointing at nothing, which
   * looks like a stuck tip and outlives whatever it was talking about. Hide it instead; the tip
   * tasks put an out-of-view marker on the target, and that is what should lead the way back.
   */
  private watchSelectPromptOnScreen(stackPanel: StackPanel): void {
    const scene = this.rendererService.getScene();
    this.selectTipVisibilityObserver = scene.onBeforeRenderObservable.add(() => {
      const camera = scene.activeCamera;
      if (!camera) {
        return;
      }
      const projected = Vector3.Project(
        this.getContainer().getAbsolutePosition(),
        Matrix.Identity(),
        scene.getTransformMatrix(),
        camera.viewport.toGlobal(scene.getEngine().getRenderWidth(), scene.getEngine().getRenderHeight()));
      const onScreen = projected.z > 0 && projected.z < 1
        && projected.x >= 0 && projected.x <= scene.getEngine().getRenderWidth()
        && projected.y >= 0 && projected.y <= scene.getEngine().getRenderHeight();
      if (stackPanel.isVisible !== onScreen) {
        stackPanel.isVisible = onScreen;
      }
    });
  }

  /**
   * Whether this item currently carries a prompt. The tip tasks check it: the prompt dies with
   * the item whenever it leaves the view, and an item that is back is not necessarily a new
   * instance the task would recognise as one to put the prompt back on.
   */
  isSelectPromptVisible(): boolean {
    return this.selectTipTexture !== null;
  }

  hideSelectPromptVisualization(): void {
    if (this.selectTipVisibilityObserver) {
      this.rendererService.getScene().onBeforeRenderObservable.remove(this.selectTipVisibilityObserver);
      this.selectTipVisibilityObserver = null;
    }
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

  /**
   * Selected, as opposed to merely under the mouse. What the item cockpit follows, and therefore
   * the only thing a tip waiting for a cockpit button may go by - isSelectOrHove() answers true
   * for a hover and had tips declaring "select the builder" done because the cursor happened to
   * rest on it.
   */
  isSelected(): boolean {
    return this.selectActive;
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

  /**
   * Acts as if the player had hit this item's mesh. Used by the terrain click path when the click
   * landed on the ground this item is standing on.
   */
  triggerClick(): void {
    this.fireItemClick();
  }

  getRadius(): number {
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
