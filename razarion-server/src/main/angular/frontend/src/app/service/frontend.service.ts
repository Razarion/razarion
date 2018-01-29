import {Injectable} from "@angular/core";
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {FbAuthResponse, FrontendLoginState, URL_FRONTEND} from "../common";

declare var RAZ_fbScriptLoadedFrontendService: any;
declare var RAZ_fbScriptLoadedFlag: boolean;
declare var RAZ_fbScriptLoadedCallback: any;

declare const FB: any;
const FB_TIMEOUT: number = 5000;

@Injectable()
export class FrontendService {
  private language: string;
  private resolve: any;
  private fbTimerId: number;

  constructor(private http: HttpClient) {
  }

  start(): Promise<boolean> {
    return new Promise((resolve, reject) => {
      this.resolve = resolve;
      this.http.get<FrontendLoginState>(URL_FRONTEND + '/isloggedin').toPromise().then(loginState => {
        try {
          this.language = loginState.language;
          if (loginState.loggedIn) {
            resolve(true);
          } else {
            // TODO remember me goes here
            this.fbTimerId = window.setTimeout(() => this.onFbTimeout(), FB_TIMEOUT);
            if (RAZ_fbScriptLoadedFlag) {
              this.checkFbLoginState();
            } else {
              RAZ_fbScriptLoadedFrontendService = this;
              RAZ_fbScriptLoadedCallback = FrontendService.onFbScriptLoaded;
            }
          }
        } catch (err) {
          this.log(err);
          resolve(false);
        }
      }).catch(err => {
        this.log(err);
        resolve(false);
      });
    });
  }

  log(message: any): void {
    this.http.post<FrontendLoginState>(URL_FRONTEND + '/log', JSON.stringify(message), {headers: new HttpHeaders().set('Content-Type', 'text/plain')}).subscribe(value => {
    });
  }

  getLanguage(): string {
    return this.language;
  }

  checkFbLoginState() {
    FB.getLoginStatus(fbResponse => {
      if (this.fbTimerId != null) {
        window.clearInterval(this.fbTimerId);
        this.fbTimerId = null;
      }
      if (fbResponse.status === 'connected') {
        // the user is logged in and has authenticated your app
        let fbAuthResponse: FbAuthResponse = new FbAuthResponse();
        fbAuthResponse.accessToken = fbResponse.authResponse.accessToken;
        fbAuthResponse.expiresIn = fbResponse.authResponse.expiresIn;
        fbAuthResponse.signedRequest = fbResponse.authResponse.signedRequest;
        fbAuthResponse.userID = fbResponse.authResponse.userID;
        this.http.post<boolean>(URL_FRONTEND + '/facebookauthenticated', fbAuthResponse, {headers: new HttpHeaders().set('Content-Type', 'application/json')}).subscribe(
          loggedIn => {
            this.resolve(loggedIn);
          },
          error => {
            this.log(error);
            this.resolve(false);
          });
      } else if (fbResponse.status === 'not_authorized') {
        this.resolve(false);
        // this.http.post(URL_FRONTEND + '/facebooknotauthorized', {}).subscribe(
        //   data => {
        //   },
        //   error => {
        //     Common.handleError(error);
        //   });
      } else {
        this.resolve(false);
        // this.http.post(URL_FRONTEND + '/nofacebookuser', {}).subscribe(
        //   data => {
        //   },
        //   error => {
        //     Common.handleError(error);
        //   });
      }
    });
  }

  static onFbScriptLoaded(frontendService: FrontendService) {
    frontendService.checkFbLoginState();
  }

  private onFbTimeout() {
    this.log("Facebook timed out");
    this.resolve(false);
  }
}
