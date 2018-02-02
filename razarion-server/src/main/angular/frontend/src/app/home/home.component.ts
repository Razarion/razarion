import {Component, OnInit} from '@angular/core';
import {Router} from "@angular/router";
import {FrontendService} from "../service/frontend.service";
import {LoginResult} from "../common";


@Component({
  templateUrl: 'home.component.html'
})

export class HomeComponent implements OnInit {
  email: string = "";
  password: string = "";
  rememberMe: boolean = true;
  loginError: string = "";

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
  }

  onLogin() {
    this.frontendService.login(this.email, this.password).then(loginResult => {
      switch (loginResult) {
        case LoginResult.OK:
          this.router.navigate(['/game']);
          break;
        case LoginResult.WRONG_EMAIL:
          this.loginError = "Unbekannte E-Mail Adresse";
          break;
        case LoginResult.WRONG_PASSWORD:
          this.loginError = "Falsches Passworte";
          break;
        case LoginResult.UNKNOWN:
          this.loginError = "Unbekannter Fehler";
          break;
      }
    });
  }
}
