import {Component, OnDestroy, OnInit} from '@angular/core';
import {EditorPanel} from '../editor-model';
import {QuestBackendInfo, UserBackendInfo, UserMgmtControllerClient} from 'src/app/generated/razarion-share';
import {TypescriptGenerator} from 'src/app/backend/typescript-generator';
import {MessageService} from 'primeng/api';
import {HttpClient} from '@angular/common/http';
import {QuestCockpitComponent} from "../../game/cockpit/quest/quest-cockpit.component";
import {ButtonModule} from 'primeng/button';
import {InputNumberModule} from 'primeng/inputnumber';
import {LevelComponent} from '../common/level/level.component';
import {TableModule} from 'primeng/table';
import { CommonModule, DatePipe } from '@angular/common';
import {ChipModule} from 'primeng/chip';
import {FormsModule} from '@angular/forms';
import {Select} from 'primeng/select';
import {ToolbarModule} from 'primeng/toolbar';
import {InputText} from 'primeng/inputtext';
import {RippleModule} from 'primeng/ripple';
import {DialogModule} from 'primeng/dialog';
import {GwtAngularService} from 'src/app/gwtangular/GwtAngularService';
import {GwtInstance} from 'src/app/gwtangular/GwtInstance';
import {BabylonRenderServiceAccessImpl} from '../../game/renderer/babylon-render-service-access-impl.service';

/**
 * A user as the table shows it: under a list heading, and keyed by list as well, since a new player
 * who just registered is in both lists. The row is the DTO itself, so edits land on it.
 */
type UserRow = UserBackendInfo & { listName: string; rowKey: string; heading?: string };

/** Typing pauses this long before the search goes out, so a name is one request and not six. */
const SEARCH_DELAY_MS = 300;

/**
 * The user table no longer holds every user: in November 2026 that was some 3000 rows, growing by
 * 300 a day of an ad campaign, and the backend page froze on it. Without a search it shows the ten
 * newest players and the ten newest registrations; the search goes to the server
 * (UserService.searchUserBackendInfos) and also finds the call sign the chat shows. Who is online
 * is the Connections tab's business. Cleaning up anonymous users went with the full list - the
 * server does that on its own (UserRepository, the unregistered cleanup).
 */
@Component({
  selector: 'user-mgmt',
  imports: [
    ButtonModule,
    InputNumberModule,
    LevelComponent,
    TableModule,
    DatePipe,
    Select,
    ChipModule,
    FormsModule,
    CommonModule,
    ToolbarModule,
    InputText,
    RippleModule,
    DialogModule
],
  templateUrl: './user-mgmt.component.html',
  styles: [`
    /* Zebra-stripe the key/value rows of the expanded detail block for readability. */
    .um-detail > div { padding: 0.2rem 0.5rem; border-radius: 4px; }
    .um-detail > div:nth-child(even) { background: rgba(255, 255, 255, 0.05); }
  `]
})
export class UserMgmtComponent extends EditorPanel implements OnInit, OnDestroy {
  rows: UserRow[] = [];
  /** Users in the table, headings not counted. */
  userCount = 0;
  searchText = '';
  loading = false;
  readonly searchLimit = 50;
  questOptions: { label: string, questId: number }[] = [];
  private userMgmtControllerClient: UserMgmtControllerClient;
  private searchTimer: ReturnType<typeof setTimeout> | null = null;
  /** Only the newest request may fill the table; an older answer arriving late is dropped. */
  private loadSequence = 0;
  showDeleteDialog = false;
  userToDelete: UserBackendInfo | null = null;
  // Per base: last unit id the camera jumped to, so a repeated "Move to" advances to the next unit.
  private lastMovedToUnitId = new Map<number, number>();
  // Rows whose game history is in flight, so a second expand does not fire a second query.
  private loadingHistoryUserIds = new Set<string>();


  constructor(private messageService: MessageService,
              private gwtAngularService: GwtAngularService,
              private renderService: BabylonRenderServiceAccessImpl,
              httpClient: HttpClient) {
    super();
    this.userMgmtControllerClient = new UserMgmtControllerClient(TypescriptGenerator.generateHttpClientAdapter(httpClient))
  }

  ngOnInit(): void {
    this.loadAllQuestOptions();
  }


  private loadAllQuestOptions() {
    this.userMgmtControllerClient.getQuestBackendInfos().then(questBackendInfos => {
      this.questOptions.length = 0;
      questBackendInfos.forEach(questBackendInfo => {
        this.questOptions.push({label: this.questBackendInfo2Option(questBackendInfo), questId: questBackendInfo.id})
      });
      this.loadUsers();
    }).catch(err =>
      this.messageService.add({
        severity: 'error',
        summary: `Can not load quest backend infos`,
        detail: err.message,
        sticky: true
      }));
  }

  private questBackendInfo2Option(questBackendInfo: QuestBackendInfo): string {
    return ` lvl: ${questBackendInfo.levelNumber} ${QuestCockpitComponent.conditionTriggerToTitle(questBackendInfo.conditionConfig.conditionTrigger)} '${questBackendInfo.id}'`
  }

  ngOnDestroy(): void {
    if (this.searchTimer !== null) {
      clearTimeout(this.searchTimer);
    }
  }

  /** The server searches from two characters on; below that the default lists show. */
  searching(): boolean {
    return this.searchText.trim().length >= 2;
  }

  onSearchChanged(): void {
    if (this.searchTimer !== null) {
      clearTimeout(this.searchTimer);
    }
    this.searchTimer = setTimeout(() => {
      this.searchTimer = null;
      this.loadUsers();
    }, SEARCH_DELAY_MS);
  }

  private loadUsers(): void {
    const sequence = ++this.loadSequence;
    this.loading = true;
    const query = this.searchText.trim();
    const lists: Promise<UserRow[]> = this.searching()
      ? this.userMgmtControllerClient.searchUserBackendInfos({query})
        .then(users => UserMgmtComponent.toRows(users, `Search "${query}"`))
      : Promise.all([
        this.userMgmtControllerClient.getNewestUserBackendInfos(),
        this.userMgmtControllerClient.getNewestRegisteredUserBackendInfos()
      ]).then(([newest, registered]) => [
        ...UserMgmtComponent.toRows(newest, 'Newest players'),
        ...UserMgmtComponent.toRows(registered, 'Newest registrations')
      ]);
    lists.then(rows => {
      if (sequence === this.loadSequence) {
        this.rows = rows;
        this.userCount = rows.filter(row => !row.heading).length;
      }
    }).catch(err =>
      this.messageService.add({
        severity: 'error',
        summary: `Can not load user infos`,
        detail: err.message,
        sticky: true
      })).finally(() => {
      if (sequence === this.loadSequence) {
        this.loading = false;
      }
    });
  }

  /** An empty list has no heading either, so a search that finds nobody shows the table's empty message. */
  static toRows(users: UserBackendInfo[], listName: string): UserRow[] {
    if (users.length === 0) {
      return [];
    }
    return [
      // The heading over a list, a row of its own; it carries no user.
      {heading: listName, rowKey: listName} as UserRow,
      ...users.map(user => Object.assign(user, {listName, rowKey: `${listName}|${user.userId}`}))
    ];
  }

  /**
   * The history is one Mongo query per user, so the list arrives without it and a row fetches its
   * own when it is opened. Kept once fetched: reopening the same row should not query again.
   */
  onRowExpand(userBackendInfo: UserBackendInfo): void {
    if (userBackendInfo.gameHistoryEntries || this.loadingHistoryUserIds.has(userBackendInfo.userId)) {
      return;
    }
    this.loadingHistoryUserIds.add(userBackendInfo.userId);
    this.userMgmtControllerClient.getGameHistory(userBackendInfo.userId)
      .then(gameHistoryEntries => {
        userBackendInfo.gameHistoryEntries = gameHistoryEntries;
        this.loadingHistoryUserIds.delete(userBackendInfo.userId);
      })
      .catch(err => {
        this.loadingHistoryUserIds.delete(userBackendInfo.userId);
        this.messageService.add({
          severity: 'error',
          summary: `Can not load game history`,
          detail: err.message,
          sticky: true
        });
      });
  }

  historyLoading(userBackendInfo: UserBackendInfo): boolean {
    return this.loadingHistoryUserIds.has(userBackendInfo.userId);
  }

  onSaveLevel(userBackendInfo: UserBackendInfo): void {
    this.userMgmtControllerClient.setLevel(userBackendInfo.userId, userBackendInfo.levelId).catch(err =>
      this.messageService.add({
        severity: 'error',
        summary: `Can not save level`,
        detail: err.message,
        sticky: true
      }));
  }

  onSetLevelResetQuests(userBackendInfo: UserBackendInfo): void {
    this.userMgmtControllerClient.setLevelResetQuests(userBackendInfo.userId, userBackendInfo.levelId)
      .then(() => {
        this.messageService.add({
          severity: 'success',
          summary: `Level set & quests reset`,
          detail: `All quests before the level are passed, the first quest of the level is active.`
        });
        this.loadUsers();
      })
      .catch(err =>
        this.messageService.add({
          severity: 'error',
          summary: `Can not set level & reset quests`,
          detail: err.message,
          sticky: true
        }));
  }

  onSaveCrystals(userBackendInfo: UserBackendInfo): void {
    this.userMgmtControllerClient.setCrystals(userBackendInfo.userId, userBackendInfo.crystals).catch(err =>
      this.messageService.add({
        severity: 'error',
        summary: `Can not save crystals`,
        detail: err.message,
        sticky: true
      }));
  }

  onSaveCompletedQuestIds(userBackendInfo: UserBackendInfo): void {
    this.userMgmtControllerClient.setCompletedQuests(userBackendInfo.userId, userBackendInfo.completedQuestIds).catch(err =>
      this.messageService.add({
        severity: 'error',
        summary: `Can not save completed quests`,
        detail: err.message,
        sticky: true
      }));
  }

  onSaveUnlockedIds(userBackendInfo: UserBackendInfo): void {
    this.userMgmtControllerClient.setUnlocked(userBackendInfo.userId, userBackendInfo.unlockedIds).catch(err =>
      this.messageService.add({
        severity: 'error',
        summary: `Can not save completed quests`,
        detail: err.message,
        sticky: true
      }));
  }

  onSaveActiveQuest(userBackendInfo: UserBackendInfo) {
    if (userBackendInfo.activeQuest || userBackendInfo.activeQuest === 0) {
      this.userMgmtControllerClient.activateQuest(userBackendInfo.userId, userBackendInfo.activeQuest).catch(err =>
        this.messageService.add({
          severity: 'error',
          summary: `Can not active quest`,
          detail: err.message,
          sticky: true
        }));
    } else {
      this.userMgmtControllerClient.deactivateQuest(userBackendInfo.userId).catch(err =>
        this.messageService.add({
          severity: 'error',
          summary: `Can not deactive quest`,
          detail: err.message,
          sticky: true
        }));
    }
  }

  removeUnlockedId(user: UserBackendInfo, unlockedId: number) {
    user.unlockedIds = user.unlockedIds.filter(id => id !== unlockedId);
  }

  removeQuestId(user: UserBackendInfo, questId: number) {
    user.completedQuestIds = user.completedQuestIds.filter(id => id !== questId);
  }

  refresh() {
    this.loadUsers();
  }

  askDeleteUser(userBackendInfo: UserBackendInfo) {
    this.userToDelete = userBackendInfo;
    this.showDeleteDialog = true;
  }

  cancelDeleteUser() {
    this.showDeleteDialog = false;
    this.userToDelete = null;
  }

  confirmDeleteUser() {
    if (!this.userToDelete) {
      return;
    }
    const userId = this.userToDelete.userId;
    this.userMgmtControllerClient.deleteUser(userId)
      .then(() => {
        this.messageService.add({
          severity: 'success',
          summary: `User deleted`,
          detail: `User '${userId}' and its base were deleted.`
        });
        this.cancelDeleteUser();
        this.loadUsers();
      })
      .catch(err => {
        this.messageService.add({
          severity: 'error',
          summary: `Can not delete user`,
          detail: err.message,
          sticky: true
        });
        this.cancelDeleteUser();
      });
  }

  // True only while the game engine is running (in-game editor). Opened via the backend URL the WASM
  // client never boots, so there is no camera to move — the "Move to" button stays disabled there.
  gameAvailable(): boolean {
    const facade = this.gwtAngularService.gwtAngularFacade;
    return !!facade && !!facade.baseItemUiService && !!facade.gameUiControl;
  }

  // Scrolls the camera to a unit of the user's base. Repeated clicks cycle through that base's units.
  moveToBase(userBackendInfo: UserBackendInfo): void {
    const baseId = userBackendInfo.baseId;
    if (baseId === null || baseId === undefined || !this.gameAvailable()) {
      return;
    }
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
        summary: `No units found`,
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
}
