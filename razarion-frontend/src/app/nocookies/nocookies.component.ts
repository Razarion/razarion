import {Component} from '@angular/core';
import {Router} from "@angular/router";
import {FrontendService} from "../service/frontend.service";


@Component({
  templateUrl: 'nocookies.component.html',
  styleUrls: ['./nocookies.component.css']
})

export class NoCookies {
  constructor(private frontendService: FrontendService, private router: Router) {
    if (this.frontendService.isCookieAllowed()) {
      this.router.navigate(['/']);
    }
  }

}
