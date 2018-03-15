import {Component, NgZone, OnInit} from '@angular/core';
import {Router} from "@angular/router";
import {FrontendService} from "../service/frontend.service";

@Component({
  templateUrl: 'facebook-app-start.component.html'
})

export class FacebookAppStart implements OnInit {

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
      } else {
        this.frontendService.fbLogin(response => {
          try {
            if ((<any>window).RAZ_inGameFbAuthResponsecallback) {
              (<any>window).RAZ_inGameFbAuthResponseCallback(response);
            } else {
              (<any>window).RAZ_inGameFbAuthResponse = response;
            }
          } catch (error) {
            this.frontendService.log("FB auth reponose handling failed", error);
          }
        });
        this.router.navigate(['/game']);
      }
    });
  }

}
