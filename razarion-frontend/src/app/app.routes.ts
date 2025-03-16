import {Routes} from '@angular/router';
import {GameComponent} from './game/game.component';

export const routes: Routes = [
  {path: '', component: GameComponent},
  {path: '**', redirectTo: ''}
];
