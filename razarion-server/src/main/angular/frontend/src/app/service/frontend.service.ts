import {Injectable} from "@angular/core";
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {FbAuthResponse, FrontendLoginState, URL_FRONTEND} from "../common";

declare var RAZ_fbScriptLoadedFrontendService: any;
declare var RAZ_fbScriptLoadedFlag: boolean;
declare var RAZ_fbScriptLoadedCallback: any;

declare const FB: any;
const FB_TIMEOUT: number = 8000;

@Injectable()
export class FrontendService {
  private language: string;
  private resolve: any;
  private fbTimerId: number;
  private loggedIn: boolean = null;
  private cookieAllowed: boolean = null;

  constructor(private http: HttpClient) {
    try {
      let d = new Date();
      d.setTime(d.getTime() + 100000);
      document.cookie = "username=testcookie; expires" + d.toUTCString();
      this.cookieAllowed = document.cookie.indexOf("testcookie") != -1;
      if (!this.cookieAllowed) {
        this.log("Cookie are not allowed");
      }
    } catch (err) {
      this.log("Cookie check failed: " + err);
    }
  }

  isCookieAllowed(): boolean {
    return this.cookieAllowed;
  }

  login(): Promise<boolean> {
    if (this.loggedIn != null) {
      return Promise.resolve(this.loggedIn);
    }
    return new Promise((resolve) => {
      this.resolve = resolve;
      this.http.get<FrontendLoginState>(URL_FRONTEND + '/isloggedin').toPromise().then(loginState => {
        try {
          this.language = loginState.language;
          if (loginState.loggedIn) {
            this.loggedIn = true;
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
          this.log("Handle isloggedin response: " + err);
          this.loggedIn = false;
          resolve(false);
        }
      }).catch(err => {
        this.log("isloggedin catch: " + err);
        this.loggedIn = false;
        resolve(false);
      });
    });
  }

  log(message: any): void {
    this.http.post<void>(URL_FRONTEND + '/log', JSON.stringify(message), {headers: new HttpHeaders().set('Content-Type', 'text/plain')}).subscribe();
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
            this.loggedIn = loggedIn;
            this.resolve(loggedIn);
          },
          error => {
            this.log("facebookauthenticated catch: " + error);
            this.loggedIn = false;
            this.resolve(false);
          });
      } else {
        this.anonymousLogin();
      }
    });
  }

  private anonymousLogin(): void {
    this.http.post<void>(URL_FRONTEND + '/anonymouslogin', {}).subscribe(
      () => {
        this.loggedIn = false;
        this.resolve(false);
      },
      error => {
        this.log("anonymouslogin catch: " + error);
        this.loggedIn = false;
        this.resolve(false);
      });
  }

  static onFbScriptLoaded(frontendService: FrontendService) {
    frontendService.checkFbLoginState();
  }

  private onFbTimeout() {
    this.log("Facebook timed out");
    this.anonymousLogin();
  }
}
