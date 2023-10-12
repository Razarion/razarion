import {Component} from '@angular/core';
import {FrontendService} from "../service/frontend.service";
import {ActivatedRoute, Router} from "@angular/router";


@Component({
  templateUrl: 'cahnge-password.component.html',
  styleUrls: ['./change-password.component.css']
})
export class ChangePasswordComponent {
  password: string = "";
  passwordConfirm: string = "";
  passwordError: string = "";
  passwordConfirmError: string = "";
  success: boolean = false;
  failed: boolean = false;

  constructor(private frontendService: FrontendService, private route: ActivatedRoute, private router: Router) {
  }

  onKeyPassword(password: string) {
    this.passwordError = "";
    if (password == "") {
      this.passwordError = "Bitte gib ein gültiges Passwort ein";
    }
  }

  onKeyPasswordConfirm(passwordConfirm: string) {
    this.passwordConfirmError = "";
    if (this.password != passwordConfirm) {
      this.passwordConfirmError = "Passwörter sind nicht identisch"
    }
  }

  onSave() {
    this.passwordError = "";
    this.passwordConfirmError = "";
    if (this.password == null || this.password == "") {
      this.passwordError = "Bitte gib ein gültiges Passwort ein";
    } else if (this.password != this.passwordConfirm) {
      this.passwordConfirmError = "Passwörter sind nicht identisch"
    } else {
      let uuid = this.route.snapshot.paramMap.get('id');
      if(uuid == null) {
        throw new Error("uuid == null");
      }
      this.frontendService.savePassword(this.password, uuid).then(success => {
        this.success = success;
        this.failed = !success;
      });
    }
  }

  onGame() {
    this.router.navigate(['/game']);
  }
}
