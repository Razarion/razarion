import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {Common, URL_FRONTEND} from "../common";

declare var RAZ_fbScriptLoadedFrontendService: any;
declare var RAZ_fbScriptLoadedFlag: boolean;
declare var RAZ_fbScriptLoadedCallback: any;

declare const FB: any;


// TODO set timer for facebook fail


@Injectable()
export class FrontendService {

  constructor(private http: HttpClient) {
  }

  start(): Promise<boolean> {
    return new Promise((resolve, reject) => {
      this.http.get<boolean>(URL_FRONTEND + '/isloggedin').toPromise().then(value => {
        try {
          if (value) {
            resolve(value);
          } else {
            if (RAZ_fbScriptLoadedFlag) {
              this.checkFbLoginState();
            } else {
              RAZ_fbScriptLoadedFrontendService = this;
              RAZ_fbScriptLoadedCallback = FrontendService.onFbScriptLoaded;
            }
          }
        } catch (err) {
          Common.handleError(err);
        }
      }).catch(err => {
        Common.handleError(err);
      });
    });
  }

  checkFbLoginState() {
    FB.getLoginStatus(response => {
      if (response.status === 'connected') {
        let body = {};
        this.http.post(URL_FRONTEND + '/facebookauthenticated', body).subscribe(
          data => {
          },
          error => {
            Common.handleError(error);
          });
        var uid = response.authResponse.userID;
        var accessToken = response.authResponse.accessToken;

        // the user is logged in and has authenticated your
        // app, and response.authResponse supplies
        // the user's ID, a valid access token, a signed
        // request, and the time the access token
        // and signed request each expire
      } else if (response.status === 'not_authorized') {
        this.http.post(URL_FRONTEND + '/facebooknotauthorized', {}).subscribe(
          data => {
          },
          error => {
            Common.handleError(error);
          });
      } else {
        this.http.post(URL_FRONTEND + '/nofacebookuser', {}).subscribe(
          data => {
          },
          error => {
            Common.handleError(error);
          });
      }
    });
  }

  static onFbScriptLoaded(frontendService: FrontendService) {
    frontendService.checkFbLoginState();
  }
}
