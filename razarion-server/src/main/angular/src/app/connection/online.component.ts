import {Component, Input, OnDestroy, OnInit} from "@angular/core";
import {OnlineInfo, OnlineInfoType} from "./online.dto";
import {OnlineService} from "./online.service";
import {Router} from "@angular/router";

@Component({
  selector: 'online-component',
  templateUrl: './online.component.html',
  styleUrls: ['./online.component.css']
})

export class OnlineComponent implements OnInit, OnDestroy {
  onlineInfos: OnlineInfo[];
  @Input() lastLoaded: Date;
  lastError: String = null;
  onlineType: OnlineInfoType;
  private timerId: number = null;

  constructor(private onlineService: OnlineService, private route: Router) {
  }

  ngOnInit(): void {
    this.loadAllOnlines();
    this.clearTimer();
    this.timerId = window.setInterval(() => {
      this.loadAllOnlines();
    }, 60000);
  }

  ngOnDestroy(): void {
    this.clearTimer();
  }

  onUpdate() {
    this.loadAllOnlines();
  }

  private setOnlineInfos(onlineInfos: OnlineInfo[]): void {
    this.onlineInfos = onlineInfos;
    this.onlineInfos.sort((a, b) => {
      return new Date(b.time).getTime() - new Date(a.time).getTime();
    });
    this.lastLoaded = new Date();
  }

  onClickSession(onlineInfo: OnlineInfo) {
    this.route.navigate(['/session', onlineInfo.sessionId]);
  }

  onClickUser(onlineInfo: OnlineInfo) {
    this.route.navigate(['/user', onlineInfo.humanPlayerId.playerId]);
  }

  private clearTimer(): void {
    if (this.timerId != null) {
      window.clearInterval(this.timerId);
      this.timerId = null;
    }
  }

  private loadAllOnlines(): void {
    this.onlineService.loadAllOnlines().then(onlineInfos => {
      this.setOnlineInfos(onlineInfos);
      this.lastError = null;
    }).catch(reason => {
      this.clearTimer();
      this.lastError = reason.toString();
    });
  }

  getUserState(onlineInfo: OnlineInfo): string {
    if (onlineInfo.name != null) {
      return onlineInfo.name;
    }
    if (onlineInfo.humanPlayerId == null) {
      return "humanPlayerId == null";
    }
    if (onlineInfo.humanPlayerId.userId != null) {
      return "Unnamed (" + onlineInfo.humanPlayerId.userId + ")";
    } else {
      return "Anonymous: " + onlineInfo.humanPlayerId.playerId;
    }
  }
}
