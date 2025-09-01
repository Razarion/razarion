import {Component, OnInit} from '@angular/core';
import {StartupTrackingComponent} from './startup-tracking/startup-tracking.component';
import {UserMgmtComponent} from '../editor/user-mgmt/user-mgmt.component';
import {UserService} from '../auth/user.service';
import {CommonModule} from '@angular/common';
import {LoginComponent} from '../auth/login/login.component';

@Component({
  selector: 'app-backend',
  templateUrl: './backend.component.html',
  imports: [
    StartupTrackingComponent,
    UserMgmtComponent,
    CommonModule,
    LoginComponent
  ]
})
export class BackendComponent implements OnInit {
  showLogin = false;
  showBackend = false;

  constructor(private userService: UserService) {
  }

  ngOnInit(): void {
    this.userService.checkToken2()
      .then(() => {
        if (this.userService.isAdmin()) {
          this.showBackend = true;
          this.showLogin = false;
        } else {
          this.showBackend = false;
          this.showLogin = true;
        }
      })
      .catch(() => {
        this.showBackend = false;
        this.showLogin = true;
      });
  }
}
