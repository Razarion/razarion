import {Component} from '@angular/core';
import {MessageService} from 'primeng/api';
import {AuthService} from '../auth.service';
import {FormsModule} from '@angular/forms';

@Component({
  selector: 'login',
  imports: [
    FormsModule
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
    localStorage.removeItem("app.token");

    this.authService.login(this.username, this.password)
      .then((token) => {
        localStorage.setItem("app.token", token);
        // const decodedToken = jwtDecode<JwtPayload>(token);
        // sessionStorage.setItem("app.roles", decodedToken.scope);
        window.location.reload();
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
