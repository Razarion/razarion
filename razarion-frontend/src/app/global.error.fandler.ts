import {ErrorHandler, Injectable, Injector} from '@angular/core';
import {FrontendService} from "./service/frontend.service";
import {MessageService} from "primeng/api";

@Injectable()
export class GlobalErrorHandler implements ErrorHandler {
  constructor(private injector: Injector, private messageService: MessageService) {
  }

  handleError(error: any) {
    const frontendService: FrontendService = this.injector.get(FrontendService);
    frontendService.log("Angular GlobalErrorHandler", error);
    console.error(error);
    this.messageService.add({
      severity: 'error',
      summary: `Unknown Error`,
      detail: String(error),
      sticky: true
    });
  }

}
