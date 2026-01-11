import {Component} from '@angular/core';
import {Button} from 'primeng/button';
import {FormsModule} from '@angular/forms';

import {UserService} from '../user.service';
import {RegisterResult} from '../../generated/razarion-share';
import {CockpitDisplayService} from '../../game/cockpit/cockpit-display.service';

@Component({
  selector: 'register',
  standalone: true,
  imports: [
    Button,
    FormsModule
],
  templateUrl: './register.component.html'
})
export class RegisterComponent {
  email: string = "";
  password1: string = "";
  password2: string = "";
  errorMessage: string = "";


  constructor(private mainCockpitService: CockpitDisplayService,
              private userService: UserService) {
  }

  onRegister() {
    if (!this.email || !this.password1 || !this.password2) {
      this.errorMessage = "All fields are required.";
      return;
    }

    if (this.password1 !== this.password2) {
      this.errorMessage = "Passwords do not match.";
      return;
    }

    if (!this.validateEmail(this.email)) {
      this.errorMessage = "Please enter a valid email address.";
      return;
    }

    this.errorMessage = "";
    this.userService.register(this.email, this.password1).then(registerResult => {
      switch (registerResult) {
        case RegisterResult.USER_ALREADY_LOGGED_IN:
          this.errorMessage = "You are already logged in.";
          break;
        case RegisterResult.INVALID_EMAIL:
          this.errorMessage = "Invalid email address.";
          break;
        case RegisterResult.EMAIL_ALREADY_USED:
          this.errorMessage = "This email is already in use.";
          break;
        case RegisterResult.INVALID_PASSWORD:
          this.errorMessage = "Invalid password.";
          break;
        case RegisterResult.OK:
          this.mainCockpitService.showRegisterDialog = false;
          this.mainCockpitService.showRegisteredDialog = true;
          break;
        case RegisterResult.UNKNOWN_ERROR:
          this.errorMessage = "Something went wrong. Please try again.";
          break;

      }
    });
  }

  private validateEmail(email: string): boolean {
    const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return re.test(email);
  }
}
