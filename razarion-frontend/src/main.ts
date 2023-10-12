import {enableProdMode} from '@angular/core';
import {platformBrowserDynamic} from '@angular/platform-browser-dynamic';

import {AppModule} from './app/app.module';
import {environment} from './environments/environment';

// ------------ facebook script loading control-------------
// (<any>window).RAZ_fbScriptLoadedFlag = false;
// (<any>window).RAZ_fbScriptLoadedFrontendService = null;
// (<any>window).RAZ_fbScriptLoadedCallback = null;
// ------------ facebook script loading control ends -------------

if (environment.production) {
  enableProdMode();
}

// ------------ init facebook -------------
// declare const FB: any;
// try {
//   (<any>window).fbAsyncInit = function () {
//     FB.init({
//       appId: environment.facebookAppId,
//       cookie: true,
//       xfbml: true,
//       version: 'v2.11'
//     });
//     (<any>window).RAZ_fbScriptLoadedFlag = true;
//     if ((<any>window).RAZ_fbScriptLoadedCallback != null) {
//       (<any>window).RAZ_fbScriptLoadedCallback((<any>window).RAZ_fbScriptLoadedFrontendService);
//     }
//   };
//
//   (function (d, s, id) {
//     let js, fjs = d.getElementsByTagName(s)[0];
//     if (d.getElementById(id)) {
//       return;
//     }
//     js = d.createElement(s);
//     js.id = id;
//     js.src = "https://connect.facebook.net/de_DE/sdk.js";
//     fjs.parentNode.insertBefore(js, fjs);
//   }(document, 'script', 'facebook-jssdk'));
// } catch (error) {
//   window.onerror("Loading Facebook script failed", null, null, null, error);
// }
// ------------ init facebook ends -------------

platformBrowserDynamic().bootstrapModule(AppModule)
  .catch(err => console.log(err));
