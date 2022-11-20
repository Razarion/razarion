import { Injectable, NgZone } from "@angular/core";
import { BabylonModelService } from "../game/renderer/babylon-model.service";
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

  constructor(private zone: NgZone, threeJsModelService: BabylonModelService) {
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
