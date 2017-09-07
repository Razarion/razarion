import {Component, OnInit} from "@angular/core";
import {ActivatedRoute, Params} from "@angular/router";
import {UserService} from "./user.service";
import {UserBackendInfo} from "./user.dto";
import {QuestBackendInfo} from "../Common";
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
    this.route.params.switchMap((params: Params) => this.userService.loadUserBackendInfo(params['id'])).subscribe(userBackendInfo => this.userBackendInfo = userBackendInfo);
  }

  onRemoveCompletedQuest(completedQuest: QuestBackendInfo) {
    this.userService.removeCompletedQuest(this.userBackendInfo.humanPlayerId.playerId, completedQuest.id).then(userBackendInfo => this.userBackendInfo = userBackendInfo);
  }

  onUpdate() {
    this.userService.loadUserBackendInfo(this.userBackendInfo.humanPlayerId.playerId).then(userBackendInfo => this.userBackendInfo = userBackendInfo);
  }

  onSetNewLevel(levelNumber: number) {
    this.userService.setLevelNumber(this.userBackendInfo.humanPlayerId.playerId, levelNumber).then(userBackendInfo => this.userBackendInfo = userBackendInfo);
  }

  onSetNewXp(xp: number) {
    this.userService.setXp(this.userBackendInfo.humanPlayerId.playerId, xp).then(userBackendInfo => this.userBackendInfo = userBackendInfo);
  }
}
