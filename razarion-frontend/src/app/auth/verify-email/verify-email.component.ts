import {Component, OnInit} from '@angular/core';
import {ActivatedRoute} from "@angular/router";
import {LoginComponent} from "../login/login.component";
import {UserControllerClient} from '../../generated/razarion-share';
import {HttpClient} from '@angular/common/http';
import {TypescriptGenerator} from '../../backend/typescript-generator';


@Component({
  selector: 'verify-email',
  imports: [
    LoginComponent
],
  templateUrl: './verify-email.component.html'
})
export class VerifyEmailComponent implements OnInit {
  private userControllerClient: UserControllerClient;
  successful = false;
  failed = false;

  constructor(httpClient: HttpClient, private route: ActivatedRoute) {
    this.userControllerClient = new UserControllerClient(TypescriptGenerator.generateHttpClientAdapter(httpClient))
  }

  ngOnInit(): void {
    let verificationId = this.route.snapshot.paramMap.get('id');
    if (verificationId) {
      this.userControllerClient.verifyEmailVerificationId(verificationId).then(value => {
        if (value) {
          this.successful = true;
        } else {
          this.failed = true;
        }
      }).catch(e => {
        this.failed = true;
      })
    }
  }

}
