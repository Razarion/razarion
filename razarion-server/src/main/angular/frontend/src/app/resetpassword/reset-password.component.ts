﻿import {Component} from '@angular/core';
import {FrontendService} from "../service/frontend.service";
import {RegisterComponent} from "../register/register.component";


@Component({
  templateUrl: 'reset-password.component.html'
})
export class ResetPasswordComponent {
  passwordResetInitiated: boolean = false;
  emailError: string = "";
  email: string = "";

  constructor(private frontendService: FrontendService) {
  }

  onReset() {
    this.emailError = "";
    if (!RegisterComponent.validateEmail(this.email)) {
      this.emailError = "Bitte gib eine gültige E-Mail Adresse an";
      return;
    }

    this.frontendService.sendEmailForgotPassword(this.email).then(success => {
      if (success) {
        this.passwordResetInitiated = true;
      } else {
        this.emailError = "Bitte gib eine gültige E-Mail Adresse an";
      }
    });
  }

}