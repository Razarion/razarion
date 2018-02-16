import {Injectable} from "@angular/core";
import {Headers, Http} from "@angular/http";
import {URL_BACKEND_PROVIDER} from "../common";

@Injectable()
export class ServerMgmtService {
  constructor(private http: Http) {
  }

  sendRestartLifecycle() {
    this.http.post(URL_BACKEND_PROVIDER + '/sendrestartlifecycle', {}).subscribe();
  }
}
