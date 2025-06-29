import {Component} from '@angular/core';
import {MessageService} from 'primeng/api';
import {AuthService} from '../auth.service';
import {FormsModule} from '@angular/forms';
import {Button} from 'primeng/button';
import {Router} from '@angular/router';

@Component({
  selector: 'login',
  imports: [
    FormsModule,
    Button
  ],
  templateUrl: './login.component.html'
})
export class LoginComponent {

  username: string = "";
  password: string = "";
  message: string = "";

  constructor(private authService: AuthService,
              private messageService: MessageService) {
  }

  public login(): void {
    this.authService.login(this.username, this.password)
      .then((token) => {
        localStorage.setItem("app.token", token);
        window.location.replace("/game");
      })
      .catch((error) => {
        this.messageService.add({
          severity: 'error',
          summary: `Login failed: ${error.status}`,
          sticky: true
        });
      })
  }

}
