import {Component, OnInit} from '@angular/core';
import {Router} from "@angular/router";
import {FrontendService} from "../service/frontend.service";


@Component({
  templateUrl: 'logout.component.html',
})
export class LogoutComponent implements OnInit {
  constructor(private frontendService: FrontendService, private router: Router) {
  }

  ngOnInit(): void {
    this.frontendService.logout()
  }

  onLogin(): void {
    this.router.navigate(['/']);
  }
}
