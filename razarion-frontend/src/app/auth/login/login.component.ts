import {Component, Input} from '@angular/core';
import {MessageService} from 'primeng/api';
import {UserService} from '../user.service';
import {FormsModule} from '@angular/forms';
import {Button} from 'primeng/button';
import {CockpitDisplayService} from '../../game/cockpit/cockpit-display.service';
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
  @Input("successUrl")
  successUrl: string | null = null;

  username: string = "";
  password: string = "";
  message: string = "";

  constructor(private mainCockpitService: CockpitDisplayService,
              public userService: UserService,
              private messageService: MessageService) {
  }

  public login(): void {
    this.userService.login(this.username, this.password)
      .then(() => {
        if (this.successUrl) {
          window.location.replace(this.successUrl);
        } else {
          window.location.replace("/game");
        }
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
