import {Component} from '@angular/core';
import {NavigationStart, Router} from "@angular/router";
import {FrontendService} from "./service/frontend.service";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {

  constructor(private router: Router, private frontendService: FrontendService) {
    window.addEventListener("beforeunload", event => {
      frontendService.logWindowClosed(event);
    });

    router.events.subscribe(event => {
      if (event instanceof NavigationStart) {
        frontendService.trackNavigation(event.url);
      }
      // NavigationEnd
      // NavigationCancel
      // NavigationError
      // RoutesRecognized
    });
  }

}
