import {Component, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {ObjectNameId, TerrainObjectConfig, TerrainObjectModel, Vertex} from "../../gwtangular/GwtAngularFacade";
import {GwtAngularService} from "../../gwtangular/GwtAngularService";
import {MessageService} from "primeng/api";
import {
  BabylonRenderServiceAccessImpl,
  RazarionMetadataType
} from "../../game/renderer/babylon-render-service-access-impl.service";
import {BabylonModelService} from "../../game/renderer/babylon-model.service";
import {TerrainObjectPositionComponent} from "./terrain-object-position.component";
import {GwtInstance} from "../../gwtangular/GwtInstance";
import {GizmoManager, Mesh, MeshBuilder, Node, PointerEventTypes, Quaternion, Tools, TransformNode, Vector3} from "@babylonjs/core";
import {BabylonTerrainTileImpl} from "../../game/renderer/babylon-terrain-tile.impl";
import {Observer} from "@babylonjs/core/Misc/observable";
import {PointerInfo} from "@babylonjs/core/Events/pointerEvents";
import {Nullable} from "@babylonjs/core/types";
import {EditorService} from "../editor-service";
import {SimpleMaterial} from "@babylonjs/materials";
import {Color3} from "@babylonjs/core/Maths/math.color";
import {UPDATE_RADIUS_REST_CALL} from "../../common";
import {HttpClient} from "@angular/common/http";
import {EditorPanel} from '../editor-model';
import {
  ScatterBrushControllerClient,
  ScatterBrushEntity,
  TerrainEditorControllerClient,
  TerrainEditorUpdate,
  TerrainObjectEditorControllerClient,
  TerrainObjectPosition
} from "../../generated/razarion-share";
import {TypescriptGenerator} from "../../backend/typescript-generator";
import {GeneratedRestHelper} from "../../common/generated-rest-helper";
import {AbstractObjectBrush, ObjectBrushContext} from "./object-brushes/abstract-object-brush";
import {DeleteObjectBrush} from "./object-brushes/delete-object-brush";
import {ScatterObjectBrush} from "./object-brushes/scatter-object-brush";
import {PushObjectBrush} from "./object-brushes/push-object-brush";
import {Button} from 'primeng/button';
import {Divider} from 'primeng/divider';
import {SelectButton} from 'primeng/selectbutton';
import {InputNumber} from 'primeng/inputnumber';
import {FormsModule} from '@angular/forms';
import {Select} from 'primeng/select';
import {MultiSelect} from 'primeng/multiselect';

type ObjectBrushMode = 'EDIT' | 'DELETE' | 'SCATTER' | 'PUSH';

/** The stored payload of a scatter-brush preset (selected object type ids + the brush parameters). */
interface ScatterPresetValues {
  configIds: number[];
  radius: number;
  countPerDab: number;
  minDistance: number;
  minScale: number;
  maxScale: number;
  yawJitterDeg: number;
  slopeFilterEnabled: boolean;
  slopeThreshold: number;
  slopeAbove: boolean;
  waterFilterEnabled: boolean;
  waterLevel: number;
  waterAbove: boolean;
}

/** A named, server-persisted scatter-brush preset. */
class ScatterPreset {
  constructor(public id: number, public internalName: string, public values: ScatterPresetValues) {
  }
}

@Component({
  selector: 'object-terrain-editor',
  imports: [
    Button,
    Divider,
    SelectButton,
    TerrainObjectPositionComponent,
    InputNumber,
    FormsModule,
    Select,
    MultiSelect
],
  templateUrl: './object-terrain-editor.component.html'
})
export class ObjectTerrainEditorComponent extends EditorPanel implements OnInit, OnDestroy, ObjectBrushContext {
  /** Minimum click tolerance (m) for forgiving nearest-object selection, used when an object's own radius is smaller. */
  private static readonly MIN_SELECT_DISTANCE = 4;
  /** Default name given to a freshly created scatter preset (the user can rename it). */
  private static readonly NEW_SCATTER_PRESET_NAME = 'Scatter';
  private readonly discRadiusMaterial: SimpleMaterial;
  terrainObjectConfigs: { objectNameId: ObjectNameId, name: string }[] = [];
  newTerrainObjectConfig: any;
  /** Object types the scatter brush draws from (multi-select); each placement picks one at random. */
  selectedScatterConfigs: { objectNameId: ObjectNameId, name: string }[] = [];
  /** All server-persisted scatter presets, wrapped for the preset dropdown. */
  scatterPresets: { name: string, value: ScatterPreset }[] = [];
  /** Currently selected scatter preset (drives the brush params + object selection). */
  activeScatterPreset: { name: string, value: ScatterPreset } | null = null;
  onOffOptions: any[] = [{label: 'Off', value: false}, {label: 'On', value: true}];
  /** Scatter slope-filter side: place on flat ground (below threshold) or on steep ground (above). */
  slopeAboveOptions: any[] = [{label: 'Below', value: false}, {label: 'Above', value: true}];
  /** Scatter water-filter side: place underwater (below level) or on land (above level). */
  waterAboveOptions: any[] = [{label: 'Underwater', value: false}, {label: 'On land', value: true}];
  newTerrainObjectMode: boolean = false;
  selectedNode: TransformNode | null = null;
  selectedTerrainObject: TerrainObjectConfig | null = null;
  selectedRadius: number = 0;
  selectedRadiusShow: boolean = false;
  selectedRadiusDisc: Mesh | null = null;
  // Edit mode always moves the selected object by dragging it across the ground (world axes, height
  // snapped to terrain); the rotation/scale gizmo is still available, the position arrows are not.
  private draggingNode: TransformNode | null = null;
  brushModeOptions: { label: string, value: ObjectBrushMode }[] = [
    {label: 'Edit', value: 'EDIT'},
    {label: 'Delete', value: 'DELETE'},
    {label: 'Scatter', value: 'SCATTER'},
    {label: 'Push', value: 'PUSH'}
  ];
  brushMode: ObjectBrushMode = 'EDIT';
  deleteBrush: DeleteObjectBrush;
  scatterBrush: ScatterObjectBrush;
  pushBrush: PushObjectBrush;
  private brushCursorDisc: Mesh | null = null;
  private painting: boolean = false;
  private lastStrokePosition: Vector3 | null = null;
  @ViewChild('terrainObjectPosition')
  terrainObjectPositionComponent!: TerrainObjectPositionComponent;
  private mouseObservable: Nullable<Observer<PointerInfo>> = null;
  private newTerrainObjects: TerrainObjectPosition[] = [];
  private updatedTerrainObjects: TerrainObjectPosition[] = [];
  private deletedTerrainObjectsIds: number[] = [];
  gizmoManager: GizmoManager;
  private selectionTransformNodeObservable: Nullable<Observer<TransformNode>> = null;
  private terrainEditorControllerClient: TerrainEditorControllerClient;
  private terrainObjectEditorControllerClient: TerrainObjectEditorControllerClient;
  private scatterBrushControllerClient: ScatterBrushControllerClient;
  /** After create/save, select the preset with this id once the list has been reloaded. */
  private pendingScatterPresetId: number | null = null;

  constructor(private gwtAngularService: GwtAngularService,
              private messageService: MessageService,
              private babylonModelService: BabylonModelService,
              private babylonRenderServiceAccess: BabylonRenderServiceAccessImpl,
              private editorService: EditorService,
              private httpClient: HttpClient) {
    super();
    this.gizmoManager = new GizmoManager(babylonRenderServiceAccess.getScene());
    // Position is done by dragging on the ground (not the rotation-aligned arrows); keep it off.
    this.gizmoManager.positionGizmoEnabled = false;
    this.gizmoManager.rotationGizmoEnabled = false;
    this.gizmoManager.scaleGizmoEnabled = false;
    this.gizmoManager.boundingBoxGizmoEnabled = false;
    this.gizmoManager.usePointerToAttachGizmos = false;
    this.discRadiusMaterial = new SimpleMaterial(`Radius`, babylonRenderServiceAccess.getScene());
    this.discRadiusMaterial.diffuseColor = Color3.Yellow();
    this.discRadiusMaterial.backFaceCulling = false;

    this.terrainEditorControllerClient = new TerrainEditorControllerClient(TypescriptGenerator.generateHttpClientAdapter(httpClient));
    this.terrainObjectEditorControllerClient = new TerrainObjectEditorControllerClient(TypescriptGenerator.generateHttpClientAdapter(httpClient));
    this.scatterBrushControllerClient = new ScatterBrushControllerClient(TypescriptGenerator.generateHttpClientAdapter(httpClient));

    this.deleteBrush = new DeleteObjectBrush(this);
    this.scatterBrush = new ScatterObjectBrush(this);
    this.pushBrush = new PushObjectBrush(this);
  }


  ngOnInit(): void {
    this.terrainObjectEditorControllerClient
      .getObjectNameIds()
      .then(objectNameIds => {
        this.terrainObjectConfigs = [];
        objectNameIds.forEach(objectNameId => {
          this.terrainObjectConfigs.push({
            name: `${objectNameId.internalName} '${objectNameId.id}'`,
            objectNameId: objectNameId
          })
        });
        this.newTerrainObjectConfig = this.terrainObjectConfigs[0];
        this.loadScatterPresets();
      })
  }

  ngOnDestroy(): void {
    this.deactivate();
    this.hideBrushCursor();
  }

  public activate() {
    this.mouseObservable = this.babylonRenderServiceAccess.getScene().onPointerObservable.add((pointerInfo) => {
      if (!this.gwtAngularService.gwtAngularFacade.inputService) {
        return;
      }
      if (this.brushMode !== 'EDIT') {
        this.handleBrushPointer(pointerInfo);
        return;
      }
      switch (pointerInfo.type) {
        case PointerEventTypes.POINTERDOWN: {
          this.selectedNode = null;
          this.draggingNode = null;

          // 1) Precise mesh pick: if it lands directly on a terrain object, select that one.
          let objectNode: TransformNode | null = null;
          const meshPick = this.babylonRenderServiceAccess.setupMeshPickPoint();
          if (meshPick.hit && meshPick.pickedMesh) {
            const node = BabylonRenderServiceAccessImpl.findRazarionMetadataNode(meshPick.pickedMesh);
            if (node && BabylonRenderServiceAccessImpl.getRazarionMetadata(node)?.type == RazarionMetadataType.TERRAIN_OBJECT) {
              objectNode = <TransformNode>node;
            }
          }

          // Ground point under the cursor (used for the nearest-object fallback and for placement).
          const groundPick = this.babylonRenderServiceAccess.setupTerrainPickPoint();
          const groundPoint = groundPick?.pickedPoint ?? null;

          // 2) Forgiving fallback (selection mode = "Mouse" off): hardware-instanced objects like
          //    stones are hard to hit precisely, so select the nearest object within its radius.
          if (!objectNode && !this.newTerrainObjectMode && groundPoint) {
            objectNode = this.pickNearestTerrainObject(groundPoint);
          }

          if (objectNode) {
            this.selectActiveTerrainObject(objectNode, false);
            // Start a ground drag: subsequent moves reposition the node in world space.
            this.draggingNode = objectNode;
            return;
          }

          // 3) Create new on ground click (placement mode = "Mouse" on).
          if (this.newTerrainObjectMode && groundPoint) {
            const placePoint = groundPoint;
            let terrainObjectConfig = this.gwtAngularService.gwtAngularFacade.terrainTypeService.getTerrainObjectConfig(this.newTerrainObjectConfig.objectNameId.id);
            if (!terrainObjectConfig.getModel3DId()) {
              throw new Error(`TerrainObjectConfig has no model3DId: ${terrainObjectConfig.toString()}`);
            }

            let terrainObjectModel = new class implements TerrainObjectModel {
              position = GwtInstance.newVertex(placePoint.x, placePoint.z, placePoint.y);
              rotation = GwtInstance.newVertex(0, 0, 0);
              scale = GwtInstance.newVertex(1, 1, 1);
              terrainObjectId = -1;
            }
            let newTerrainObjectMesh = BabylonTerrainTileImpl.createTerrainObject(terrainObjectModel, terrainObjectConfig, this.babylonModelService, null);
            this.selectActiveTerrainObject(<TransformNode>newTerrainObjectMesh, true)
          }
          break;
        }
        case PointerEventTypes.POINTERMOVE: {
          // Drag-on-ground move: while the left button is held, the object follows the ground point
          // under the cursor (world axes, independent of its rotation), with its height snapped to the
          // terrain. setupTerrainPickPoint only hits ground meshes, so the object never blocks the pick.
          // Skip (don't cancel) frames without the left button so a single stray buttons=0 event can't
          // kill the drag; POINTERUP is what ends it.
          if (this.draggingNode && (pointerInfo.event.buttons & 0x01) === 0x01) {
            const pickingInfo = this.babylonRenderServiceAccess.setupTerrainPickPoint();
            if (pickingInfo?.pickedPoint) {
              this.draggingNode.setAbsolutePosition(pickingInfo.pickedPoint.clone());
            }
          }
          break;
        }
        case PointerEventTypes.POINTERUP: {
          this.draggingNode = null;
          break;
        }
      }
    });
  }

  /**
   * Pointer handling for the painting brushes (Delete / Scatter / Push): apply on press and on
   * drag, with evenly-spaced dabs interpolated between samples so the result is independent of mouse
   * speed (same pattern as the height-map editor's stroke). The cursor disc tracks the ground.
   */
  private handleBrushPointer(pointerInfo: PointerInfo): void {
    switch (pointerInfo.type) {
      case PointerEventTypes.POINTERDOWN: {
        const pickingInfo = this.babylonRenderServiceAccess.setupTerrainPickPoint();
        if (pickingInfo?.pickedPoint) {
          this.painting = true;
          this.lastStrokePosition = null;
          this.applyBrushStroke(pickingInfo.pickedPoint);
          this.updateBrushCursor(pickingInfo.pickedPoint);
        }
        break;
      }
      case PointerEventTypes.POINTERMOVE: {
        const pickingInfo = this.babylonRenderServiceAccess.setupTerrainPickPoint();
        const pickedPoint = pickingInfo?.pickedPoint ?? null;
        this.updateBrushCursor(pickedPoint);
        if (this.painting && pickedPoint && (pointerInfo.event.buttons & 0x01) === 0x01) {
          this.applyBrushStroke(pickedPoint);
        } else {
          this.painting = false;
          this.lastStrokePosition = null;
        }
        break;
      }
      case PointerEventTypes.POINTERUP: {
        this.painting = false;
        this.lastStrokePosition = null;
        break;
      }
    }
  }

  private activeBrush(): AbstractObjectBrush | null {
    switch (this.brushMode) {
      case 'DELETE':
        return this.deleteBrush;
      case 'SCATTER':
        return this.scatterBrush;
      case 'PUSH':
        return this.pushBrush;
      default:
        return null;
    }
  }

  private applyBrushStroke(target: Vector3): void {
    const brush = this.activeBrush();
    if (!brush) {
      return;
    }
    const from = this.lastStrokePosition;
    if (!from) {
      brush.apply(target);
      this.lastStrokePosition = target.clone();
      return;
    }
    const dx = target.x - from.x;
    const dz = target.z - from.z;
    const dist = Math.sqrt(dx * dx + dz * dz);
    const spacing = Math.max(0.5, brush.radius * 0.34);
    const steps = Math.min(50, Math.max(1, Math.floor(dist / spacing)));
    for (let i = 1; i <= steps; i++) {
      const t = i / steps;
      brush.apply(new Vector3(from.x + dx * t, 0, from.z + dz * t));
    }
    this.lastStrokePosition = target.clone();
  }

  onBrushModeChange(): void {
    this.painting = false;
    this.lastStrokePosition = null;
    this.draggingNode = null;
    if (this.brushMode !== 'EDIT') {
      // Leave the gizmo/selection world when switching to a painting brush.
      this.clearSelection();
    } else {
      this.hideBrushCursor();
    }
  }

  private updateBrushCursor(pickedPoint: Vector3 | null): void {
    const brush = this.activeBrush();
    if (!brush || !pickedPoint) {
      this.hideBrushCursor();
      return;
    }
    if (!this.brushCursorDisc) {
      this.brushCursorDisc = MeshBuilder.CreateDisc("Brush Cursor", {radius: 1}, this.babylonRenderServiceAccess.getScene());
      this.brushCursorDisc.material = this.discRadiusMaterial;
      this.brushCursorDisc.rotation.x = Tools.ToRadians(90);
      this.brushCursorDisc.isPickable = false;
    }
    this.brushCursorDisc.scaling.x = brush.radius;
    this.brushCursorDisc.scaling.y = brush.radius;
    this.brushCursorDisc.position.set(pickedPoint.x, pickedPoint.y + 0.05, pickedPoint.z);
  }

  private hideBrushCursor(): void {
    if (this.brushCursorDisc) {
      this.brushCursorDisc.dispose();
      this.brushCursorDisc = null;
    }
  }

  public deactivate() {
    this.babylonRenderServiceAccess.getScene().onPointerObservable.remove(this.mouseObservable);
    this.painting = false;
    this.lastStrokePosition = null;
    this.hideBrushCursor();
    this.clearSelection();
  }

  // --- ObjectBrushContext -------------------------------------------------------------------------

  groundHeightAt(x: number, z: number): number | null {
    return this.babylonRenderServiceAccess.getTerrainHeightAt(x, z);
  }

  terrainObjectNodes(): TransformNode[] {
    return this.babylonRenderServiceAccess.getScene().transformNodes.filter(node => {
      const metadata = BabylonRenderServiceAccessImpl.getRazarionMetadata(node);
      return metadata?.type === RazarionMetadataType.TERRAIN_OBJECT;
    });
  }

  configIdOf(node: TransformNode): number | undefined {
    return BabylonRenderServiceAccessImpl.getRazarionMetadata(node)?.configId;
  }

  /**
   * Returns the terrain-object node closest to the given ground point, within its selection radius
   * (the object's configured radius, but at least MIN_SELECT_DISTANCE so tiny objects stay clickable).
   * Makes selection forgiving for hardware-instanced objects (e.g. stones) that are hard to hit exactly.
   */
  private pickNearestTerrainObject(groundPoint: Vector3): TransformNode | null {
    let best: TransformNode | null = null;
    let bestDistance = Number.POSITIVE_INFINITY;
    for (const node of this.terrainObjectNodes()) {
      const position = node.absolutePosition;
      const dx = position.x - groundPoint.x;
      const dz = position.z - groundPoint.z;
      const distance = Math.sqrt(dx * dx + dz * dz);
      let threshold = ObjectTerrainEditorComponent.MIN_SELECT_DISTANCE;
      const configId = this.configIdOf(node);
      if (configId != null) {
        const radius = this.gwtAngularService.gwtAngularFacade.terrainTypeService.getTerrainObjectConfig(configId).getRadius();
        if (radius > threshold) {
          threshold = radius;
        }
      }
      if (distance <= threshold && distance < bestDistance) {
        bestDistance = distance;
        best = node;
      }
    }
    return best;
  }

  selectedConfigId(): number | null {
    return this.newTerrainObjectConfig?.objectNameId?.id ?? null;
  }

  scatterConfigIds(): number[] {
    if (this.selectedScatterConfigs.length > 0) {
      return this.selectedScatterConfigs.map(config => config.objectNameId.id);
    }
    // Fall back to the single "Object" dropdown so the brush still works without an explicit multi-select.
    const single = this.selectedConfigId();
    return single !== null ? [single] : [];
  }

  /** Loads all scatter presets from the server and applies the selected (or first) one to the brush. */
  private loadScatterPresets(): void {
    this.scatterBrushControllerClient.readAll()
      .then(entities => {
        this.scatterPresets = (entities ?? []).map(entity => {
          const values = this.parsePresetJson(entity.presetJson);
          return {
            name: `${entity.internalName} (${entity.id})`,
            value: new ScatterPreset(entity.id, entity.internalName, values)
          };
        });
        if (this.pendingScatterPresetId !== null) {
          this.activeScatterPreset = this.scatterPresets.find(p => p.value.id === this.pendingScatterPresetId) ?? null;
          this.pendingScatterPresetId = null;
        }
        if (!this.activeScatterPreset && this.scatterPresets.length > 0) {
          this.activeScatterPreset = this.scatterPresets[0];
        }
        this.applyActiveScatterPreset();
      })
      .catch(reason => this.onScatterPresetError('Failed loading scatter presets', reason));
  }

  /** Pushes the active preset's stored values onto the live brush + object selection. */
  onScatterPresetChange(): void {
    this.applyActiveScatterPreset();
  }

  private applyActiveScatterPreset(): void {
    if (!this.activeScatterPreset) {
      return;
    }
    const values = this.activeScatterPreset.value.values;
    this.scatterBrush.radius = values.radius;
    this.scatterBrush.countPerDab = values.countPerDab;
    this.scatterBrush.minDistance = values.minDistance;
    this.scatterBrush.minScale = values.minScale;
    this.scatterBrush.maxScale = values.maxScale;
    this.scatterBrush.yawJitterDeg = values.yawJitterDeg;
    this.scatterBrush.slopeFilterEnabled = values.slopeFilterEnabled;
    this.scatterBrush.slopeThreshold = values.slopeThreshold;
    this.scatterBrush.slopeAbove = values.slopeAbove;
    this.scatterBrush.waterFilterEnabled = values.waterFilterEnabled;
    this.scatterBrush.waterLevel = values.waterLevel;
    this.scatterBrush.waterAbove = values.waterAbove;
    this.selectedScatterConfigs = this.terrainObjectConfigs
      .filter(config => values.configIds.includes(config.objectNameId.id));
  }

  /** Creates a new (empty) scatter preset on the server and selects it once reloaded. */
  onCreateScatterPreset(): void {
    this.scatterBrushControllerClient.create()
      .then(created => {
        // Give it a default name + the current brush values so it is immediately usable.
        const entity: ScatterBrushEntity = {
          id: created.id,
          internalName: ObjectTerrainEditorComponent.NEW_SCATTER_PRESET_NAME,
          presetJson: JSON.stringify(this.currentScatterValues())
        };
        this.pendingScatterPresetId = created.id;
        return this.scatterBrushControllerClient.update(entity);
      })
      .then(() => this.loadScatterPresets())
      .catch(reason => this.onScatterPresetError('Failed creating scatter preset', reason));
  }

  /** Saves the current brush params + object selection into the active preset. */
  onSaveScatterPreset(): void {
    if (!this.activeScatterPreset) {
      this.messageService.add({severity: 'error', summary: 'No scatter preset selected', sticky: true});
      return;
    }
    const entity: ScatterBrushEntity = {
      id: this.activeScatterPreset.value.id,
      internalName: this.activeScatterPreset.value.internalName,
      presetJson: JSON.stringify(this.currentScatterValues())
    };
    this.pendingScatterPresetId = entity.id;
    this.scatterBrushControllerClient.update(entity)
      .then(() => {
        this.messageService.add({severity: 'success', life: 300, summary: 'Scatter preset saved'});
        this.loadScatterPresets();
      })
      .catch(reason => this.onScatterPresetError('Failed saving scatter preset', reason));
  }

  /** Deletes the active preset, then selects whatever remains. */
  onDeleteScatterPreset(): void {
    if (!this.activeScatterPreset) {
      return;
    }
    const deletedId = this.activeScatterPreset.value.id;
    this.scatterBrushControllerClient.delete(deletedId)
      .then(() => {
        this.activeScatterPreset = this.scatterPresets.find(p => p.value.id !== deletedId) ?? null;
        this.loadScatterPresets();
      })
      .catch(reason => this.onScatterPresetError('Failed deleting scatter preset', reason));
  }

  private currentScatterValues(): ScatterPresetValues {
    return {
      configIds: this.selectedScatterConfigs.map(config => config.objectNameId.id),
      radius: this.scatterBrush.radius,
      countPerDab: this.scatterBrush.countPerDab,
      minDistance: this.scatterBrush.minDistance,
      minScale: this.scatterBrush.minScale,
      maxScale: this.scatterBrush.maxScale,
      yawJitterDeg: this.scatterBrush.yawJitterDeg,
      slopeFilterEnabled: this.scatterBrush.slopeFilterEnabled,
      slopeThreshold: this.scatterBrush.slopeThreshold,
      slopeAbove: this.scatterBrush.slopeAbove,
      waterFilterEnabled: this.scatterBrush.waterFilterEnabled,
      waterLevel: this.scatterBrush.waterLevel,
      waterAbove: this.scatterBrush.waterAbove
    };
  }

  private parsePresetJson(presetJson: string): ScatterPresetValues {
    const fallback = this.currentScatterValues();
    try {
      const parsed = JSON.parse(presetJson) ?? {};
      return {
        configIds: Array.isArray(parsed.configIds) ? parsed.configIds : [],
        radius: typeof parsed.radius === 'number' ? parsed.radius : fallback.radius,
        countPerDab: typeof parsed.countPerDab === 'number' ? parsed.countPerDab : fallback.countPerDab,
        minDistance: typeof parsed.minDistance === 'number' ? parsed.minDistance : fallback.minDistance,
        minScale: typeof parsed.minScale === 'number' ? parsed.minScale : fallback.minScale,
        maxScale: typeof parsed.maxScale === 'number' ? parsed.maxScale : fallback.maxScale,
        yawJitterDeg: typeof parsed.yawJitterDeg === 'number' ? parsed.yawJitterDeg : fallback.yawJitterDeg,
        slopeFilterEnabled: typeof parsed.slopeFilterEnabled === 'boolean' ? parsed.slopeFilterEnabled : fallback.slopeFilterEnabled,
        slopeThreshold: typeof parsed.slopeThreshold === 'number' ? parsed.slopeThreshold : fallback.slopeThreshold,
        slopeAbove: typeof parsed.slopeAbove === 'boolean' ? parsed.slopeAbove : fallback.slopeAbove,
        waterFilterEnabled: typeof parsed.waterFilterEnabled === 'boolean' ? parsed.waterFilterEnabled : fallback.waterFilterEnabled,
        waterLevel: typeof parsed.waterLevel === 'number' ? parsed.waterLevel : fallback.waterLevel,
        waterAbove: typeof parsed.waterAbove === 'boolean' ? parsed.waterAbove : fallback.waterAbove
      };
    } catch (error) {
      console.error('Failed to parse scatter preset', error);
      return {...fallback, configIds: []};
    }
  }

  private onScatterPresetError(summary: string, reason: any): void {
    console.error(summary, reason);
    this.messageService.add({
      severity: 'error',
      summary,
      detail: reason?.message || `${JSON.stringify(reason)}`,
      sticky: true
    });
  }

  createObject(configId: number, x: number, z: number, height: number, yaw: number, scale: number, tiltNormal?: Vector3): void {
    const config = this.gwtAngularService.gwtAngularFacade.terrainTypeService.getTerrainObjectConfig(configId);
    if (!config.getModel3DId()) {
      return;
    }
    const rotation = this.buildRotation(yaw, tiltNormal);
    const terrainObjectModel = new class implements TerrainObjectModel {
      position = GwtInstance.newVertex(x, z, height);
      rotation = rotation;
      scale = GwtInstance.newVertex(scale, scale, scale);
      terrainObjectId = -1;
    };
    const node = BabylonTerrainTileImpl.createTerrainObject(terrainObjectModel, config, this.babylonModelService, null);

    const terrainObjectPosition = GeneratedRestHelper.newTerrainObjectPosition();
    terrainObjectPosition.terrainObjectConfigId = configId;
    this.updateTerrainObjectPosition(node, terrainObjectPosition);
    this.newTerrainObjects.push(terrainObjectPosition);

    const metadata = BabylonRenderServiceAccessImpl.getRazarionMetadata(node);
    if (metadata) {
      // Marks the node as pending-new so deleteObjectNode removes it from newTerrainObjects rather
      // than scheduling a server delete.
      metadata.editorHintTerrainObjectPosition = terrainObjectPosition;
    }
  }

  deleteObjectNode(node: TransformNode): void {
    const metadata = BabylonRenderServiceAccessImpl.getRazarionMetadata(node);
    if (metadata?.editorHintTerrainObjectPosition) {
      // Pending-new object: just drop it from the create list.
      const index = this.newTerrainObjects.indexOf(metadata.editorHintTerrainObjectPosition);
      if (index >= 0) {
        this.newTerrainObjects.splice(index, 1);
      }
    } else if (metadata && metadata.id != null) {
      // Persisted object: schedule a server delete and drop any pending update for it.
      const id = metadata.id;
      if (!this.deletedTerrainObjectsIds.includes(id)) {
        this.deletedTerrainObjectsIds.push(id);
      }
      const updateIndex = this.updatedTerrainObjects.findIndex(o => o.id === id);
      if (updateIndex >= 0) {
        this.updatedTerrainObjects.splice(updateIndex, 1);
      }
    }
    if (this.selectedNode === node) {
      this.clearSelection();
    }
    node.dispose();
  }

  moveObjectNode(node: TransformNode, x: number, z: number): void {
    const position = this.pendingPositionFor(node);
    if (!position) {
      return; // not a tracked terrain object (no id, not pending-new) — leave it alone
    }
    const height = this.babylonRenderServiceAccess.getTerrainHeightAt(x, z);
    node.position.set(x, height ?? node.position.y, z);
    this.updateTerrainObjectPosition(node, position);
  }

  /**
   * Returns the pending TerrainObjectPosition that should track this node's transform, creating an
   * update entry on first touch for a persisted object. null if the node carries no terrain-object id
   * and is not a pending-new object.
   */
  private pendingPositionFor(node: TransformNode): TerrainObjectPosition | null {
    const metadata = BabylonRenderServiceAccessImpl.getRazarionMetadata(node);
    if (!metadata) {
      return null;
    }
    if (metadata.editorHintTerrainObjectPosition) {
      return metadata.editorHintTerrainObjectPosition; // pending-new
    }
    if (metadata.id != null) {
      let position = this.findUpdatedTerrainObjectPosition(metadata.id);
      if (!position) {
        position = GeneratedRestHelper.newTerrainObjectPosition();
        position.id = metadata.id;
        position.terrainObjectConfigId = metadata.configId!;
        this.updatedTerrainObjects.push(position);
      }
      return position;
    }
    return null;
  }

  /**
   * Builds the model-space rotation Vertex. createTerrainObject maps it to the mesh as
   * mesh.rotation = (model.getX(), model.getZ(), model.getY()). For a plain yaw we rotate about the
   * vertical (mesh Y) axis; with a tilt normal we align the up-axis to the normal, combine with the
   * yaw and convert back to mesh-space Euler angles.
   */
  private buildRotation(yaw: number, tiltNormal?: Vector3): Vertex {
    if (!tiltNormal) {
      return GwtInstance.newVertex(0, 0, yaw);
    }
    const up = Vector3.Up();
    const normal = tiltNormal.clone().normalize();
    const dot = Math.min(1, Math.max(-1, Vector3.Dot(up, normal)));
    let tiltQuaternion: Quaternion;
    if (dot > 0.99999) {
      tiltQuaternion = Quaternion.Identity();
    } else {
      const axis = Vector3.Cross(up, normal).normalize();
      tiltQuaternion = Quaternion.RotationAxis(axis, Math.acos(dot));
    }
    const yawQuaternion = Quaternion.RotationAxis(Vector3.Up(), yaw);
    const euler = tiltQuaternion.multiply(yawQuaternion).toEulerAngles();
    return GwtInstance.newVertex(euler.x, euler.z, euler.y);
  }

  private clearGizmos() {
    this.gizmoManager.attachToNode(null);
  }

  private setupGizmo(node: Node) {
    // Position is handled by ground-drag, so the position gizmo stays off (constructor). The
    // rotation/scale gizmos are still attached here for when the user toggles them on.
    this.clearGizmos();
    this.gizmoManager.attachToNode(node);
  }

  save() {
    let terrainEditorUpdate: TerrainEditorUpdate = {
      createdTerrainObjects: this.newTerrainObjects,
      updatedTerrainObjects: this.updatedTerrainObjects,
      deletedTerrainObjectsIds: this.deletedTerrainObjectsIds
    }
    this.terrainEditorControllerClient
      .updateTerrain(this.editorService.getPlanetId(), terrainEditorUpdate)
      .then(() => {
        this.newTerrainObjects = [];
        this.updatedTerrainObjects = [];
        this.deletedTerrainObjectsIds = [];
        this.clearSelection();
        this.messageService.add({
          severity: 'success',
          life: 300,
          summary: 'Terrain objects saved'
        })
      }).catch(reason => {
      console.error(reason);
      this.messageService.add({
        severity: 'error',
        summary: `Save terrain objects failed`,
        detail: reason.message || `${JSON.stringify(reason)}`,
        sticky: true
      });
    })
  }

  private selectActiveTerrainObject(node: TransformNode, isNew: boolean) {
    this.setupGizmo(node);

    this.selectedNode = node;
    let terrainObjectPosition: TerrainObjectPosition;

    if (isNew) {
      terrainObjectPosition = GeneratedRestHelper.newTerrainObjectPosition();
      terrainObjectPosition.terrainObjectConfigId = this.newTerrainObjectConfig.objectNameId.id;
      this.updateTerrainObjectPosition(node, terrainObjectPosition);
      let razarionMetadata = BabylonRenderServiceAccessImpl.getRazarionMetadata(node);
      razarionMetadata!.editorHintTerrainObjectPosition = terrainObjectPosition;
      this.newTerrainObjects.push(terrainObjectPosition);
      this.selectedTerrainObject = this.gwtAngularService.gwtAngularFacade.terrainTypeService.getTerrainObjectConfig(this.newTerrainObjectConfig.objectNameId.id);
      this.selectedRadius = this.selectedTerrainObject.getRadius();
      this.showHideRadius();
    } else {
      let razarionMetadata = BabylonRenderServiceAccessImpl.getRazarionMetadata(node);
      if (razarionMetadata!.id) {
        let to = this.findUpdatedTerrainObjectPosition(razarionMetadata!.id);
        if (!to) {
          terrainObjectPosition = GeneratedRestHelper.newTerrainObjectPosition();
          terrainObjectPosition.id = razarionMetadata?.id!;
          terrainObjectPosition.terrainObjectConfigId = razarionMetadata?.configId!;
          this.updatedTerrainObjects.push(terrainObjectPosition);
        } else {
          terrainObjectPosition = to;
        }
      } else {
        terrainObjectPosition = razarionMetadata!.editorHintTerrainObjectPosition!;
      }
      this.updateTerrainObjectPosition(node, terrainObjectPosition);
      this.selectedTerrainObject = this.gwtAngularService.gwtAngularFacade.terrainTypeService.getTerrainObjectConfig(razarionMetadata!.configId!);
      this.selectedRadius = this.selectedTerrainObject.getRadius();
      this.showHideRadius();
    }

    if (this.terrainObjectPositionComponent.getTransformNode() !== node) {
      if (this.selectionTransformNodeObservable) {
        this.terrainObjectPositionComponent.getTransformNode()!.onAfterWorldMatrixUpdateObservable.remove(this.selectionTransformNodeObservable!);
      }
      this.selectionTransformNodeObservable = node.onAfterWorldMatrixUpdateObservable.add(() => {
        this.updateTerrainObjectPosition(node, terrainObjectPosition);
      });
    }

    this.terrainObjectPositionComponent.setSelected(node, terrainObjectPosition);
  }

  private findUpdatedTerrainObjectPosition(id: number): TerrainObjectPosition | undefined {
    return this.updatedTerrainObjects.find((o) => {
      return o.id === id
    });
  }

  private updateTerrainObjectPosition(node: TransformNode, terrainObjectPosition: TerrainObjectPosition) {
    terrainObjectPosition.position = GeneratedRestHelper.newDecimalPosition(node.position.x, node.position.z);
    terrainObjectPosition.rotation = GeneratedRestHelper.newVertex(node.rotation.x, node.rotation.z, node.rotation.y);
    terrainObjectPosition.scale = GeneratedRestHelper.newVertex(node.scaling.x, node.scaling.z, node.scaling.y);
  }

  public clearSelection() {
    this.clearGizmos();
    // The position component only exists in EDIT mode (it lives inside an @if), so it is undefined when
    // clearSelection runs after switching to a painting brush or on deactivate — guard every access.
    const positionComponent = this.terrainObjectPositionComponent;
    if (positionComponent) {
      if (this.selectionTransformNodeObservable && positionComponent.getTransformNode()) {
        positionComponent.getTransformNode()!.onAfterWorldMatrixUpdateObservable.remove(this.selectionTransformNodeObservable!);
      }
      positionComponent.clearSelection();
    }
    this.selectionTransformNodeObservable = null;
    this.showHideRadius();
    this.selectedRadiusShow = false;
    this.selectedTerrainObject = null;
    this.newTerrainObjectMode = false;
    this.selectedNode = null;
    this.draggingNode = null;
  }

  restartPlanetWarm() {
    this.editorService.executeServerCommand(EditorService.RESTART_PLANET_WARM);
  }

  restartPlanetCold() {
    this.editorService.executeServerCommand(EditorService.RESTART_PLANET_COLD);
  }

  showHideRadius(): void {
    if (this.selectedRadiusDisc) {
      this.selectedRadiusDisc.dispose();
    }
    if (this.selectedRadiusShow && this.selectedTerrainObject) {
      this.selectedRadiusDisc = MeshBuilder.CreateDisc("Radius Disc", {radius: 1});
      this.selectedRadiusDisc.material = this.discRadiusMaterial;
      this.selectedRadiusDisc.position.y = 0.01;
      this.selectedRadiusDisc.rotation.x = Tools.ToRadians(90);
      this.selectedRadiusDisc.scaling.x = this.selectedRadius;
      this.selectedRadiusDisc.scaling.y = this.selectedRadius;
      this.selectedRadiusDisc.parent = this.selectedNode;
    }
  }

  onSelectedRadiusChange(value: any) {
    if (this.selectedRadiusDisc) {
      this.selectedRadiusDisc.scaling.x = value;
      this.selectedRadiusDisc.scaling.y = value;
    }
  }

  saveRadius() {
    if (!this.selectedTerrainObject) {
      this.messageService.add({
        severity: 'error',
        summary: `Nothing Selected`,
        sticky: true
      });
      return;
    }
    const url = `${UPDATE_RADIUS_REST_CALL}/${this.selectedTerrainObject.getId()}/${this.selectedRadius}`
    this.httpClient.post(url, null).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          life: 300,
          summary: `Radius updated ${this.selectedTerrainObject?.getInternalName()}`
        });
      },
      error: error => {
        this.messageService.add({
          severity: 'error',
          summary: `Error calling: ${url}`,
          detail: error,
          sticky: true
        });
      }
    });

  }

}
