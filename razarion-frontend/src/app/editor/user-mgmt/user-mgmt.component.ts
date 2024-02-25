import { Component, OnInit } from '@angular/core';
import { EditorPanel } from '../editor-model';
import { UserBackendInfo, UserMgmtControllerClient } from 'src/app/generated/razarion-share';
import { TypescriptGenerator } from 'src/app/backend/typescript-generator';
import { MessageService } from 'primeng/api';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'user-mgmt',
  templateUrl: './user-mgmt.component.html'
})
export class UserMgmtComponent extends EditorPanel implements OnInit {
  userBackendInfos: UserBackendInfo[] = [];
  private userMgmtControllerClient: UserMgmtControllerClient;

  constructor(private messageService: MessageService,
    httpClient: HttpClient) {
    super();
    this.userMgmtControllerClient = new UserMgmtControllerClient(TypescriptGenerator.generateHttpClientAdapter(httpClient))
  }

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {
    this.userMgmtControllerClient.getUserBackendInfos().then(userBackendInfos => this.userBackendInfos = userBackendInfos).catch(err =>
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
}