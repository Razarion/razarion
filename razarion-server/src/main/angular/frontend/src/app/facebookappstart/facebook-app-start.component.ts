import {AfterViewChecked, Component, NgZone, OnInit} from '@angular/core';
import {Router} from "@angular/router";
import {FrontendService} from "../service/frontend.service";

@Component({
  templateUrl: 'facebook-app-start.component.html',
  styleUrls: ['./facebook-app-start.component.css']
})

export class FacebookAppStart implements OnInit, AfterViewChecked {
  private bouncingStopper: boolean = false;
  private facebookEventCallback: any = null;
  private fbLoginBouncingStopper: boolean = false;

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
            if (this.fbLoginBouncingStopper) {
              return;
            }
            this.fbLoginBouncingStopper = true;
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

  ngAfterViewChecked(): void {
    if (this.bouncingStopper) {
      return;
    }
    this.bouncingStopper = true;
    this.frontendService.fbScriptLoaded().then(() => {
      if (this.facebookEventCallback != null) {
        this.frontendService.unsubscribeFbAuthChange(this.facebookEventCallback);
        this.facebookEventCallback = null;
      }
      this.facebookEventCallback = (fbResponse) => {
        if (fbResponse.status === "connected") {
          if (this.fbLoginBouncingStopper) {
            return;
          }
          this.fbLoginBouncingStopper = true;
          this.frontendService.onFbAuthorized(fbResponse.authResponse).then(success => {
            // Angular problem with 3rd part library (Facebook) and routing https://github.com/angular/angular/issues/18254
            this.zone.run(() => this.router.navigate(['/game']));
          });
        }

      };
      this.frontendService.subscribeFbAuthChange(this.facebookEventCallback);
      this.frontendService.parseFbXFBML();
    });
  }

  onPlay() {
    if (this.facebookEventCallback != null) {
      this.frontendService.unsubscribeFbAuthChange(this.facebookEventCallback);
      this.facebookEventCallback = null;
    }
    this.router.navigate(['/game']);
  }

}
