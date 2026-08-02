import {Component, OnInit} from '@angular/core';
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
import {CheckboxModule} from 'primeng/checkbox';
import {RippleModule} from 'primeng/ripple';
import {DialogModule} from 'primeng/dialog';
import {GwtAngularService} from 'src/app/gwtangular/GwtAngularService';
import {GwtInstance} from 'src/app/gwtangular/GwtInstance';
import {BabylonRenderServiceAccessImpl} from '../../game/renderer/babylon-render-service-access-impl.service';

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
    CheckboxModule,
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
export class UserMgmtComponent extends EditorPanel implements OnInit {
  userBackendInfos: UserBackendInfo[] = [];
  questOptions: { label: string, questId: number }[] = [];
  private userMgmtControllerClient: UserMgmtControllerClient;
  selectedUserIds = new Set<string>();
  minutes: number = 60;
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

  private loadUsers(): void {
    this.userMgmtControllerClient.getUserBackendInfos().then(userBackendInfos => {
      this.selectedUserIds.clear();
      this.userBackendInfos.length = 0;
      this.userBackendInfos.push(...userBackendInfos);
    }).catch(err =>
      this.messageService.add({
        severity: 'error',
        summary: `Can not load user infos`,
        detail: err.message,
        sticky: true
      }));
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

  onSelectionChanged(checked: boolean, userBackendInfo: UserBackendInfo) {
    if (checked) {
      this.selectedUserIds.add(userBackendInfo.userId);
    } else {
      this.selectedUserIds.delete(userBackendInfo.userId);
    }
  }

  deleteSelectedUsers() {
    this.userMgmtControllerClient.deleteUsersAndBases(Array.from(this.selectedUserIds))
      .then(() => this.loadUsers());
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

  selectUnregistered() {
    const now = new Date();
    this.userBackendInfos.forEach((userBackendInfo) => {
      // Only ever auto-select anonymous (unregistered) users.
      if (userBackendInfo.email) {
        return;
      }
      // Never touch a user that currently holds a live connection. The server persists
      // systemConnectionOpened the moment a system connection opens (UserService.onClientSystemConnectionOpened),
      // so anyone mid-connect already has that timestamp and is no longer a "grey" row — but we still
      // guard on the live flags to be safe against a user who is about to build a connection.
      if (userBackendInfo.systemConnectionOpen || userBackendInfo.gameConnectionOpen) {
        return;
      }
      if (userBackendInfo.systemConnectionClosed) {
        // Red row: was online and disconnected. Select once offline longer than the threshold.
        if (UserMgmtComponent.isOlderThanMinutes(userBackendInfo.systemConnectionClosed, now, this.minutes)) {
          this.selectedUserIds.add(userBackendInfo.userId);
        }
      } else if (!userBackendInfo.systemConnectionOpened && userBackendInfo.creationDate) {
        // Grey row: never established a system connection at all. Guard by creation age so a freshly
        // created account that might still be opening its connection is left alone.
        if (UserMgmtComponent.isOlderThanMinutes(userBackendInfo.creationDate, now, this.minutes)) {
          this.selectedUserIds.add(userBackendInfo.userId);
        }
      }
    })
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

  public static isOlderThanMinutes(date: Date, now: Date, minutes: number): boolean {
    date = new Date(date)
    const diffInMs = now.getTime() - date.getTime();
    const diffInMinutes = diffInMs / (1000 * 60);

    return diffInMinutes > minutes;
  }

}
