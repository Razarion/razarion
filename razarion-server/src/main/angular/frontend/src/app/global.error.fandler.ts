import {ErrorHandler, Injectable, Injector} from '@angular/core';
import {FrontendService} from "./service/frontend.service";

@Injectable()
export class GlobalErrorHandler implements ErrorHandler {
  constructor(private injector: Injector) {
  }

  handleError(error: any) {
    const frontendService: FrontendService = this.injector.get(FrontendService);
    frontendService.log("GlobalErrorHandler", error);
    throw error;
  }

}
