import {Component} from '@angular/core';
import {AuthService} from '../auth.service';
import {Button} from 'primeng/button';

@Component({
  selector: 'user',
  imports: [Button],
  templateUrl: './user.component.html'
})
export class UserComponent {
  constructor(private authService: AuthService) {
  }

  logout() {
    this.authService.logout();
    window.location.reload();
  }

  getUserName(): string {
    return this.authService.getUserName();
  }
}
