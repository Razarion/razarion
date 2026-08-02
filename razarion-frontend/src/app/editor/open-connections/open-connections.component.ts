import {Component, OnDestroy, OnInit} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {MessageService} from 'primeng/api';
import {Button} from 'primeng/button';
import {TableModule} from 'primeng/table';
import {FormsModule} from '@angular/forms';
import {ToggleSwitch} from 'primeng/toggleswitch';
import {EditorPanel} from '../editor-model';
import {TypescriptGenerator} from 'src/app/backend/typescript-generator';
import {ConnectionMgmtControllerClient, OpenConnectionInfo} from '../../generated/razarion-share';
import {BabylonRenderServiceAccessImpl} from '../../game/renderer/babylon-render-service-access-impl.service';
import {GwtAngularService} from 'src/app/gwtangular/GwtAngularService';
import {GwtInstance} from 'src/app/gwtangular/GwtInstance';

/**
 * One table row. Everything the table sorts on is a plain number or string here, taken once per
 * load - the same snapshot approach the base table uses, and for the same reason: PrimeNG sorts on
 * field paths, and a getter chain or a formatted string is not something it can order sensibly.
 */
interface ConnectionRow {
  userId: string;
  /** The player's name; empty for the vast majority who never set one. */
  name: string;
  system: boolean;
  game: boolean;
  /** Null when this player has no base - normal while starting up, telling after a while. */
  baseId: number | null;
  baseText: string;
  baseTitle: string;
  /** Items the base owns, buildings included; -1 sorts the baseless rows together. */
  itemCount: number;
  /** Sortable level; -1 for bases without one, which is every bot and every fresh player. */
  level: number;
  levelText: string;
  /** Somewhere to send the camera, null when the base has no items left to look at. */
  baseX: number | null;
  baseY: number | null;
  /** Epoch millis of the first of the two sockets to open; 0 if neither reported a time. */
  openedMillis: number;
  openedText: string;
  openedTitle: string;
  /** Seconds since the last message in either direction on either socket; -1 if there was none. */
  idleSeconds: number;
  idleText: string;
  idleTitle: string;
}

@Component({
  selector: 'app-open-connections',
  imports: [
    Button,
    TableModule,
    FormsModule,
    ToggleSwitch
  ],
  templateUrl: './open-connections.component.html'
})
export class OpenConnectionsComponent extends EditorPanel implements OnInit, OnDestroy {
  rows: ConnectionRow[] = [];
  systemOnlyCount = 0;
  gameOnlyCount = 0;
  loading = false;
  /** Off by default: a table that moves under the cursor is hard to read. */
  autoRefresh = false;
  private readonly connectionMgmtControllerClient: ConnectionMgmtControllerClient;
  private refreshTimer: ReturnType<typeof setInterval> | null = null;
  // Per base: last unit id the camera jumped to, so a repeated click advances to the next unit.
  private lastMovedToUnitId = new Map<number, number>();

  constructor(private messageService: MessageService,
              private gwtAngularService: GwtAngularService,
              private renderService: BabylonRenderServiceAccessImpl,
              httpClient: HttpClient) {
    super();
    this.connectionMgmtControllerClient =
      new ConnectionMgmtControllerClient(TypescriptGenerator.generateHttpClientAdapter(httpClient));
  }

  ngOnInit(): void {
    this.load();
  }

  ngOnDestroy(): void {
    this.stopAutoRefresh();
  }

  /**
   * True only while the game engine is running, same test the user table makes. Opened through the
   * backend URL the WASM client never boots, so there is no camera to move.
   */
  gameAvailable(): boolean {
    const facade = this.gwtAngularService.gwtAngularFacade;
    return !!facade && !!facade.baseItemUiService && !!facade.gameUiControl;
  }

  /** Every row keeps its button; a row that cannot be looked at shows it greyed out. */
  canScrollTo(row: ConnectionRow): boolean {
    return row.baseX !== null && row.baseY !== null && this.gameAvailable();
  }

  scrollToTitle(row: ConnectionRow): string {
    if (row.baseId === null) {
      return 'This player owns no base to move to';
    }
    if (row.baseX === null || row.baseY === null) {
      return 'This base has no items left on the planet';
    }
    if (!this.gameAvailable()) {
      return 'No running game - open the editor from inside the game to move the camera';
    }
    return 'Move the camera to this base - click again for the next unit';
  }

  /**
   * Puts the camera on one of the base's units; repeated clicks cycle through them, the same way the
   * Users tab does it. The panel stays open on purpose - the point is walking down the list and
   * looking at one base after another.
   */
  scrollTo(row: ConnectionRow): void {
    if (!this.canScrollTo(row)) {
      return;
    }
    const baseId = row.baseId!;
    const facade = this.gwtAngularService.gwtAngularFacade;
    // Scan the whole planet: the worker simulates all bases, so the full item buffer is available
    // regardless of what is currently on screen.
    const size = facade.gameUiControl.getPlanetConfig().getSize();
    const bottomLeft = GwtInstance.newDecimalPosition(0, 0);
    const topRight = GwtInstance.newDecimalPosition(size.getX(), size.getY());
    const units = facade.baseItemUiService.getVisibleNativeSyncBaseItemTickInfos(bottomLeft, topRight)
      .filter(info => info.baseId === baseId)
      // Stable order so cycling is deterministic across clicks even as units move.
      .sort((a, b) => a.id - b.id);
    if (units.length === 0) {
      this.messageService.add({
        severity: 'warn',
        summary: 'No units found',
        detail: `Base ${baseId} has no units on the planet.`
      });
      return;
    }
    const lastId = this.lastMovedToUnitId.get(baseId);
    let nextIdx = 0;
    if (lastId !== undefined) {
      const lastIdx = units.findIndex(u => u.id === lastId);
      nextIdx = lastIdx >= 0 ? (lastIdx + 1) % units.length : 0;
    }
    const next = units[nextIdx];
    this.lastMovedToUnitId.set(baseId, next.id);
    this.renderService.setViewFieldCenter(next.x, next.y);
  }

  onAutoRefreshChange(): void {
    if (this.autoRefresh) {
      this.refreshTimer = setInterval(() => this.load(), 5000);
    } else {
      this.stopAutoRefresh();
    }
  }

  load(): void {
    this.loading = true;
    this.connectionMgmtControllerClient.openConnections()
      .then(infos => {
        const now = Date.now();
        this.rows = infos.map(info => OpenConnectionsComponent.toRow(info, now));
        this.systemOnlyCount = this.rows.filter(row => row.system && !row.game).length;
        this.gameOnlyCount = this.rows.filter(row => row.game && !row.system).length;
        this.loading = false;
      })
      .catch((reason: any) => {
        this.loading = false;
        this.messageService.add({
          severity: 'error',
          summary: 'Failed to load open connections',
          detail: reason,
          sticky: true
        });
      });
  }

  private stopAutoRefresh(): void {
    if (this.refreshTimer) {
      clearInterval(this.refreshTimer);
      this.refreshTimer = null;
    }
  }

  private static toRow(info: OpenConnectionInfo, now: number): ConnectionRow {
    const opened = OpenConnectionsComponent.earliest([
      info.systemConnectionOpened, info.gameConnectionOpened]);
    const lastMessage = OpenConnectionsComponent.latest([
      info.systemLastMessageSent, info.systemLastMessageReceived,
      info.gameLastMessageSent, info.gameLastMessageReceived]);
    const idleSeconds = lastMessage === null ? -1 : Math.floor((now - lastMessage) / 1000);
    const hasBase = info.baseId !== null && info.baseId !== undefined;
    return {
      userId: info.userId,
      name: info.name ? info.name : '',
      system: info.systemConnectionOpened !== null && info.systemConnectionOpened !== undefined,
      game: info.gameConnectionOpened !== null && info.gameConnectionOpened !== undefined,
      baseId: hasBase ? info.baseId : null,
      baseText: hasBase ? `${info.baseName} (${info.itemCount})` : 'none',
      baseTitle: hasBase
        ? `Base ${info.baseId} - ${info.itemCount} units and buildings`
        : 'This player owns no base',
      itemCount: hasBase ? info.itemCount : -1,
      level: info.levelNumber !== null && info.levelNumber !== undefined ? info.levelNumber : -1,
      levelText: info.levelNumber !== null && info.levelNumber !== undefined ? String(info.levelNumber) : '—',
      baseX: info.baseX !== null && info.baseX !== undefined ? info.baseX : null,
      baseY: info.baseY !== null && info.baseY !== undefined ? info.baseY : null,
      openedMillis: opened === null ? 0 : opened,
      openedText: opened === null ? '—' : OpenConnectionsComponent.duration(now - opened),
      openedTitle: opened === null ? '' : new Date(opened).toLocaleString(),
      idleSeconds,
      idleText: idleSeconds < 0 ? 'never' : OpenConnectionsComponent.duration(idleSeconds * 1000),
      idleTitle: lastMessage === null
        ? 'No message has crossed either socket since it was opened'
        : new Date(lastMessage).toLocaleString(),
    };
  }

  /**
   * The generated client types these as Date, but they arrive from JSON as epoch numbers - the
   * server serialises java.util.Date that way. Reading them through Number() copes with both.
   */
  private static millis(value: any): number | null {
    if (value === null || value === undefined) {
      return null;
    }
    const millis = value instanceof Date ? value.getTime() : Number(new Date(value as any));
    return isNaN(millis) ? null : millis;
  }

  private static earliest(values: any[]): number | null {
    return values
      .map(value => OpenConnectionsComponent.millis(value))
      .filter((millis): millis is number => millis !== null)
      .reduce((a: number | null, b: number) => a === null || b < a ? b : a, null);
  }

  private static latest(values: any[]): number | null {
    return values
      .map(value => OpenConnectionsComponent.millis(value))
      .filter((millis): millis is number => millis !== null)
      .reduce((a: number | null, b: number) => a === null || b > a ? b : a, null);
  }

  /** Readable at a glance; the exact timestamp lives in the column's tooltip. */
  private static duration(millis: number): string {
    const seconds = Math.floor(millis / 1000);
    if (seconds < 60) {
      return `${seconds} s`;
    }
    const minutes = Math.floor(seconds / 60);
    if (minutes < 60) {
      return `${minutes} min`;
    }
    const hours = Math.floor(minutes / 60);
    if (hours < 24) {
      return `${hours} h ${minutes % 60} min`;
    }
    return `${Math.floor(hours / 24)} d ${hours % 24} h`;
  }
}
