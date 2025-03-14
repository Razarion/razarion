﻿import {Component, OnInit} from '@angular/core';
import {Router} from "@angular/router";
import {FrontendService} from "../service/frontend.service";
import { LoginResult } from '../generated/razarion-share';


@Component({
    templateUrl: 'home.component.html',
    styleUrls: ['./home.component.css'],
    standalone: false
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
    if (!FrontendService.validateEmail(this.email)) {
      this.loginError = "Bitte gib eine gültige E-Mail Adresse an";
      return;
    }

    this.frontendService.login(this.email, this.password, this.rememberMe).then(loginResult => {
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
        default:
          this.loginError = "Unbekannter Fehler";
          break;
      }
    });
  }
}
