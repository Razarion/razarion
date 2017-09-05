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
import {BackupRestoreComponent} from "./backuprestore/backup-restore.component";
import {BackupRestoreService} from "./backuprestore/backup-restore.service";
import {OnlineService} from "./connection/online.service";
import {OnlineComponent} from "./connection/online.component";
import {DurationPipe} from "./duration.pipe";
import {UserComponent} from "./user/user.component";
import {UserService} from "./user/user.service";

@NgModule({
  declarations: [
    MainWindow,
    Dashboard,
    SessionHistory,
    SessionDetails,
    BackupRestoreComponent,
    OnlineComponent,
    DurationPipe,
    UserComponent
  ],
  imports: [
    BrowserModule,
    FormsModule,
    HttpModule,
    AppRoutingModule,
    // InMemoryWebApiModule.forRoot(InMemoryDataService, {apiBase: "rest/servermgmtprovider"})
  ],
  providers: [SessionService, BackupRestoreService, OnlineService, UserService],
  bootstrap: [MainWindow]
})
export class Backend {
}
