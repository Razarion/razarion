import {Component} from '@angular/core';
import {LoginComponent} from "../login/login.component";
import {Button} from "primeng/button";
import {AuthService} from '../auth.service';

@Component({
  selector: 'invalid-token',
  imports: [
    LoginComponent,
    Button
  ],
  templateUrl: './invalid-token.component.html'
})
export class InvalidTokenComponent {
  constructor(private authService: AuthService) {
  }

  signOutAndPlayGuast() {
    this.authService.logout();
    window.location.replace("/game");
  }

}
