import {ErrorHandler, Injectable, Injector} from '@angular/core';
import {LocationStrategy, PathLocationStrategy} from '@angular/common';
import {FrontendService} from "./service/frontend.service";

@Injectable()
export class GlobalErrorHandler implements ErrorHandler {
  constructor(private injector: Injector) {
  }

  handleError(error: any) {
    const frontendService: FrontendService = this.injector.get(FrontendService);
    const location = this.injector.get(LocationStrategy);
    const url = location instanceof PathLocationStrategy ? location.path() : '';
    frontendService.log("GlobalErrorHandler url: '" + url + "'", error);
    throw error;
  }

}
