import {Component, OnInit} from '@angular/core';
import {FrontendService} from "../service/frontend.service";
import {ActivatedRoute, Router} from "@angular/router";


@Component({
  templateUrl: 'email-verification.component.html',
  styleUrls: ['./email-verification.component.css']
})
export class EmailVerification implements OnInit {
  success: boolean = false;
  fail: boolean = false;

  constructor(private frontendService: FrontendService, private route: ActivatedRoute, private router: Router) {
  }

  ngOnInit(): void {
    let verificationId = this.route.snapshot.paramMap.get('id');
    if(verificationId == null) {
      verificationId = "Service returned null: razarion-server/src/main/angular/frontend/src/app/emailverification/email-verification.component.ts:20";
    }
    this.frontendService.verifyEmailLink(verificationId).then(success => {
      this.success = success;
      this.fail = !success;
    });
  }

  onPlay() {
    this.router.navigate(['/game']);
  }

}
