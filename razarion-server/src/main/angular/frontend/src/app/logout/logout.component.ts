import {Component, OnInit} from '@angular/core';
import {NavigationStart, Router} from "@angular/router";
import {FrontendService} from "../service/frontend.service";


@Component({
  templateUrl: 'logout.component.html',
  styleUrls: ['./logout.component.css']
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
    this.frontendService.logout()
  }

  onLogin(): void {
    window.location.replace('/');
  }
}
