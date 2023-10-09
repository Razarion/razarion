import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {HomeComponent} from "./home/home.component";
import {GameComponent} from "./game/game.component";
import {RegisterComponent} from "./register/register.component";
import {NoCookies} from "./nocookies/nocookies.component";
import {FacebookAppStart} from "./facebookappstart/facebook-app-start.component";
import {EmailVerification} from "./emailverification/email-verification.component";
import {LogoutComponent} from "./logout/logout.component";
import {ResetPasswordComponent} from "./resetpassword/reset-password.component";
import {ChangePasswordComponent} from "./resetpassword/change-password.component";
import {TerrainEditor2dComponent} from "./terrain-editor2d/terrain-editor2d.component";

const routes: Routes = [
  {path: '', component: HomeComponent},
  {path: 'game', component: GameComponent},
  {path: 'terrain-editor', component: TerrainEditor2dComponent},
  {path: 'register', component: RegisterComponent},
  {path: 'nocookies', component: NoCookies},
  {path: 'facebookappstart', component: FacebookAppStart},
  {path: 'verify-email/:id', component: EmailVerification},
  {path: 'reset-password', component: ResetPasswordComponent},
  {path: 'change-password/:id', component: ChangePasswordComponent},
  {path: 'logout', component: LogoutComponent},
  {path: '**', redirectTo: ''}
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
