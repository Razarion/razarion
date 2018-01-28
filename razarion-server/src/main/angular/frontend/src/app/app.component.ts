import {Component, OnInit} from '@angular/core';
import {FrontendService} from "./service/frontend.service";
import {Router} from "@angular/router";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {
  title = 'Razarion';

  constructor(private frontendService: FrontendService, private router: Router) {
  }

  ngOnInit(): void {
    this.frontendService.start().then(loggedIn => {
      this.router.navigate(['/game']);
    });
  }
}
