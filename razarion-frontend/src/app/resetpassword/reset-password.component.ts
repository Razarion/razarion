import {Component} from '@angular/core';
import {FrontendService} from "../service/frontend.service";


@Component({
    templateUrl: 'reset-password.component.html',
    styleUrls: ['./reset-password.component.css'],
    standalone: false
})
export class ResetPasswordComponent {
  passwordResetInitiated: boolean = false;
  emailError: string = "";
  email: string = "";
  working: boolean = false;

  constructor(private frontendService: FrontendService) {
  }

  onKeyEmail(email: string) {
    this.emailError = "";
    if (!FrontendService.validateEmail(email)) {
      this.emailError = "Bitte gib eine gültige E-Mail Adresse an";
      return;
    }
  }

  onReset() {
    this.emailError = "";
    if (!FrontendService.validateEmail(this.email)) {
      this.emailError = "Bitte gib eine gültige E-Mail Adresse an";
      return;
    }

    this.working = true;
    this.frontendService.sendEmailForgotPassword(this.email).then(success => {
      this.working = false;
      if (success) {
        this.passwordResetInitiated = true;
      } else {
        this.emailError = "Bitte gib eine gültige E-Mail Adresse an";
      }
    });
  }

}
