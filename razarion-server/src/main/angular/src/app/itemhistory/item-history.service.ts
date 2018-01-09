import {Injectable} from "@angular/core";
import {Common, URL_TRACKING} from "../Common";
import {Http} from "@angular/http";
import {ItemTracking} from "./item-history.dto";

@Injectable()
export class ItemHistoryService {
  constructor(private http: Http) {
  }

  getItemHistory(): Promise<ItemTracking[]> {
    return this.http.get(URL_TRACKING + '/itemhistory').toPromise().then(response => {
      return response.json();
    }).catch(Common.handleError);
  }
}
