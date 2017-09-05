import {Injectable} from "@angular/core";
import {UserBackendInfo} from "./user.dto";
import {Common, URL_SERVER_MGMT} from "../Common";
import {Http} from "@angular/http";

@Injectable()
export class UserService {
  constructor(private http: Http) {
  }

  loadUserBackendInfo(playerId: number): Promise<UserBackendInfo> {
    return this.http.get(URL_SERVER_MGMT + '/loadbackenduserinfo/' + playerId)
      .toPromise()
      .then(response => {
        return response.json();
      })
      .catch(Common.handleError);
  }

  removeCompletedQuest(humanPlayerId: number, questId: number): Promise<UserBackendInfo> {
    return this.http.delete(URL_SERVER_MGMT + '/removecompletedquest/' + humanPlayerId + "/" + questId)
      .toPromise()
      .then(response => {
        return response.json();
      })
      .catch(Common.handleError);
  }
}
