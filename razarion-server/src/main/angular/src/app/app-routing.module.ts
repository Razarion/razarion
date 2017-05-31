import {NgModule} from "@angular/core";
import {RouterModule, Routes} from "@angular/router";
import {Dashboard} from "./dashboard.component";
import {SessionDetails} from "./tracking/session-detail.component";

const routes: Routes = [
  {path: 'dashboard', component: Dashboard},
  {path: 'session/:id', component: SessionDetails },
  {path: '', redirectTo: '/dashboard', pathMatch: 'full'},
  // { path: 'heroes',     component: HeroesComponent }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})

export class AppRoutingModule {
}
