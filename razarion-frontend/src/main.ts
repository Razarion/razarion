import {bootstrapApplication} from '@angular/platform-browser';
import {appConfig} from './app/app.config';
import {AppComponent} from './app/app.component';
import {shouldStartWasmEarly, startWasmDownload} from './app/wasm-boot';

// Before Angular, and deliberately so: the engine is 613 KB that compiles while the rest of the
// bundle is still arriving, and nothing in fetching it needs a running application. See wasm-boot.
if (shouldStartWasmEarly()) {
  startWasmDownload();
}

bootstrapApplication(AppComponent, appConfig)
  .catch((err) => console.error(err));
