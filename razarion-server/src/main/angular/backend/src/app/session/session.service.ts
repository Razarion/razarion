import {Injectable} from "@angular/core";
import {Headers, Http} from "@angular/http";
import {SearchConfig, SessionTracker, SessionDetail} from "./session-dto";
import "rxjs/add/operator/toPromise";
import {Common, URL_BACKEND_PROVIDER} from "../common";

@Injectable()
export class SessionService {
  constructor(private http: Http) {
  }

  getSessions(searchConfig: SearchConfig): Promise<SessionTracker[]> {

    return this.http.post(URL_BACKEND_PROVIDER + '/sessions', JSON.stringify(searchConfig), {headers: new Headers({'Content-Type': 'application/json'})})
      .toPromise()
      .then(response => {
        return response.json();
      })
      .catch(Common.handleError);
  }

  getSessionDetail(id: string): Promise<SessionDetail> {
    return this.http.get(URL_BACKEND_PROVIDER + '/sessiondetail/' + id)
      .toPromise()
      .then(response => {
        return response.json();
      })
      .catch(Common.handleError);
  }
}
