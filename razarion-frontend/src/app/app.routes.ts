import {Routes} from '@angular/router';
import {GameComponent} from './game/game.component';
import {InvalidTokenComponent} from './auth/invalid-token/invalid-token.component';
import {VerifyEmailComponent} from './auth/verify-email/verify-email.component';

export const routes: Routes = [
  {path: '', component: GameComponent},
  {path: 'invalid-token', component: InvalidTokenComponent},
  {path: 'verify-email/:id', component: VerifyEmailComponent},
  {path: 'backend', loadComponent: () => import('./backend/backend.component').then(m => m.BackendComponent)},
  {path: '**', redirectTo: ''}
];
