import {Injectable} from "@angular/core";
import {Http} from "@angular/http";
import {Session} from "./session";
import "rxjs/add/operator/toPromise";

@Injectable()
export class SessionService {
  private sessionUrl = '/rest/tracking';

  constructor(private http: Http) {
  }

  getSessions(): Promise<Session[]> {
    return this.http.get('/rest/tracking/sessions')
      .toPromise()
      .then(response => {
        return response.json().data; // TODO remove data
      })
      .catch(this.handleError);
  }

  private handleError(error: any): Promise<any> {
    console.error('An error occurred', error);
    return Promise.reject(error.message || error);
  }
}
