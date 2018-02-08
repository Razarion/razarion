import {Component, OnInit} from "@angular/core";
import {ActivatedRoute, Params} from "@angular/router";
import {UserService} from "./user.service";
import {UnlockedBackendInfo, UserBackendInfo} from "./user.dto";
import {QuestBackendInfo} from "../common";
import "rxjs/add/operator/switchMap";
import "rxjs/add/operator/toPromise";

@Component({
  selector: 'user-component',
  templateUrl: './user.component.html',
  styleUrls: ['./user.component.css']
})

export class UserComponent implements OnInit {
  userBackendInfo: UserBackendInfo;

  constructor(private userService: UserService, private route: ActivatedRoute) {
  }

  ngOnInit(): void {
    this.route.params.switchMap((params: Params) => this.userService.loadUserBackendInfo(params['id'])).subscribe(userBackendInfo => this.displayUser(userBackendInfo));
  }

  onRemoveCompletedQuest(completedQuest: QuestBackendInfo) {
    this.userService.removeCompletedQuest(this.userBackendInfo.humanPlayerId.playerId, completedQuest.id).then(userBackendInfo => this.displayUser(userBackendInfo));
  }

  onUpdate() {
    this.userService.loadUserBackendInfo(this.userBackendInfo.humanPlayerId.playerId).then(userBackendInfo => this.displayUser(userBackendInfo));
  }

  onSetNewLevel(levelNumber: number) {
    this.userService.setLevelNumber(this.userBackendInfo.humanPlayerId.playerId, levelNumber).then(userBackendInfo => this.displayUser(userBackendInfo));
  }

  onSetNewXp(xp: number) {
    this.userService.setXp(this.userBackendInfo.humanPlayerId.playerId, xp).then(userBackendInfo => this.displayUser(userBackendInfo));
  }

  onSetNewCrystals(crystals: number) {
    this.userService.setCrystals(this.userBackendInfo.humanPlayerId.playerId, crystals).then(userBackendInfo => this.displayUser(userBackendInfo));
  }

  onRemoveUnlocked(unlockedBackendInfo: UnlockedBackendInfo) {
    this.userService.removeUnlocked(this.userBackendInfo.humanPlayerId.playerId, unlockedBackendInfo).then(userBackendInfo => this.displayUser(userBackendInfo));
  }

  private displayUser(userBackendInfo: UserBackendInfo): void {
    this.userBackendInfo = userBackendInfo;
    if (userBackendInfo.name != null) {
      window.document.title = userBackendInfo.name;
    } else if (userBackendInfo.humanPlayerId.userId != null) {
      window.document.title = "UserId: " + userBackendInfo.humanPlayerId.userId;
    } else {
      window.document.title = "PlayerId: " + userBackendInfo.humanPlayerId.playerId;
    }
  }
}
