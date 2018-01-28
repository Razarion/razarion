import {RouterModule, Routes} from '@angular/router';
import {HomeComponent} from "./home/home.component";
import {GameComponent} from "./game/game.component";
import {RegisterComponent} from "./register/register.component";


const appRoutes: Routes = [
  {path: '', component: HomeComponent},
  {path: 'game', component: GameComponent},
  {path: 'register', component: RegisterComponent},

  // otherwise redirect to home
  {path: '**', redirectTo: ''}
];

export const routing = RouterModule.forRoot(appRoutes);
