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
          if (response && response.authResponse) {
            this.frontendService.onFbAuthorized(response.authResponse).then(() => {
              // Angular problem with 3rd part library (Facebook) and routing https://github.com/angular/angular/issues/18254
              this.zone.run(() => this.router.navigate(['/game']));
            });
          } else {
            // Angular problem with 3rd part library (Facebook) and routing https://github.com/angular/angular/issues/18254
            this.zone.run(() => this.router.navigate(['/game']));
          }
        });

      }
    });
  }
}
