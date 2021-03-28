import {ErrorHandler, Injectable, Injector} from '@angular/core';
import {FrontendService} from "./service/frontend.service";
import {MessageService} from "primeng/api";

@Injectable()
export class GlobalErrorHandler implements ErrorHandler {
  constructor(private injector: Injector, private messageService: MessageService) {
  }

  handleError(error: any) {
    const frontendService: FrontendService = this.injector.get(FrontendService);
    frontendService.log("GlobalErrorHandler", error);
    this.messageService.add({
      severity: 'error',
      summary: `Unknown Error`,
      detail: error,
      sticky: true
    });
    throw error;
  }

}
