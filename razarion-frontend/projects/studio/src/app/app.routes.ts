import {Routes} from '@angular/router';
import {ShellComponent} from './shell/shell.component';
import {ThumbnailsTaskComponent} from './tasks/thumbnails-task.component';
import {SceneComposerTaskComponent} from './tasks/scene-composer-task.component';

export const routes: Routes = [
  {
    path: '',
    component: ShellComponent,
    children: [
      {path: 'thumbnails', component: ThumbnailsTaskComponent},
      {path: 'scenes', component: SceneComposerTaskComponent},
      {path: '', redirectTo: 'thumbnails', pathMatch: 'full'}
    ]
  },
  {path: '**', redirectTo: ''}
];
