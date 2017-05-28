import {Component, OnInit} from "@angular/core";
import {SessionService} from "./session.service";
import {Session} from "./session";
import {Router} from "@angular/router";

@Component({
  selector: 'session-history',
  templateUrl: './session-history.component.html',
  styleUrls: ['./session-history.component.css']
})

export class SessionHistory implements OnInit {
  sessions: Session[];

  constructor(private sessionService: SessionService, private route: Router) {
  }

  ngOnInit(): void {
    this.sessionService.getSessions().then(sessions => {
      this.sessions = sessions;
    });
  }

  onClick(session: Session): void {
    this.route.navigate(['/session', session.id]);
  }

}
