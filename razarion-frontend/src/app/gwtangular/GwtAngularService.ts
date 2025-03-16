import {Injectable} from "@angular/core";
import {GwtAngularFacade} from "./GwtAngularFacade";

declare global {
  interface Window {
    gwtAngularFacade: GwtAngularFacade;
  }
}

@Injectable()
export class GwtAngularService {
  gwtAngularFacade!: GwtAngularFacade;
  crashListener!: () => void;

  // TODO constructor(zone: NgZone, babylonModelService: BabylonModelService) {
  //   const self = this;
  //   this.gwtAngularFacade = new class extends GwtAngularFacade {
  //     onCrash(): void {
  //       zone.run(() => {
  //         self.crashListener();
  //       });
  //     }
  //   };
  //   this.gwtAngularFacade.gwtAngularBoot = new class implements GwtAngularBoot {
  //     loadThreeJsModels() {
  //       return babylonModelService.init();
  //     };
  //   }
  //   window.gwtAngularFacade = this.gwtAngularFacade;
  // }
}
