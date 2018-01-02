import {Injectable} from "@angular/core";
import {Http} from "@angular/http";
import "rxjs/add/operator/toPromise";
import {Common, URL_TRACKING} from "../Common";
import {NewUser} from "./new-user.dto";

@Injectable()
export class NewUserService {
  constructor(private http: Http) {
  }

  getNewUsers(): Promise<NewUser[]> {
    return this.http.get(URL_TRACKING + '/newusers').toPromise().then(response => {
      return response.json();
    }).catch(Common.handleError);
  }
}
