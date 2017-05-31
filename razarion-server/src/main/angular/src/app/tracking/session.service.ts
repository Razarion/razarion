import {Injectable} from "@angular/core";
import {Headers, Http} from "@angular/http";
import {SearchConfig, Session, SessionDetail} from "./session-dto";
import "rxjs/add/operator/toPromise";

@Injectable()
export class SessionService {
  private sessionUrl = '/rest/trackerbackend';

  constructor(private http: Http) {
  }

  getSessions(searchConfig: SearchConfig): Promise<Session[]> {

    return this.http.post(this.sessionUrl + '/sessions', JSON.stringify(searchConfig), {headers: new Headers({'Content-Type': 'application/json'})})
      .toPromise()
      .then(response => {
        return response.json();
      })
      .catch(SessionService.handleError);
  }

  getSessionDetail(id: string): Promise<SessionDetail> {
    return this.http.get(this.sessionUrl + '/sessiondetail/' + id)
      .toPromise()
      .then(response => {
        return response.json();
      })
      .catch(SessionService.handleError);
  }

  private static handleError(error: any): Promise<any> {
    console.error('An error occurred', error);
    return Promise.reject(error.message || error);
  }
}
