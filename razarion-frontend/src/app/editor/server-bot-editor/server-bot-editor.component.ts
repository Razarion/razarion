import {Component, OnDestroy, OnInit} from '@angular/core';
import {EditorPanel} from "../editor-model";
import {EditorService} from "../editor-service";
import {
  BotConfig,
  BotEnragementStateConfig,
  BotItemConfig,
  DecimalPosition,
  ServerGameEngineConfigEntity
} from "../../generated/razarion-share";
import {BabylonTerrainTileImpl} from '../../game/renderer/babylon-terrain-tile.impl';
import {PlaceConfigComponent} from '../common/place-config/place-config.component';
import {InputNumberModule} from 'primeng/inputnumber';
import {FormsModule} from '@angular/forms';
import {Checkbox} from 'primeng/checkbox';
import {BaseItemTypeComponent} from '../common/base-item-type/base-item-type.component';
import {ButtonModule} from 'primeng/button';
import {Accordion, AccordionModule,} from 'primeng/accordion';

import {Divider} from 'primeng/divider';
import {Select} from 'primeng/select';
import {MessageService} from 'primeng/api';
import {ScrollPanelModule} from 'primeng/scrollpanel';
import {Model3dComponent} from '../common/model3d/model3d.component';
import {BotGroundEditorService} from './bot-ground-editor.service';
import {ToggleButtonModule} from 'primeng/togglebutton';

@Component({
  selector: 'server-bot-editor',
  imports: [
    PlaceConfigComponent,
    InputNumberModule,
    FormsModule,
    Checkbox,
    BaseItemTypeComponent,
    ButtonModule,
    Accordion,
    Divider,
    Select,
    AccordionModule,
    ScrollPanelModule,
    Model3dComponent,
    ToggleButtonModule
],
  templateUrl: './server-bot-editor.component.html'
})
export class ServerBotEditorComponent extends EditorPanel implements OnInit, OnDestroy {
  serverGameEngineConfigEntity!: ServerGameEngineConfigEntity;
  selectedBot?: BotConfig;
  showGroundEditor = false;
  slopeModeGroundEditor = false;

  constructor(public editorService: EditorService,
              private messageService: MessageService,
              private botGroundEditorService: BotGroundEditorService) {
    super();
  }

  ngOnInit(): void {
    this.load();
  }

  ngOnDestroy(): void {
    if (this.showGroundEditor) {
      this.botGroundEditorService.deactivate(this.selectedBot!);
    }
  }

  private load(): void {
    this.editorService.readServerGameEngineConfig().then(serverGameEngineConfig => {
      this.serverGameEngineConfigEntity = serverGameEngineConfig;
    })
  }

  onSave() {
    this.editorService.updateBotConfig(this.serverGameEngineConfigEntity.botConfigs).catch(error => {
      console.error(error);
      this.messageService.add({
        severity: 'error',
        summary: `Can not save`,
        detail: error.message,
        sticky: true
      });
    });
  }

  protected readonly EditorService = EditorService;

  onCreate() {
    this.selectedBot = {
      id: <any>null,
      actionDelay: 3000,
      autoAttack: false,
      auxiliaryId: 0,
      botEnragementStateConfigs: [],
      internalName: "New",
      maxActiveMs: null,
      maxInactiveMs: null,
      minActiveMs: null,
      minInactiveMs: null,
      name: "",
      npc: false,
      realm: null,
      groundBoxHeight: null,
      groundBoxModel3DEntityId: null,
      groundBoxPositions: [],
      botGroundSlopeBoxes: [],
    }
    this.serverGameEngineConfigEntity!.botConfigs.push(this.selectedBot)
  }

  onCopy() {
    this.serverGameEngineConfigEntity.botConfigs.push(this.selectedBot!);
    this.editorService.updateBotConfig(this.serverGameEngineConfigEntity.botConfigs).then(() => {
      this.load();
    }).catch(error => {
      console.error(error);
      this.messageService.add({
        severity: 'error',
        summary: `Can not save`,
        detail: error.message,
        sticky: true
      });
    });
  }

  onDelete() {
    this.serverGameEngineConfigEntity!.botConfigs.splice(this.serverGameEngineConfigEntity!.botConfigs.findIndex(b => b === this.selectedBot), 1);
    this.selectedBot = undefined;
  }

  onCreateEnragementState() {
    this.selectedBot?.botEnragementStateConfigs.push({botItems: [], enrageUpKills: 0, name: ""})
  }

  onDeleteEnragementState(botEnragementStateConfig: BotEnragementStateConfig) {
    this.selectedBot?.botEnragementStateConfigs.splice(this.selectedBot?.botEnragementStateConfigs.findIndex(b => b === botEnragementStateConfig), 1);
  }

  onCreateBotItem(botEnragementStateConfig: BotEnragementStateConfig) {
    botEnragementStateConfig.botItems.push({
      angle: 0,
      baseItemTypeId: null,
      count: 0,
      createDirectly: false,
      idleTtl: null,
      moveRealmIfIdle: false,
      noRebuild: false,
      noSpawn: false,
      place: null,
      rePopTime: 0
    })
  }

  onDeleteBotItem(botItem: BotItemConfig, botEnragementStateConfig: BotEnragementStateConfig) {
    botEnragementStateConfig.botItems.splice(botEnragementStateConfig.botItems.findIndex(b => b === botItem), 1);
  }

  onOpenGroundEditor() {
    if (this.showGroundEditor) {
      this.botGroundEditorService.activate(this.selectedBot!);
    } else {
      this.botGroundEditorService.deactivate(this.selectedBot!);
    }
  }

  onHeightInput(height: number | string | null) {
    this.botGroundEditorService.setHeight(this.selectedBot!, height === null ? 0 : <number>height);
  }

  onSlopeModeGroundEditor() {
    this.botGroundEditorService.setSlopeMode(this.selectedBot!, this.slopeModeGroundEditor);
  }

  onRotationSlopeGroundEditor() {
    this.botGroundEditorService.rotationSlope(this.selectedBot!);
  }

  onGenerateGroundFromRealm() {
    if (!this.selectedBot?.realm) {
      this.messageService.add({
        severity: 'warn',
        summary: 'No Realm',
        detail: 'Bot has no realm configured'
      });
      return;
    }

    const realm = this.selectedBot.realm;
    const boxLength = BabylonTerrainTileImpl.BOT_BOX_LENGTH;
    const positions: DecimalPosition[] = [];

    if (realm.polygon2D?.corners && realm.polygon2D.corners.length >= 3) {
      const corners = realm.polygon2D.corners;
      let minX = Infinity, minY = Infinity, maxX = -Infinity, maxY = -Infinity;
      for (const corner of corners) {
        minX = Math.min(minX, corner.x);
        minY = Math.min(minY, corner.y);
        maxX = Math.max(maxX, corner.x);
        maxY = Math.max(maxY, corner.y);
      }

      const startX = Math.floor(minX / boxLength) * boxLength;
      const startY = Math.floor(minY / boxLength) * boxLength;

      for (let x = startX; x <= maxX; x += boxLength) {
        for (let y = startY; y <= maxY; y += boxLength) {
          if (this.isPointInPolygon({x, y}, corners)) {
            positions.push({x, y});
          }
        }
      }
    } else if (realm.position && realm.radius) {
      const cx = realm.position.x;
      const cy = realm.position.y;
      const r = realm.radius;

      const startX = Math.floor((cx - r) / boxLength) * boxLength;
      const startY = Math.floor((cy - r) / boxLength) * boxLength;

      for (let x = startX; x <= cx + r; x += boxLength) {
        for (let y = startY; y <= cy + r; y += boxLength) {
          const dx = x - cx;
          const dy = y - cy;
          if (dx * dx + dy * dy <= r * r) {
            positions.push({x, y});
          }
        }
      }
    }

    this.selectedBot.groundBoxPositions = positions;

    if (this.showGroundEditor) {
      this.botGroundEditorService.deactivate(this.selectedBot);
      this.botGroundEditorService.activate(this.selectedBot);
    }

    this.messageService.add({
      severity: 'info',
      summary: 'Ground generated',
      detail: `${positions.length} box positions generated from realm`
    });
  }

  onGenerateRealmFromGroundBoxes() {
    if (!this.selectedBot) {
      return;
    }

    const points: DecimalPosition[] = [];

    if (this.selectedBot.groundBoxPositions) {
      for (const pos of this.selectedBot.groundBoxPositions) {
        points.push(pos);
      }
    }

    if (this.selectedBot.botGroundSlopeBoxes) {
      for (const box of this.selectedBot.botGroundSlopeBoxes) {
        points.push({x: box.xPos, y: box.yPos});
      }
    }

    if (points.length < 1) {
      this.messageService.add({
        severity: 'warn',
        summary: 'Not enough points',
        detail: 'At least 1 ground box position is needed to generate a realm polygon'
      });
      return;
    }

    const boxLength = BabylonTerrainTileImpl.BOT_BOX_LENGTH;
    const halfBox = boxLength / 2;

    // Build grid occupancy set from box center positions
    const occupied = new Set<string>();
    const gridPositions: Array<{ gx: number, gy: number, x: number, y: number }> = [];

    for (const p of points) {
      const gx = Math.round(p.x / boxLength);
      const gy = Math.round(p.y / boxLength);
      const key = `${gx},${gy}`;
      if (!occupied.has(key)) {
        occupied.add(key);
        gridPositions.push({gx, gy, x: gx * boxLength, y: gy * boxLength});
      }
    }

    // Collect directed boundary edges (counterclockwise winding)
    type Edge = { fromX: number, fromY: number, toX: number, toY: number };
    const edges: Edge[] = [];

    for (const {gx, gy, x, y} of gridPositions) {
      if (!occupied.has(`${gx},${gy - 1}`)) { // bottom empty
        edges.push({fromX: x - halfBox, fromY: y - halfBox, toX: x + halfBox, toY: y - halfBox});
      }
      if (!occupied.has(`${gx + 1},${gy}`)) { // right empty
        edges.push({fromX: x + halfBox, fromY: y - halfBox, toX: x + halfBox, toY: y + halfBox});
      }
      if (!occupied.has(`${gx},${gy + 1}`)) { // top empty
        edges.push({fromX: x + halfBox, fromY: y + halfBox, toX: x - halfBox, toY: y + halfBox});
      }
      if (!occupied.has(`${gx - 1},${gy}`)) { // left empty
        edges.push({fromX: x - halfBox, fromY: y + halfBox, toX: x - halfBox, toY: y - halfBox});
      }
    }

    // Build adjacency: from vertex key → outgoing edges
    const adjacency = new Map<string, Edge[]>();
    for (const edge of edges) {
      const key = `${edge.fromX},${edge.fromY}`;
      if (!adjacency.has(key)) adjacency.set(key, []);
      adjacency.get(key)!.push(edge);
    }

    // Trace boundary loops, keep the longest (outer boundary)
    const visited = new Set<Edge>();
    let longestLoop: DecimalPosition[] = [];

    for (const edge of edges) {
      if (visited.has(edge)) continue;

      const loop: DecimalPosition[] = [];
      let current: Edge | undefined = edge;

      while (current && !visited.has(current)) {
        visited.add(current);
        loop.push({x: current.fromX, y: current.fromY});

        const toKey = `${current.toX},${current.toY}`;
        const nextEdges = adjacency.get(toKey);
        current = nextEdges?.find(e => !visited.has(e));
      }

      if (loop.length > longestLoop.length) {
        longestLoop = loop;
      }
    }

    // Remove collinear points (straight edges)
    const simplified = this.removeCollinearPoints(longestLoop);

    this.selectedBot.realm = {
      polygon2D: {corners: simplified, lines: []},
      position: null,
      radius: null
    };

    this.messageService.add({
      severity: 'info',
      summary: 'Realm generated',
      detail: `Realm polygon with ${simplified.length} corners generated from ${points.length} ground box positions`
    });
  }

  private removeCollinearPoints(points: DecimalPosition[]): DecimalPosition[] {
    if (points.length < 3) return points;
    const result: DecimalPosition[] = [];
    const n = points.length;
    for (let i = 0; i < n; i++) {
      const prev = points[(i - 1 + n) % n];
      const curr = points[i];
      const next = points[(i + 1) % n];
      const cross = (curr.x - prev.x) * (next.y - prev.y) - (curr.y - prev.y) * (next.x - prev.x);
      if (Math.abs(cross) > 0.001) {
        result.push(curr);
      }
    }
    return result;
  }

  private isPointInPolygon(point: DecimalPosition, polygon: DecimalPosition[]): boolean {
    let inside = false;
    for (let i = 0, j = polygon.length - 1; i < polygon.length; j = i++) {
      const xi = polygon[i].x, yi = polygon[i].y;
      const xj = polygon[j].x, yj = polygon[j].y;
      const intersect = ((yi > point.y) !== (yj > point.y))
        && (point.x < (xj - xi) * (point.y - yi) / (yj - yi) + xi);
      if (intersect) inside = !inside;
    }
    return inside;
  }
}
