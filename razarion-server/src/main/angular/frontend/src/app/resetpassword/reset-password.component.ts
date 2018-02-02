import {Component} from '@angular/core';
import {FrontendService} from "../service/frontend.service";


@Component({
  templateUrl: 'reset-password.component.html'
})
export class ResetPasswordComponent {
  passwordResetInitiated: boolean = false;
  emailError: string = "";
  email: string = "";

  constructor(private frontendService: FrontendService) {
  }

  onPlay() {
    this.emailError = "";
    if (this.email != "") {
      this.frontendService.sendEmailForgotPassword(this.email).then(success => {
        if(success) {
          this.passwordResetInitiated = true;
        }
      });
    } else {
      this.emailError = "Bitte gib eine gültige E-Mail Adresse ein";
    }
  }

}
