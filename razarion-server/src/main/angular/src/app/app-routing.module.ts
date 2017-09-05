import {NgModule} from "@angular/core";
import {RouterModule, Routes} from "@angular/router";
import {Dashboard} from "./dashboard.component";
import {SessionDetails} from "./tracking/session-detail.component";
import {BackupRestoreComponent} from "./backuprestore/backup-restore.component";
import {UserComponent} from "./user/user.component";

const routes: Routes = [
  {path: 'dashboard', component: Dashboard},
  {path: 'session/:id', component: SessionDetails},
  {path: 'backuprestore', component: BackupRestoreComponent},
  {path: 'user/:id', component: UserComponent},
  {path: '', redirectTo: '/dashboard', pathMatch: 'full'},
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})

export class AppRoutingModule {
}
