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
import {CommonModule, DatePipe, NgForOf} from '@angular/common';
import {ChipModule} from 'primeng/chip';
import {FormsModule} from '@angular/forms';
import {SelectModule} from 'primeng/select';
import {DropdownModule} from 'primeng/dropdown';
import {ToolbarModule} from 'primeng/toolbar';
import {CheckboxModule} from 'primeng/checkbox';

@Component({
  selector: 'user-mgmt',
  imports: [
    ButtonModule,
    InputNumberModule,
    LevelComponent,
    TableModule,
    DatePipe,
    SelectModule,
    ChipModule,
    FormsModule,
    DropdownModule,
    NgForOf,
    CommonModule,
    ToolbarModule,
    CheckboxModule,
  ],
  templateUrl: './user-mgmt.component.html'
})
export class UserMgmtComponent extends EditorPanel implements OnInit {
  userBackendInfos: UserBackendInfo[] = [];
  questOptions: { label: string, questId: number }[] = [];
  private userMgmtControllerClient: UserMgmtControllerClient;
  selectedUserIds = new Set<string>();
  minutes: number = 60;


  constructor(private messageService: MessageService,
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

  onSaveLevel(userBackendInfo: UserBackendInfo): void {
    this.userMgmtControllerClient.setLevel(userBackendInfo.userId, userBackendInfo.levelId).catch(err =>
      this.messageService.add({
        severity: 'error',
        summary: `Can not save level`,
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

  selectUnregistered() {
    const now = new Date();
    this.userBackendInfos.forEach((userBackendInfo) => {
      if (!userBackendInfo.email) {
        if (userBackendInfo.systemConnectionClosed
          && UserMgmtComponent.isOlderThanMinutes(userBackendInfo.systemConnectionClosed, now, this.minutes)) {
          this.selectedUserIds.add(userBackendInfo.userId);
        }
      }
    })
  }

  public static isOlderThanMinutes(date: Date, now: Date, minutes: number): boolean {
    date = new Date(date)
    const diffInMs = now.getTime() - date.getTime();
    const diffInMinutes = diffInMs / (1000 * 60);

    return diffInMinutes > minutes;
  }

}
