import {Component, OnInit} from "@angular/core";
import {SessionService} from "./session.service";
import {Params, ActivatedRoute} from "@angular/router";
import "rxjs/add/operator/switchMap";
import "rxjs/add/operator/toPromise";
import {SessionDetail} from "./session-dto";

@Component({
  selector: 'session-detail',
  templateUrl: './session-detail.component.html',
  // styleUrls: ['./session-history.component.css']
})

export class SessionDetails implements OnInit {
  sessionDetail: SessionDetail = new SessionDetail();
  // private gameSessionDetails: GameSessionDetail[];

  constructor(private sessionService: SessionService, private route: ActivatedRoute) {
  }

  ngOnInit(): void {
    this.route.params.switchMap((params: Params) => this.sessionService.getSessionDetail(params['id'])).subscribe(sessionDetail => this.sessionDetail = sessionDetail);
  }

}
