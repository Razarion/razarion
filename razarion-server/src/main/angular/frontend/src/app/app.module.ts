import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';


import {AppComponent} from './app.component';
import {FrontendService} from "./service/frontend.service";
import {HttpClientModule} from "@angular/common/http";
import {routing} from "./app.routing";
import {HomeComponent} from "./home/home.component";
import {GameComponent} from "./game/game.component";
import {RegisterComponent} from "./register/register.component";


@NgModule({
  declarations: [
    AppComponent,
    HomeComponent,
    GameComponent,
    RegisterComponent
  ],
  imports: [
    BrowserModule,
    HttpClientModule,
    routing
  ],
  providers: [FrontendService],
  bootstrap: [AppComponent]
})
export class AppModule {
}
