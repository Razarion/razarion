import {Component} from '@angular/core';
import {UserService} from '../user.service';
import {Button} from 'primeng/button';
import {Router} from '@angular/router';

@Component({
  selector: 'user',
  imports: [Button],
  templateUrl: './user.component.html'
})
export class UserComponent {
  constructor(private userService: UserService, private router: Router) {
  }

  logout() {
    this.userService.logout();
    this.router.navigate(['invalid-token']);
  }

  getUserName(): string {
    return this.userService.getUserName();
  }
}
