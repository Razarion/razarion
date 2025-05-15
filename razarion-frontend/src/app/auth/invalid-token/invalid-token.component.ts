import {Component} from '@angular/core';
import {Router} from '@angular/router';
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
  constructor(private router: Router, private authService: AuthService) {
  }

  signOutAndPlayGuast() {
    this.authService.logout();
    this.router.navigate(['/']);
  }

}
