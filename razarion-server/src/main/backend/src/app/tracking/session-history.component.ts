import {Component, OnInit} from "@angular/core";
import {SessionService} from "./session.service";
import {SearchConfig, Session} from "./session-dto";
import {Router} from "@angular/router";

@Component({
  selector: 'session-history',
  templateUrl: './session-history.component.html',
  styleUrls: ['./session-history.component.css']
})

export class SessionHistory implements OnInit {
  sessions: Session[];
  fromDateString: string;

  constructor(private sessionService: SessionService, private route: Router) {
    let now: Date = new Date();
    now.setDate(new Date().getDate() - 1);
    this.fromDateString = now.toISOString().slice(0, 19);
  }

  private updateSessions(): void {
    let searchConfig: SearchConfig = new SearchConfig();
    searchConfig.fromDate = new Date(this.fromDateString);

    this.sessionService.getSessions(searchConfig).then(sessions => {
      this.sessions = sessions;
    });
  }

  ngOnInit(): void {
    this.updateSessions();
  }

  onClick(session: Session): void {
    this.route.navigate(['/session', session.id]);
  }

  onSearch() {
    this.updateSessions();
  }
}