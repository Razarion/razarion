import {Component} from '@angular/core';
import {AuthService} from '../auth.service';
import {Button} from 'primeng/button';
import {Router} from '@angular/router';

@Component({
  selector: 'user',
  imports: [Button],
  templateUrl: './user.component.html'
})
export class UserComponent {
  constructor(private authService: AuthService, private router: Router) {
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['invalid-token']);
  }

  getUserName(): string {
    return this.authService.getUserName();
  }
}
