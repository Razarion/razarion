import {Component} from '@angular/core';
import {StartupTrackingComponent} from './startup-tracking/startup-tracking.component';
import {UserMgmtComponent} from '../editor/user-mgmt/user-mgmt.component';
import {UserService} from '../auth/user.service';
import {NgIf} from '@angular/common';
import {LoginComponent} from '../auth/login/login.component';

@Component({
  selector: 'app-backend',
  templateUrl: './backend.component.html',
  imports: [
    StartupTrackingComponent,
    UserMgmtComponent,
    NgIf,
    LoginComponent
  ]
})
export class BackendComponent {

  constructor(public userService: UserService) {
  }
}
