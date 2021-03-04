import {Injectable} from "@angular/core";
import {GwtAngularFacade} from "./GwtAngularFacade";

declare global {
  interface Window {
    gwtAngularFacade: GwtAngularFacade;
  }
}

@Injectable()
export class GwtAngularService {
  gwtAngularFacade: GwtAngularFacade = new GwtAngularFacade();

  constructor() {
    window.gwtAngularFacade = this.gwtAngularFacade;
  }
}
