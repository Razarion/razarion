import {ApplicationRef, Component, NgZone} from '@angular/core';
import {Toast} from 'primeng/toast';
import {RouterOutlet} from '@angular/router';
import {MessageService} from 'primeng/api';
import {LoggingControllerImplClient} from './generated/razarion-share';
import {TypescriptGenerator} from './backend/typescript-generator';
import {HttpClient} from '@angular/common/http';
import {RemoteLogging} from './backend/remote-logging';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  standalone: true,
  imports: [
    Toast,
    RouterOutlet
  ],
})
export class AppComponent {

  constructor(httpClient: HttpClient, ngZone: NgZone, appRef: ApplicationRef) {
    // Expose for E2E testing
    (window as any).__e2eNgZone = ngZone;
    (window as any).__e2eAppRef = appRef;
    const loggingController = new LoggingControllerImplClient(TypescriptGenerator.generateHttpClientAdapter(httpClient));

    // Read once: navigator does not change while the tab lives, and a console hook must not do
    // more work per line than it has to.
    const nav = navigator as Navigator & { deviceMemory?: number };
    const userAgent = nav.userAgent ?? null;
    const hardwareConcurrency = nav.hardwareConcurrency ?? null;
    // Chromium only, and rounded to a power of two there. Absent everywhere else, hence the null.
    const deviceMemory = nav.deviceMemory ?? null;

    const forward = (level: string, args: any[]) => {
      if (!RemoteLogging.allow()) {
        return;
      }
      try {
        loggingController.angularJsonLogger({
          level: level,
          message: args.map(arg => (typeof arg === 'string' ? arg : JSON.stringify(arg))).join(' '),
          millis: Date.now().toString(),
          thrown: null,
          loggerName: 'console',
          gwtStrongName: null,
          gwtModuleName: null,
          userAgent: userAgent,
          hardwareConcurrency: hardwareConcurrency,
          deviceMemory: deviceMemory
        }).catch(() => {
        });
      } catch (e) {
      }
    };

    const originalWarn = console.warn;
    console.warn = function (...args) {
      originalWarn.apply(console, args);
      forward('warn', args);
    };

    const originalError = console.error;
    console.error = function (...args) {
      originalError.apply(console, args);
      forward('error', args);
    };
  }
}
