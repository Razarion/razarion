import {Component, OnInit} from '@angular/core';
import {ActivatedRoute} from "@angular/router";

@Component({
    selector: 'verify-email',
    imports: [],
    templateUrl: './verify-email.component.html'
})
export class VerifyEmailComponent implements OnInit {
    constructor(private route: ActivatedRoute) {
    }

    ngOnInit(): void {
        let verificationId = this.route.snapshot.paramMap.get('id');
        console.log(verificationId);
    }

}
