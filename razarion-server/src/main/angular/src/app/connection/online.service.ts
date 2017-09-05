import {Injectable} from "@angular/core";
import {Http} from "@angular/http";
import {Common, URL_ONLINE} from "../Common";
import {OnlineInfo} from "./online.dto";

@Injectable()
export class OnlineService {
  constructor(private http: Http) {
  }

  loadAllOnlines(): Promise<OnlineInfo[]> {
    return this.http.get(URL_ONLINE + '/loadallonlines')
      .toPromise()
      .then(response => {
        return response.json();
      }).catch(Common.handleError);
  }
}
