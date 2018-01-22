import {Injectable} from "@angular/core";
import {Http} from "@angular/http";
import {URL_SERVER_MGMT} from "../Common";
import {OnlineInfo} from "./online.dto";

@Injectable()
export class OnlineService {
  constructor(private http: Http) {
  }

  loadAllOnlines(): Promise<OnlineInfo[]> {
    return this.http.get(URL_SERVER_MGMT + '/loadallonlines')
      .toPromise()
      .then(response => {
        return response.json();
      }).catch(reason => {
        throw reason;
      });
  }
}
