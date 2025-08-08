import {Component} from '@angular/core';
import {LoginComponent} from "../login/login.component";
import {Button} from "primeng/button";
import {UserService} from '../user.service';

@Component({
  selector: 'invalid-token',
  imports: [
    LoginComponent,
    Button
  ],
  templateUrl: './invalid-token.component.html'
})
export class InvalidTokenComponent {
  constructor(private userService: UserService) {
  }

  signOutAndPlayGuest() {
    this.userService.logout();
    window.location.replace("/game");
  }

}
