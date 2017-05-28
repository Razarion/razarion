import {BrowserModule} from "@angular/platform-browser";
import {NgModule} from "@angular/core";
import {FormsModule} from "@angular/forms";
import {HttpModule} from "@angular/http";
import {Dashboard} from "./dashboard.component";
import {InMemoryDataService} from "./in-memory-data.service";
import { InMemoryWebApiModule } from 'angular-in-memory-web-api/in-memory-web-api.module';
import {MainWindow} from "./main-window.component";
import {AppRoutingModule} from "./app-routing.module";
import {SessionHistory} from "./tracking/session-history.component";
import {SessionService} from "./tracking/session.service";
import {SessionDetails} from "./tracking/session-detail.component";

@NgModule({
  declarations: [
    MainWindow,
    Dashboard,
    SessionHistory,
    SessionDetails
  ],
  imports: [
    BrowserModule,
    FormsModule,
    HttpModule,
    AppRoutingModule,
    InMemoryWebApiModule.forRoot(InMemoryDataService, {apiBase: "rest/tracking"})
  ],
  providers: [SessionService],
  bootstrap: [MainWindow]
})
export class Backend {
}
