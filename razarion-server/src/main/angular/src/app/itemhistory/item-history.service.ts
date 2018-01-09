import {Injectable} from "@angular/core";
import {Common, URL_TRACKING} from "../Common";
import {ItemTracking, ItemTrackingSearch} from "./item-history.dto";
import { Http, Headers } from '@angular/http';
@Injectable()
export class ItemHistoryService {
  constructor(private http: Http) {
  }

  getItemHistory(itemTrackingSearch: ItemTrackingSearch): Promise<ItemTracking[]> {
    return this.http.post(URL_TRACKING + '/itemhistory', JSON.stringify(itemTrackingSearch), {headers: new Headers({'Content-Type': 'application/json'})})
      .toPromise()
      .then(response => {
        return response.json();
      })
      .catch(Common.handleError);
  }

}
