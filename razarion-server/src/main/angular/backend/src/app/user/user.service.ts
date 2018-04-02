import {Injectable} from "@angular/core";
import {UnlockedBackendInfo, UserBackendInfo} from "./user.dto";
import {Common, URL_BACKEND_PROVIDER} from "../common";
import {Headers, Http} from "@angular/http";

@Injectable()
export class UserService {
  constructor(private http: Http) {
  }

  loadUserBackendInfo(playerId: number): Promise<UserBackendInfo> {
    return this.http.get(URL_BACKEND_PROVIDER + '/loadbackenduserinfo/' + playerId)
      .toPromise()
      .then(response => {
        return response.json();
      })
      .catch(Common.handleError);
  }

  removeCompletedQuest(humanPlayerId: number, questId: number): Promise<UserBackendInfo> {
    return this.http.delete(URL_BACKEND_PROVIDER + '/removecompletedquest/' + humanPlayerId + "/" + questId)
      .toPromise()
      .then(response => {
        return response.json();
      })
      .catch(Common.handleError);
  }

  addCompletedQuest(humanPlayerId: number, questId: number): Promise<UserBackendInfo> {
    let urlSearchParams = new URLSearchParams();
    urlSearchParams.append('playerId', humanPlayerId.toString());
    urlSearchParams.append('questId', questId.toString());
    return this.http.post(URL_BACKEND_PROVIDER + '/addcompletedquest', urlSearchParams.toString(), {headers: new Headers({'Content-Type': 'application/x-www-form-urlencoded'})})
      .toPromise()
      .then(response => {
        return response.json();
      })
      .catch(Common.handleError);
  }

  setLevelNumber(humanPlayerId: number, levelNumber: number): Promise<UserBackendInfo> {
    let urlSearchParams = new URLSearchParams();
    urlSearchParams.append('playerId', humanPlayerId.toString());
    urlSearchParams.append('levelNumber', levelNumber.toString());
    return this.http.post(URL_BACKEND_PROVIDER + '/setlevelnumber', urlSearchParams.toString(), {headers: new Headers({'Content-Type': 'application/x-www-form-urlencoded'})})
      .toPromise()
      .then(response => {
        return response.json();
      })
      .catch(Common.handleError);
  }

  setXp(humanPlayerId: number, xp: number) {
    let urlSearchParams = new URLSearchParams();
    urlSearchParams.append('playerId', humanPlayerId.toString());
    urlSearchParams.append('xp', xp.toString());
    return this.http.post(URL_BACKEND_PROVIDER + '/setxp', urlSearchParams.toString(), {headers: new Headers({'Content-Type': 'application/x-www-form-urlencoded'})})
      .toPromise()
      .then(response => {
        return response.json();
      })
      .catch(Common.handleError);
  }

  setCrystals(humanPlayerId: number, crystals: number) {
    let urlSearchParams = new URLSearchParams();
    urlSearchParams.append('playerId', humanPlayerId.toString());
    urlSearchParams.append('crystals', crystals.toString());
    return this.http.post(URL_BACKEND_PROVIDER + '/setcrystals', urlSearchParams.toString(), {headers: new Headers({'Content-Type': 'application/x-www-form-urlencoded'})})
      .toPromise()
      .then(response => {
        return response.json();
      })
      .catch(Common.handleError);
  }

  removeUnlocked(humanPlayerId: number, unlockedBackendInfo: UnlockedBackendInfo) {
    return this.http.delete(URL_BACKEND_PROVIDER + '/removeunlocked/' + humanPlayerId + "/" + unlockedBackendInfo.id)
      .toPromise()
      .then(response => {
        return response.json();
      })
      .catch(Common.handleError);
  }
}
