import {Routes} from '@angular/router';
import {InvalidTokenComponent} from './auth/invalid-token/invalid-token.component';
import {VerifyEmailComponent} from './auth/verify-email/verify-email.component';

export const routes: Routes = [
  // Loaded rather than imported, because a static import puts everything the game reaches into
  // the bundle that has to arrive before Angular can start - 2.4 MB, of which 1.5 MB is Babylon.
  // Nothing on the way to the first frame needs the renderer: the engine module is already
  // downloading (see wasm-boot) and spends about five seconds initialising its worker, which is
  // more than enough time for this chunk to arrive beside it.
  {path: '', loadComponent: () => import('./game/game.component').then(m => m.GameComponent)},
  {
    path: 'director',
    loadComponent: () => import('./game/game.component').then(m => m.GameComponent),
    data: {director: true}
  },
  {path: 'invalid-token', component: InvalidTokenComponent},
  {path: 'verify-email/:id', component: VerifyEmailComponent},
  {path: 'backend', loadComponent: () => import('./backend/backend.component').then(m => m.BackendComponent)},
  {path: '**', redirectTo: ''}
];
