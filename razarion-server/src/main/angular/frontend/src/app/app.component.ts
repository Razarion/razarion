import {Component, Injector} from '@angular/core';
import {NavigationStart, Router} from "@angular/router";
import {FrontendService} from "./service/frontend.service";
import { createCustomElement } from '@angular/elements';
import {PropertyTableComponent} from "./editor/property-table/property-table.component";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
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

    const propertyTableComponent = createCustomElement(PropertyTableComponent, {injector});
    // Register the custom element with the browser.
    customElements.define('angular-property-table', propertyTableComponent);

  }

}
