import {Component} from '@angular/core';

@Component({
  selector: 'dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class Dashboard {
  enumAccess = HistoryView;
  currentView: HistoryView = HistoryView.SESSIONS;

  setView(newView: HistoryView) {
    this.currentView = newView;
  }
}

export enum HistoryView {
  SESSIONS = 0,
  NEW_USERS = 1,
  LOGIN_HISTORY = 3
}
