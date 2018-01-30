import {RouterModule, Routes} from '@angular/router';
import {HomeComponent} from "./home/home.component";
import {GameComponent} from "./game/game.component";
import {RegisterComponent} from "./register/register.component";
import {NoCookies} from "./nocookies/nocookies.component";
import {FacebookAppStart} from "./facebookappstart/facebook-app-start.component";


const appRoutes: Routes = [
  {path: '', component: HomeComponent},
  {path: 'game', component: GameComponent},
  {path: 'register', component: RegisterComponent},
  {path: 'nocookies', component: NoCookies},
  {path: 'facebookappstart', component: FacebookAppStart},
  {path: '**', redirectTo: ''}
];

export const routing = RouterModule.forRoot(appRoutes);
