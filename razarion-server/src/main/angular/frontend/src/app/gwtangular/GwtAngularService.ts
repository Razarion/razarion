import { Injectable, NgZone } from "@angular/core";
import { ThreeJsModelService } from "../game/renderer/three-js-model.service";
import { GwtAngularBoot, GwtAngularFacade, ThreeJsModelConfig } from "./GwtAngularFacade";

declare global {
  interface Window {
    gwtAngularFacade: GwtAngularFacade;
  }
}

@Injectable()
export class GwtAngularService {
  gwtAngularFacade!: GwtAngularFacade;
  crashListener!: () => void;

  constructor(private zone: NgZone, threeJsModelService: ThreeJsModelService) {
    const self = this;
    this.gwtAngularFacade = new class extends GwtAngularFacade {
      onCrash(): void {
        zone.run(() => {
          self.crashListener();
        });
      }
    };
    this.gwtAngularFacade.gwtAngularBoot = new class implements GwtAngularBoot {
      loadThreeJsModels(threeJsModelConfigs: ThreeJsModelConfig[]) {
        return threeJsModelService.init(threeJsModelConfigs, self);
      };
    }
    window.gwtAngularFacade = this.gwtAngularFacade;
  }
}
