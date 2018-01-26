import {Component, OnInit} from "@angular/core";
import {Router} from "@angular/router";
import {UserHistoryService} from "./user-login-history.service";
import {UserHistoryEntry} from "./user-login-history.dto";

@Component({
  selector: 'user-login-history',
  templateUrl: './user-login-history.component.html',
  styleUrls: ['./user-login-history.component.css']
})
export class UserLoginHistory implements OnInit {
  userLoginEntries: UserHistoryEntry[];

  constructor(private userHistoryService: UserHistoryService, private route: Router) {
  }

  ngOnInit(): void {
    this.load();
  }

  load() {
    this.userHistoryService.getUserHistory().then(newUsers => this.userLoginEntries = newUsers);
  }

  onReload() {
    this.load()
  }

  onClickUser(playerId: number) {
    this.route.navigate(['/user', playerId]);
  }

  onClickSession(sessionId: string) {
    this.route.navigate(['/session', sessionId]);
  }
}
