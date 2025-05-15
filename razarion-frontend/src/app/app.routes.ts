import {Routes} from '@angular/router';
import {GameComponent} from './game/game.component';
import {InvalidTokenComponent} from './auth/invalid-token/invalid-token.component';

export const routes: Routes = [
  {path: '', component: GameComponent},
  {path: 'invalid-token', component: InvalidTokenComponent},
  {path: '**', redirectTo: ''}
];
