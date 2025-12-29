import {Component, OnInit} from '@angular/core';
import {UserService} from '../auth/user.service';
import {CommonModule} from '@angular/common';
import {LoginComponent} from '../auth/login/login.component';
import {TrackingContainerComponent} from './tracking-container/tracking-container.component';

@Component({
  selector: 'app-backend',
  templateUrl: './backend.component.html',
  imports: [
    CommonModule,
    LoginComponent,
    TrackingContainerComponent
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
