import {Routes} from '@angular/router';
import {GameComponent} from './game/game.component';
import {InvalidTokenComponent} from './auth/invalid-token/invalid-token.component';
import {VerifyEmailComponent} from './auth/verify-email/verify-email.component';
import {BackendComponent} from './backend/backend.component';

export const routes: Routes = [
  {path: '', component: GameComponent},
  {path: 'invalid-token', component: InvalidTokenComponent},
  {path: 'verify-email/:id', component: VerifyEmailComponent},
  {path: 'backend', component: BackendComponent},
  {path: '**', redirectTo: ''}
];
