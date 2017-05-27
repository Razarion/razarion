import {Component, OnInit} from "@angular/core";
import {SessionService} from "./session.service";
import {Session} from "./session";

@Component({
  selector: 'session-table',
  templateUrl: './session-table.component.html',
  styleUrls: ['./session-table.component.css']
})

export class SessionTable implements OnInit {
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
