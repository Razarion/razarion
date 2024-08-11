import { Component, NgZone, OnDestroy, OnInit } from '@angular/core';
import { Router } from "@angular/router";
import { FrontendService } from "../service/frontend.service";
import { RegisterResult } from "../common";

@Component({
  templateUrl: 'register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent implements OnInit, OnDestroy {
  email: string = "";
  password: string = "";
  passwordConfirm: string = "";
  rememberMe: boolean = true;
  private facebookEventCallback: any = null;
  emailError: string = "";
  passwordError: string = "";
  passwordConfirmError: string = "";
  registered: boolean = false;
  bouncingStopper: boolean = false;
  working: boolean = false;

  constructor(private frontendService: FrontendService, private router: Router, private zone: NgZone) {
  }

  ngOnInit(): void {
    if (!this.frontendService.isCookieAllowed()) {
      this.router.navigate(['/nocookies']);
      return;
    }
    this.frontendService.autoLogin().then(loggedIn => {
      if (loggedIn) {
        this.router.navigate(['/game']);
      }
    });
  }

  ngOnDestroy(): void {
    if (this.facebookEventCallback != null) {
      this.facebookEventCallback = null;
    }
  }

  onRegister() {
    this.emailError = "";
    this.passwordError = "";
    this.passwordConfirmError = "";

    if (!FrontendService.validateEmail(this.email)) {
      this.emailError = "Bitte gib eine gültige E-Mail Adresse an";
    } else if (this.password == "") {
      this.passwordError = "Passwort is leer"
    } else if (this.password != this.passwordConfirm) {
      this.passwordConfirmError = "Passwörter sind nicht identisch"
    } else {
      this.working = true;
      this.frontendService.register(this.email, this.password, this.rememberMe).then(registerResult => {
        this.working = false;
        switch (registerResult) {
          case RegisterResult.USER_ALREADY_LOGGED_IN:
            this.emailError = "Du bist bereits eingeloggt";
            break;
          case RegisterResult.INVALID_EMAIL:
            this.emailError = "Bitte gib eine gültige E-Mail Adresse an";
            break;
          case RegisterResult.EMAIL_ALREADY_USED:
            this.emailError = "Diese Email-Adresse wurde bereits verwendet";
            break;
          case RegisterResult.INVALID_PASSWORD:
            this.passwordError = "Das Passwort ist ungültig";
            break;
          case RegisterResult.UNKNOWN_ERROR:
            this.emailError = "Unbekannter Fehler";
            break;
          case RegisterResult.OK:
            this.registered = true;
            break;
        }
      });
    }
  }

  onKeyEmail(email: string) {
    this.emailError = "";
    if (!FrontendService.validateEmail(email)) {
      this.emailError = "Bitte gib eine gültige E-Mail Adresse an";
      return;
    }
    this.frontendService.verifyEmail(email).then(success => {
      if (success) {
        this.emailError = "";
      } else {
        this.emailError = "Diese Email-Adresse wurde bereits verwendet";
      }
    });
  }

  onKeyPassword(password: string) {
    this.passwordError = "";
    if (password == "") {
      this.passwordError = "Passwort is leer";
    }
  }

  onKeyPasswordConfirm(passwordConfirm: string) {
    this.passwordConfirmError = "";
    if (this.password != passwordConfirm) {
      this.passwordConfirmError = "Passwörter sind nicht identisch"
    }
  }

  onPlay() {
    this.router.navigate(['/game']);
  }
}
