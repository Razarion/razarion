import {Injectable} from "@angular/core";
import {Headers, Http} from "@angular/http";
import {BackupPlanetOverview} from "./backup-restore.dto";
import {Common, URL_PLANET_MGMT} from "../common";

@Injectable()
export class BackupRestoreService {
  constructor(private http: Http) {
  }

  loadAllBackupBaseOverviews(): Promise<BackupPlanetOverview[]> {
    return this.http.get(URL_PLANET_MGMT + '/loadallbackupbaseoverviews')
      .toPromise()
      .then(response => {
        return response.json();
      }).catch(Common.handleError);
  }

  backup(): Promise<BackupPlanetOverview[]> {
    return this.http.post(URL_PLANET_MGMT + '/dobackup', null)
      .toPromise()
      .then(response => {
        return response.json();
      }).catch(Common.handleError);
  }

  restore(backupBaseOverview: BackupPlanetOverview) {
    this.http.post(URL_PLANET_MGMT + '/dorestore', JSON.stringify(backupBaseOverview), {headers: new Headers({'Content-Type': 'application/json'})}).toPromise().catch(Common.handleError);
  }

  doDelete(backupBaseOverview: BackupPlanetOverview): Promise<BackupPlanetOverview[]> {
    return this.http.delete(URL_PLANET_MGMT + '/deletebackup/' + backupBaseOverview.planetId + "/" + JSON.stringify(backupBaseOverview.date), {headers: new Headers({'Content-Type': 'application/json'})}).toPromise().then(response => {
      return response.json();
    }).catch(Common.handleError);
  }
}
