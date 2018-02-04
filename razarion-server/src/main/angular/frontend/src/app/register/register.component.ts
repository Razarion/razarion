import {Component, OnDestroy, OnInit} from '@angular/core';
import {Router} from "@angular/router";
import {FrontendService} from "../service/frontend.service";
import {RegisterResult} from "../common";

@Component({
  templateUrl: 'register.component.html'
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

  constructor(private frontendService: FrontendService, private router: Router) {
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
    if (this.facebookEventCallback != null) {
      this.frontendService.unsubscribeFbAuthChange(this.facebookEventCallback);
      this.facebookEventCallback = null;
    }
    this.facebookEventCallback = function (fbResponse) {
      this.frontendService.onFbAuthorized().then(success => {
        if (success) {
          this.registered = true;
          this.router.navigate(['/game']);
        }
      });
    };
    this.frontendService.subscribeFbAuthChange(this.facebookEventCallback);
    this.frontendService.parseFbXFBML();
  }

  ngOnDestroy(): void {
    if (this.facebookEventCallback != null) {
      this.frontendService.unsubscribeFbAuthChange(this.facebookEventCallback);
      this.facebookEventCallback = null;
    }
  }

  onRegister() {
    this.emailError = "";
    this.passwordError = "";
    this.passwordConfirmError = "";

    if (!RegisterComponent.validateEmail(this.email)) {
      this.emailError = "Bitte gib eine gültige E-Mail Adresse an";
    } else if (this.password == "") {
      this.passwordError = "Passwort is leer"
    } else if (this.password != this.passwordConfirm) {
      this.passwordConfirmError = "Passwörter sind nicht identisch"
    } else {
      this.frontendService.register(this.email, this.password, this.rememberMe).then(registerResult => {
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
    if (!RegisterComponent.validateEmail(email)) {
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

  onKeyPasswordConfirm(passwordConfirm: string) {
    this.passwordConfirmError = "";
    if (this.password != passwordConfirm) {
      this.passwordConfirmError = "Passwörter sind nicht identisch"
    }
  }

  onPlay() {
    this.router.navigate(['/game']);
  }

  onContinue() {
    this.router.navigate(['/game']);
  }

  static validateEmail(email) {
    if (email == null || email == "") {
      return false;
    }
    let re = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
    return re.test(String(email).toLowerCase());
  }

}
