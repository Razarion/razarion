import {BrowserModule} from "@angular/platform-browser";
import {NgModule} from "@angular/core";
import {FormsModule} from "@angular/forms";
import {HttpModule} from "@angular/http";
import {Dashboard} from "./dashboard.component";
import {SessionTable} from "./session-table.component";
import {SessionService} from "./session.service";
import {InMemoryDataService} from "./in-memory-data.service";
import { InMemoryWebApiModule } from 'angular-in-memory-web-api/in-memory-web-api.module';

@NgModule({
  declarations: [
    Dashboard,
    SessionTable
  ],
  imports: [
    BrowserModule,
    FormsModule,
    HttpModule,
    InMemoryWebApiModule.forRoot(InMemoryDataService, {apiBase: "rest/tracking"})
  ],
  providers: [SessionService],
  bootstrap: [Dashboard]
})
export class Backend {
}
