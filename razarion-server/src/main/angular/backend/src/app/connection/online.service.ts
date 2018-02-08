import {Injectable} from "@angular/core";
import {Http} from "@angular/http";
import {URL_BACKEND_PROVIDER} from "../common";
import {OnlineInfo} from "./online.dto";

@Injectable()
export class OnlineService {
  constructor(private http: Http) {
  }

  loadAllOnlines(): Promise<OnlineInfo[]> {
    return this.http.get(URL_BACKEND_PROVIDER + '/loadallonlines')
      .toPromise()
      .then(response => {
        return response.json();
      }).catch(reason => {
        throw reason;
      });
  }
}
