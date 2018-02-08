import {Injectable} from "@angular/core";
import {Http} from "@angular/http";
import "rxjs/add/operator/toPromise";
import {Common, URL_BACKEND_PROVIDER} from "../common";
import {UserHistoryEntry} from "./user-login-history.dto";

@Injectable()
export class UserHistoryService {
  constructor(private http: Http) {
  }

  getUserHistory(): Promise<UserHistoryEntry[]> {
    return this.http.get(URL_BACKEND_PROVIDER + '/userhistory').toPromise().then(response => {
      return response.json();
    }).catch(Common.handleError);
  }
}
