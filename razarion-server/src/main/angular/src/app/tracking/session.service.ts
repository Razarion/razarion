import {Injectable} from "@angular/core";
import {Headers, Http} from "@angular/http";
import {SearchConfig, SessionTracker, SessionDetail} from "./session-dto";
import "rxjs/add/operator/toPromise";
import {Common, URL_TRACKING} from "../Common";

@Injectable()
export class SessionService {
  constructor(private http: Http) {
  }

  getSessions(searchConfig: SearchConfig): Promise<SessionTracker[]> {

    return this.http.post(URL_TRACKING + '/sessions', JSON.stringify(searchConfig), {headers: new Headers({'Content-Type': 'application/json'})})
      .toPromise()
      .then(response => {
        return response.json();
      })
      .catch(Common.handleError);
  }

  getSessionDetail(id: string): Promise<SessionDetail> {
    return this.http.get(URL_TRACKING + '/sessiondetail/' + id)
      .toPromise()
      .then(response => {
        return response.json();
      })
      .catch(Common.handleError);
  }
}
