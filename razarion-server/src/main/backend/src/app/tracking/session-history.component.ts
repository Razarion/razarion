import {Component, OnInit} from "@angular/core";
import {SessionService} from "./session.service";
import {Session} from "./session";

@Component({
  selector: 'session-history',
  templateUrl: './session-history.component.html',
  styleUrls: ['./session-history.component.css']
})

export class SessionHistory implements OnInit {
  sessions: Session[];

  constructor(private sessionService: SessionService) {
  }

  ngOnInit(): void {
    this.sessionService.getSessions().then(sessions => {
      this.sessions = sessions;
    });
  }

  onClick(session: Session): void {
    console.error("***** onClick(): " + session);
  }

}
