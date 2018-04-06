import {Injectable} from "@angular/core";
import {Common, URL_BACKEND_PROVIDER} from "../common";
import {ItemTracking, ItemTrackingDescription, ItemTrackingSearch} from "./item-history.dto";
import {Headers, Http} from '@angular/http';

@Injectable()
export class ItemHistoryService {
  private itemTrackingDescription: ItemTrackingDescription;
  private queuedItemTrackingSearch: ItemTrackingSearch;

  constructor(private http: Http) {
  }

  getItemHistory(itemTrackingSearch: ItemTrackingSearch): Promise<ItemTracking[]> {
    if (this.itemTrackingDescription == null) {
      this.queuedItemTrackingSearch = itemTrackingSearch;
      return this.loadItemTrackingDescription();
    } else {
      return this.getItemHistoryPrivate(itemTrackingSearch);
    }
  }

  private getItemHistoryPrivate(itemTrackingSearch: ItemTrackingSearch): Promise<ItemTracking[]> {
    return this.http.post(URL_BACKEND_PROVIDER + '/itemhistory', JSON.stringify(itemTrackingSearch), {headers: new Headers({'Content-Type': 'application/json'})})
      .toPromise()
      .then(response => {
        this.queuedItemTrackingSearch = null;
        return response.json();
      })
      .catch(Common.handleError);
  }

  private loadItemTrackingDescription(): Promise<ItemTracking[]> {
    return this.http.get(URL_BACKEND_PROVIDER + '/itemhistorydescription')
      .toPromise()
      .then(response => {
        this.itemTrackingDescription = response.json();
        if (this.queuedItemTrackingSearch != null) {
          return this.getItemHistoryPrivate(this.queuedItemTrackingSearch);
        }
      })
      .catch(Common.handleError);
  }

  name4BaseItemTypeId(baseItemTypeId: number): string {
    if (this.itemTrackingDescription != null) {
      return this.itemTrackingDescription.baseItemTypeNames[baseItemTypeId];
    } else {
      return "not loaded";
    }
  }

  name4ResourceItemTypeId(resourceItemTypeId: number): string {
    if (this.itemTrackingDescription != null) {
      return this.itemTrackingDescription.resourceItemTypeNames[resourceItemTypeId];
    } else {
      return "not loaded";
    }
  }

  name4BoxItemTypeId(boxItemTypeId: number): string {
    if (this.itemTrackingDescription != null) {
      return this.itemTrackingDescription.boxItemTypeNames[boxItemTypeId];
    } else {
      return "not loaded";
    }
  }

  name4HumanPlayerId(humanPlayerId: number) {
    if (this.itemTrackingDescription != null) {
      return this.itemTrackingDescription.humanPlayerIdNames[humanPlayerId];
    } else {
      return "not loaded";
    }
  }

  name4BotId(botId: number) {
    if (this.itemTrackingDescription != null) {
      return this.itemTrackingDescription.botNames[botId];
    } else {
      return "not loaded";
    }
  }
}
