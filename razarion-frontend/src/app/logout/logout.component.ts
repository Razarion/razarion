import { Component, OnInit } from '@angular/core';
import { NavigationStart, Router } from "@angular/router";
import { FrontendService } from "../service/frontend.service";


@Component({
    templateUrl: 'logout.component.html',
    styleUrls: ['./logout.component.css'],
    standalone: false
})
export class LogoutComponent implements OnInit {
  constructor(private frontendService: FrontendService, private router: Router) {
    this.router.events.subscribe(event => {
      if (event instanceof NavigationStart) {
        window.location.replace('/');
      }
    });
  }

  ngOnInit(): void {
    try {
      this.frontendService.logout()
    } catch (e) {
      console.error(e)
    }
  }

  onLogin(): void {
    window.location.replace('/');
  }

  clearRemeberMe() {
    this.frontendService.clearRemeberMe();
  }
}
