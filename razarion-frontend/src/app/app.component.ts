import {Component} from '@angular/core';
import {Toast} from 'primeng/toast';
import {RouterOutlet} from '@angular/router';
import {MessageService} from 'primeng/api';
import {LoggingControllerImplClient} from './generated/razarion-share';
import {TypescriptGenerator} from './backend/typescript-generator';
import {HttpClient} from '@angular/common/http';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  standalone: true,
  imports: [
    Toast,
    RouterOutlet
  ],
  providers: [MessageService]
})
export class AppComponent {

  constructor(httpClient: HttpClient) {
    const loggingController = new LoggingControllerImplClient(TypescriptGenerator.generateHttpClientAdapter(httpClient));

    const originalWarn = console.warn;
    console.warn = function (...args) {
      originalWarn.apply(console, args);
      try {
        loggingController.angularJsonLogger({
          level: 'warn',
          message: args.map(arg => (typeof arg === 'string' ? arg : JSON.stringify(arg))).join(' '),
          millis: Date.now().toString(),
          thrown: null,
          loggerName: 'console',
          gwtStrongName: null,
          gwtModuleName: null
        }).catch(() => {
        });
      } catch (e) {
      }
    };

    const originalError = console.error;
    console.error = function (...args) {
      originalError.apply(console, args);
      try {
        loggingController.angularJsonLogger({
          level: 'error',
          message: args.map(arg => (typeof arg === 'string' ? arg : JSON.stringify(arg))).join(' '),
          millis: Date.now().toString(),
          thrown: null,
          loggerName: 'console',
          gwtStrongName: null,
          gwtModuleName: null
        }).catch(() => {
        });
      } catch (e) {
      }
    };
  }
}
