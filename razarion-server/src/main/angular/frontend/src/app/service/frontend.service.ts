import {Injectable} from "@angular/core";
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {FbAuthResponse, FrontendLoginState, LoginResult, URL_FRONTEND} from "../common";

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
      d.setTime(d.getTime() + 5000);
      document.cookie = "TestCooiesEnabled=testcookie; expires" + d.toUTCString();
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

  autoLogin(): Promise<boolean> {
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

  private checkFbLoginState() {
    try {
      FB.getLoginStatus(fbResponse => {
        if (this.fbTimerId != null) {
          window.clearInterval(this.fbTimerId);
          this.fbTimerId = null;
        }
        if (fbResponse.status === 'connected') {
          // the user is logged in and has authenticated your app
          this.onFbAuthorized(fbResponse.authResponse).then(loggedIn => {
            this.resolve(loggedIn);
          });
        } else {
          this.loggedIn = false;
          this.resolve(false);
        }
      });
    } catch (err) {
      this.log("checkFbLoginState: " + err);
      this.loggedIn = false;
      this.resolve(false);
    }
  }

  onFbAuthorized(authResponse: any): Promise<boolean> {
    return new Promise((resolve) => {
      let fbAuthResponse: FbAuthResponse = new FbAuthResponse();
      fbAuthResponse.accessToken = authResponse.accessToken;
      fbAuthResponse.expiresIn = authResponse.expiresIn;
      fbAuthResponse.signedRequest = authResponse.signedRequest;
      fbAuthResponse.userID = authResponse.userID;
      this.http.post<boolean>(URL_FRONTEND + '/facebookauthenticated', fbAuthResponse, {headers: new HttpHeaders().set('Content-Type', 'application/json')}).subscribe(
        loggedIn => {
          this.loggedIn = loggedIn;
          resolve(loggedIn);
        },
        error => {
          this.log("facebookauthenticated catch: " + error);
          this.loggedIn = false;
          resolve(false);
        });
    });
  }

  subscribeFbAuthChange(facebookEventCallback: any) {
    try {
      FB.Event.subscribe("auth.statusChange", facebookEventCallback);
    } catch (err) {
      this.log("subscribeFbAuthChange: " + err);
    }
  }

  unsubscribeFbAuthChange(facebookEventCallback: any) {
    try {
      FB.Event.unsubscribe("auth.statusChange", facebookEventCallback);
    } catch (err) {
      this.log("unsubscribeFbAuthChange: " + err);
    }
  }

  parseFbXFBML() {
    try {
      FB.XFBML.parse();
    } catch (err) {
      this.log("parseFbXFBML: " + err);
    }
  }

  fbLogin(facebookLoginCallback: any) {
    try {
      FB.login(facebookLoginCallback);
    } catch (err) {
      this.log("fbLogin: " + err);
      facebookLoginCallback();
    }
  }

  static onFbScriptLoaded(frontendService: FrontendService) {
    frontendService.checkFbLoginState();
  }

  private onFbTimeout() {
    this.log("Facebook timed out");
    this.loggedIn = false;
    this.resolve(false);
  }

  login(email: string, password: string): Promise<LoginResult> {
    return new Promise((resolve) => {
      this.http.post<LoginResult>(URL_FRONTEND + '/login', {email: email, password: password}, {headers: new HttpHeaders().set('Content-Type', 'application/json')}).subscribe(
        loginResult => {
          if (loginResult == LoginResult.OK) {
            this.loggedIn = true;
            resolve(loginResult);
          } else {
            this.loggedIn = false;
            resolve(loginResult);
          }
        },
        error => {
          this.log("login catch: " + error);
          this.loggedIn = false;
          resolve(LoginResult.UNKNOWN);
        });
    });
  }

  register(email: string, password: string, rememberMe: boolean): Promise<boolean> {
    return new Promise((resolve) => {
      this.http.post<boolean>(URL_FRONTEND + '/register', {email: email, password: password}, {headers: new HttpHeaders().set('Content-Type', 'application/json')}).subscribe(
        loggedIn => {
          this.loggedIn = loggedIn;
          resolve(loggedIn);
        },
        error => {
          this.log("register catch: " + error);
          this.loggedIn = false;
          resolve(false);
        });
    });
  }

  verifyEmail(email: string): Promise<boolean> {
    return new Promise((resolve) => {
      this.http.post<boolean>(URL_FRONTEND + '/verifyemail', {email: email}, {headers: new HttpHeaders().set('Content-Type', 'application/json')}).subscribe(
        valid => resolve(valid)
      );
    });
  }

  verifyEmailLink(verificationId: string): Promise<boolean> {
    return new Promise((resolve) => {
      this.http.post<boolean>(URL_FRONTEND + '/verifyemaillink', {verificationId: verificationId}, {headers: new HttpHeaders().set('Content-Type', 'application/json')}).subscribe(
        success => {
          if (success) {
            this.loggedIn = true;
          }
          resolve(success);
        }
      );
    });
  }

  sendEmailForgotPassword(email: any): Promise<boolean> {
    return new Promise((resolve) => {
      this.http.post<boolean>(URL_FRONTEND + '/sendemailforgotpassword', {email: email}, {headers: new HttpHeaders().set('Content-Type', 'application/json')}).subscribe(
        sent => resolve(sent)
      )
    });
  }

  savePassword(password: string, uuid: string): Promise<boolean> {
    return new Promise((resolve) => {
      this.http.post<boolean>(URL_FRONTEND + '/savepassword', {password: password, uuid: uuid}, {headers: new HttpHeaders().set('Content-Type', 'application/json')}).subscribe(
        success => {
          if (success) {
            this.loggedIn = true;
          }
          resolve(success)
        }
      )
    });

  }
}
