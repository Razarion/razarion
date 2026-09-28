import {bootstrapApplication} from '@angular/platform-browser';
import {appConfig} from './app/app.config';
import {AppComponent} from './app/app.component';
import {shouldStartWasmEarly, startWasmDownload} from './app/wasm-boot';
import {prefetchTerrain} from './app/terrain-prefetch';
import {prefetchWorker} from './app/worker-prefetch';

// Before Angular, and deliberately so: the engine is 613 KB that compiles while the rest of the
// bundle is still arriving, and nothing in fetching it needs a running application. See wasm-boot.
if (shouldStartWasmEarly()) {
  startWasmDownload();
  // The same reasoning for the terrain the worker will ask for. See terrain-prefetch.
  prefetchTerrain();
  // And for the worker itself, which the client starts only once it runs. See worker-prefetch.
  prefetchWorker();
}

bootstrapApplication(AppComponent, appConfig)
  .catch((err) => console.error(err));
