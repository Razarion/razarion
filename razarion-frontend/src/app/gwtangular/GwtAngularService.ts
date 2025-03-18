import {Injectable, NgZone} from "@angular/core";
import {GwtAngularFacade} from "./GwtAngularFacade";

declare global {
  interface Window {
    gwtAngularFacade: GwtAngularFacade;
  }
}

@Injectable({
  providedIn: 'root' // This ensures it is available application-wide
})
export class GwtAngularService {
  gwtAngularFacade!: GwtAngularFacade;
  crashListener!: () => void;

  constructor(zone: NgZone/* TODO , babylonModelService: BabylonModelService*/) {
    const self = this;
    this.gwtAngularFacade = new class extends GwtAngularFacade {
      onCrash(): void {
        zone.run(() => {
          self.crashListener();
        });
      }
    };
    // TODO this.gwtAngularFacade.gwtAngularBoot = new class implements GwtAngularBoot {
    //   loadThreeJsModels() {
    //     return babylonModelService.init();
    //   };
    // }
    window.gwtAngularFacade = this.gwtAngularFacade;
  }
}
