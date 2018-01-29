import {Component, OnInit} from '@angular/core';
import {Router} from "@angular/router";
import {FrontendService} from "../service/frontend.service";


@Component({
  templateUrl: 'home.component.html'
})

export class HomeComponent implements OnInit {

  constructor(private frontendService: FrontendService, private router: Router) {
  }

  ngOnInit(): void {
    if(!this.frontendService.isCookieAllowed()) {
      this.router.navigate(['/nocookies']);
      return;
    }
    this.frontendService.login().then(loggedIn => {
      if (loggedIn) {
        this.router.navigate(['/game']);
      }
    });
  }

}
