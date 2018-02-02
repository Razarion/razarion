import {Component} from '@angular/core';
import {FrontendService} from "../service/frontend.service";
import {ActivatedRoute, Router} from "@angular/router";


@Component({
  templateUrl: 'cahnge-password.component.html'
})
export class ChangePasswordComponent {
  password: string = "Passwort";
  passwordConfirm: string = "Passwort bestätigen";
  passwordError: string = "";
  passwordConfirmError: string = "";
  success: boolean = true;
  failed: boolean = true;
  sent: boolean = true;

  constructor(private frontendService: FrontendService, private route: ActivatedRoute, private router: Router) {
  }


  onKeyPasswordConfirm(passwordConfirm: string) {
    this.passwordConfirmError = "";
    if (this.password != passwordConfirm) {
      this.passwordConfirmError = "Passwörter sind nicht identisch"
    }
  }

  onSave() {
    this.passwordConfirmError = "";
    if (this.password != this.passwordConfirm) {
      this.passwordConfirmError = "Passwörter sind nicht identisch"
    } else {
      let uuid = this.route.snapshot.paramMap.get('id');
      this.frontendService.savePassword(this.password, uuid).then(success => {
        this.success = success;
        this.failed = !success;
        this.sent = true;
      });
    }
  }

  onGame() {
    this.router.navigate(['/game']);
  }
}
