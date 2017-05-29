import {Injectable} from "@angular/core";
import {Http} from "@angular/http";
import {Session, SessionDetail} from "./session-dto";
import "rxjs/add/operator/toPromise";

@Injectable()
export class SessionService {
  // private sessionUrl = '/rest/tracking';

  constructor(private http: Http) {
  }

  getSessions(): Promise<Session[]> {
    return this.http.get('/rest/tracking/sessions')
      .toPromise()
      .then(response => {
        return response.json();
      })
      .catch(SessionService.handleError);
  }

  getSessionDetail(id: string): Promise<SessionDetail> {
    return this.http.get('/rest/tracking/sessiondetail?id=' + id)
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
