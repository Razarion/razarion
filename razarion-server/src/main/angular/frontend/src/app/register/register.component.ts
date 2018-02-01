import {Component, OnDestroy, OnInit} from '@angular/core';
import {Router} from "@angular/router";
import {FrontendService} from "../service/frontend.service";

declare const FB: any;


@Component({
  templateUrl: 'register.component.html'
})
export class RegisterComponent implements OnInit, OnDestroy {
  email: string = "E-MAil-Adresse";
  password: string = "Passwort";
  passwordConfirm: string = "Passwort bestätigen";
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
    this.frontendService.login().then(loggedIn => {
      if (loggedIn) {
        this.router.navigate(['/game']);
      }
    });
    if (this.facebookEventCallback != null) {
      FB.Event.unsubscribe("auth.statusChange", this.facebookEventCallback);
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
    FB.Event.subscribe("auth.statusChange", this.facebookEventCallback);
    FB.XFBML.parse();
  }

  ngOnDestroy(): void {
    if (this.facebookEventCallback != null) {
      FB.Event.unsubscribe("auth.statusChange", this.facebookEventCallback);
      this.facebookEventCallback = null;
    }
  }

  onRegister() {
    this.password = "";
    this.passwordError = "";

    if (this.password == "") {
      this.passwordError = "Passwort is leer"
    } else if (this.password != this.passwordConfirm) {
      this.passwordConfirmError = "Passwörter sind nicht identisch"
    } else {
      this.frontendService.register(this.email, this.password, this.rememberMe).then(success => {
        if (success) {
          this.registered = true;
        } else {
          this.emailError = "Diese Email-Adresse wurde bereits verwendet";
        }
      });
    }
  }

  onContinue() {
    this.router.navigate(['/game']);
  }

  onKeyEmail(email: string) {
    this.frontendService.verifyEmail(email).then(success => {
      if (success) {
        this.emailError = "";
      } else {
        this.emailError = "Diese Email-Adresse wurde bereits verwendet";
      }
    });
  }

  onPlay() {
    this.router.navigate(['/game']);
  }
}
