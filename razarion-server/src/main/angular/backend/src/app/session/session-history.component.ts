import {Component, OnInit} from "@angular/core";
import {SessionService} from "./session.service";
import {SearchConfig, SessionTracker} from "./session-dto";
import {Router} from "@angular/router";

@Component({
  selector: 'session-history',
  templateUrl: './session-history.component.html',
  styleUrls: ['./session-history.component.css']
})

export class SessionHistory implements OnInit {
  sessionTrackers: SessionTracker[];
  fromDateString: string;
  botFilter: boolean;

  constructor(private sessionService: SessionService, private route: Router) {
    let now: Date = new Date();
    now.setDate(new Date().getDate() - 1);
    this.fromDateString = now.toISOString().slice(0, 19);
    this.botFilter = true;
  }

  private updateSessions(): void {
    let searchConfig: SearchConfig = new SearchConfig();
    searchConfig.fromDate = new Date(this.fromDateString);
    searchConfig.botFilter = this.botFilter;

    this.sessionService.getSessions(searchConfig).then(sessions => {
      this.sessionTrackers = sessions;
    });
  }

  ngOnInit(): void {
    this.updateSessions();
  }

  getHumanPlayerId(sessionTracker: SessionTracker): number {
    if (sessionTracker.createdHumanPlayerId != null && sessionTracker.userFromHistory == null) {
      // Anonymous
      return sessionTracker.createdHumanPlayerId;
    } else if (sessionTracker.createdHumanPlayerId == null && sessionTracker.userFromHistory != null) {
      // Logged in
      return sessionTracker.userFromHistory.humanPlayerId.playerId;
    } else if (sessionTracker.createdHumanPlayerId != null && sessionTracker.userFromHistory != null) {
      if (sessionTracker.createdHumanPlayerId == sessionTracker.userFromHistory.humanPlayerId.playerId) {
        // New user
        return sessionTracker.createdHumanPlayerId;
      } else {
        // Invalid
        return null;
      }
    }
  }

  onSearch() {
    this.updateSessions();
  }

  analyseUserState(sessionTracker: SessionTracker): string {
    if (sessionTracker.createdHumanPlayerId != null && sessionTracker.userFromHistory == null) {
      return "Anonymous: " + sessionTracker.createdHumanPlayerId;
    } else if (sessionTracker.createdHumanPlayerId == null && sessionTracker.userFromHistory != null) {
      let description = "Logged in: ";
      if (sessionTracker.userFromHistory.name != null) {
        description += sessionTracker.userFromHistory.name
      } else {
        description += "(" + sessionTracker.userFromHistory.humanPlayerId.userId + ")";
      }
      return description;
    } else if (sessionTracker.createdHumanPlayerId != null && sessionTracker.userFromHistory != null) {
      if (sessionTracker.createdHumanPlayerId == sessionTracker.userFromHistory.humanPlayerId.playerId) {
        let description = "New user: ";
        if (sessionTracker.userFromHistory.name != null) {
          description += sessionTracker.userFromHistory.name
        } else {
          description += "(" + sessionTracker.userFromHistory.humanPlayerId.userId + ")";
        }
        return description;
      } else {
        return "Invalid (createdHumanPlayerId != userFromHistory.humanPlayerId.userId)";
      }
    } else {
      return null;
    }
  }

  analyseUserStateBgColor(sessionTracker: SessionTracker): string {
    if (sessionTracker.createdHumanPlayerId != null && sessionTracker.userFromHistory == null) {
      return "#d2d2d2"; // Anonymous
    } else if (sessionTracker.createdHumanPlayerId == null && sessionTracker.userFromHistory != null) {
      return "#cef8d0"; // Logged in
    } else if (sessionTracker.createdHumanPlayerId != null && sessionTracker.userFromHistory != null) {
      if (sessionTracker.createdHumanPlayerId == sessionTracker.userFromHistory.humanPlayerId.playerId) {
        return "#ffd7e0"; // New user
      } else {
        return "#FF0000"; // Invalid
      }
    } else {
      return "transparent";
    }
  }
}
