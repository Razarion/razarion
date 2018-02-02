import {BrowserModule} from '@angular/platform-browser';
import {ErrorHandler, NgModule} from '@angular/core';
import {AppComponent} from './app.component';
import {FrontendService} from "./service/frontend.service";
import {HttpClientModule} from "@angular/common/http";
import {routing} from "./app.routing";
import {HomeComponent} from "./home/home.component";
import {GameComponent} from "./game/game.component";
import {RegisterComponent} from "./register/register.component";
import {GlobalErrorHandler} from "./global.error.fandler";
import {NoCookies} from "./nocookies/nocookies.component";
import {FacebookAppStart} from "./facebookappstart/facebook-app-start.component";
import {EmailVerification} from "./emailverification/email-verification.component";
import {FormsModule} from "@angular/forms";


@NgModule({
  declarations: [
    AppComponent,
    HomeComponent,
    GameComponent,
    RegisterComponent,
    NoCookies,
    FacebookAppStart,
    EmailVerification
  ],
  imports: [
    BrowserModule,
    HttpClientModule,
    FormsModule,
    routing
  ],
  providers: [FrontendService, {
    provide: ErrorHandler,
    useClass: GlobalErrorHandler
  }],
  bootstrap: [AppComponent]
})
export class AppModule {

  constructor(private frontendService: FrontendService) {
    window.onerror = function (msg, url, line, col, error) {
      this.frontendService.log("Gloabl error handler. msg: " + msg + ". url: " + url + ". line: " + line + ". col: " + col + ". error: " + error)
    };
  }
}
