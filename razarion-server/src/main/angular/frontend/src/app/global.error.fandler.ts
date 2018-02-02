import {ErrorHandler, Injectable, Injector} from '@angular/core';
import {LocationStrategy, PathLocationStrategy} from '@angular/common';
import {FrontendService} from "./service/frontend.service";

@Injectable()
export class GlobalErrorHandler implements ErrorHandler {
  constructor(private injector: Injector) {
  }

  handleError(error) {
    const frontendService: FrontendService = this.injector.get(FrontendService);
    const location = this.injector.get(LocationStrategy);
    const message = error.message ? error.message : error.toString();
    const url = location instanceof PathLocationStrategy ? location.path() : '';
    let stack:string="";
    if(error.stack) {
      stack = " stack: '" + error.stack + "'."
    }

    frontendService.log("GlobalErrorHandler. message: '" + message + "'. url: '" + url + "'." + stack);

    throw error;
  }

}
