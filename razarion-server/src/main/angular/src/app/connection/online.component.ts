import {Component, Input, OnInit} from "@angular/core";
import {OnlineInfo} from "./online.dto";
import {OnlineService} from "./online.service";
import {Router} from "@angular/router";

@Component({
  selector: 'online-component',
  templateUrl: './online.component.html',
  styleUrls: ['./online.component.css']
})

export class OnlineComponent implements OnInit {
  onlineInfos: OnlineInfo[];
  @Input() lastLoaded: Date;

  constructor(private onlineService: OnlineService, private route: Router) {
  }

  ngOnInit(): void {
    this.onlineService.loadAllOnlines().then(onlineInfos => this.setOnlineInfos(onlineInfos));
    window.setInterval(() => {
      this.onlineService.loadAllOnlines().then(onlineInfos => this.setOnlineInfos(onlineInfos));
    }, 60000);
  }

  onUpdate() {
    this.onlineService.loadAllOnlines().then(onlineInfos => this.setOnlineInfos(onlineInfos));
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
    this.route.navigate(['/humanplayerid', onlineInfo.humanPlayerId]);
  }
}
