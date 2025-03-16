import {Component} from '@angular/core';
import {Toast} from 'primeng/toast';
import {RouterOutlet} from '@angular/router';
import {MessageService} from 'primeng/api';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  imports: [
    Toast,
    RouterOutlet
  ],
  providers: [MessageService]
})
export class AppComponent {
}
