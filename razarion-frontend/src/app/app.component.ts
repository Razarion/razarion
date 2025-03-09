import {Component, Injector} from '@angular/core';
import {NavigationStart, Router} from "@angular/router";
import {FrontendService} from "./service/frontend.service";

@Component({
    selector: 'app-root',
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.scss'],
    standalone: false
})
export class AppComponent {

  constructor(injector: Injector, private router: Router, private frontendService: FrontendService) {
    // TODO window.addEventListener("beforeunload", event => {
    //   frontendService.logWindowClosed(event);
    // });

    // TODO router.events.subscribe(event => {
    //   if (event instanceof NavigationStart) {
    //     frontendService.trackNavigation(event.url);
    //   }
    //   // NavigationEnd
    //   // NavigationCancel
    //   // NavigationError
    //   // RoutesRecognized
    // });
  }

}
