import {Component, OnInit} from "@angular/core";
import {NewUser} from "./new-user.dto";
import {NewUserService} from "./new-user.service";
import {Router} from "@angular/router";

@Component({
  selector: 'new-user-history',
  templateUrl: './new-user-history.component.html',
  styleUrls: ['./new-user-history.component.css']
})
export class NewUserHistory implements OnInit {
  newUsers: NewUser[];

  constructor(private newUserService: NewUserService, private route: Router) {
  }

  ngOnInit(): void {
    this.load();
  }

  load() {
    this.newUserService.getNewUsers().then(newUsers => this.newUsers = newUsers);
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
