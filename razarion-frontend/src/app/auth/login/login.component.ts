import {Component, Input} from '@angular/core';
import {MessageService} from 'primeng/api';
import {UserService} from '../user.service';
import {FormsModule} from '@angular/forms';
import {Button} from 'primeng/button';
import {MainCockpitService} from '../../game/cockpit/main/main-cockpit.service';
import {NgIf} from '@angular/common';

@Component({
  selector: 'login',
  imports: [
    FormsModule,
    Button,
    NgIf
  ],
  templateUrl: './login.component.html'
})
export class LoginComponent {
  @Input("showRegister")
  showRegister = false;

  username: string = "";
  password: string = "";
  message: string = "";

  constructor(private mainCockpitService: MainCockpitService,
              private serviceService: UserService,
              public userService: UserService,
              private messageService: MessageService) {
  }

  public login(): void {
    this.serviceService.login(this.username, this.password)
      .then(() => {
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

  onRegister() {
    this.mainCockpitService.showLoginDialog = false;
    this.mainCockpitService.showRegisterDialog = true;
  }
}
