import {Injectable} from "@angular/core";
import {HttpClient, HttpHeaders, HttpParams} from "@angular/common/http";
import {FbAuthResponse, FrontendLoginState, LoginResult, RegisterResult, URL_FRONTEND} from "../common";
import {Router} from "@angular/router";

declare const FB: any;
const FB_TIMEOUT: number = 8000;

@Injectable()
export class FrontendService {
  private language: string;
  private resolve: any;
  private fbTimerId: number;
  private loggedIn: boolean = null;
  private cookieAllowed: boolean = null;
  private fbScriptLoadedCallbacks: any[] = [];

  constructor(private http: HttpClient, private router: Router) {
    try {
      let d = new Date();
      d.setTime(d.getTime() + 5000);
      document.cookie = "TestCooiesEnabled=testcookie;path=/;expires=" + d.toUTCString();
      this.cookieAllowed = document.cookie.indexOf("testcookie") != -1;
      if (!this.cookieAllowed) {
        this.log("Cookie are not allowed", null);
      }
    } catch (err) {
      this.log("Cookie check failed", err);
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
            this.fbTimerId = window.setTimeout(() => this.onFbTimeout(), FB_TIMEOUT);
            this.fbScriptLoaded().then(() => this.checkFbLoginState());
          }
        } catch (err) {
          this.log("Handle isloggedin response", err);
          this.loggedIn = false;
          resolve(false);
        }
      }).catch(err => {
        this.log("isloggedin catch", err);
        this.loggedIn = false;
        resolve(false);
      });
    });
  }

  log(message: string, error: any): void {
    let body = new HttpParams().set(`message`, message);
    body = body.set(`url`, JSON.stringify(this.router.url).toString());
    if (error) {
      try {
        let errorMessage: string = "";
        if (error) {
          if (error.message) {
            errorMessage += 'message: ' + error.message;
          }
          if (error.stack) {
            errorMessage += '\nstack: ' + error.stack;
          }
        }
        body = body.set(`error`, errorMessage);
      } catch (innerErr) {
        body = body.set(`error`, "Error handling error: '" + innerErr.toString() + "' Original error '" + error.toString() + "'");
      }
    }
    this.http.post<void>(URL_FRONTEND + '/log', body, {headers: new HttpHeaders().set('Content-Type', 'application/x-www-form-urlencoded')}).subscribe();
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
      this.log("checkFbLoginState", err);
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
          this.log("facebookauthenticated catch", error);
          this.loggedIn = false;
          resolve(false);
        });
    });
  }

  subscribeFbAuthChange(facebookEventCallback: any) {
    try {
      FB.Event.subscribe("auth.statusChange", facebookEventCallback);
    } catch (err) {
      this.log("subscribeFbAuthChange", err);
    }
  }

  unsubscribeFbAuthChange(facebookEventCallback: any) {
    try {
      FB.Event.unsubscribe("auth.statusChange", facebookEventCallback);
    } catch (err) {
      this.log("unsubscribeFbAuthChange", err);
    }
  }

  parseFbXFBML() {
    try {
      FB.XFBML.parse();
    } catch (err) {
      this.log("parseFbXFBML", err);
    }
  }

  fbLogin(facebookLoginCallback: any) {
    try {
      FB.login(facebookLoginCallback);
    } catch (err) {
      this.log("fbLogin: ", err);
      facebookLoginCallback();
    }
  }

  fbScriptLoaded(): Promise<void> {
    if ((<any>window).RAZ_fbScriptLoadedFlag) {
      return Promise.resolve();
    } else {
      (<any>window).RAZ_fbScriptLoadedFrontendService = this;
      (<any>window).RAZ_fbScriptLoadedCallback = FrontendService.onFbScriptLoaded;
      return new Promise((resolve) => {
        this.fbScriptLoadedCallbacks.push(resolve);
      });
    }
  }

  static onFbScriptLoaded(frontendService: FrontendService) {
    for (let i = 0; i < frontendService.fbScriptLoadedCallbacks.length; i++) {
      try {
        frontendService.fbScriptLoadedCallbacks[i]();
      } catch (err) {
        frontendService.log("rontendService.fbScriptLoadedCallbacks failed", err);
      }
    }
  }

  private onFbTimeout() {
    this.log("Facebook timed out", null);
    this.loggedIn = false;
    this.resolve(false);
  }

  login(email: string, password: string, rememberMe: boolean): Promise<LoginResult> {
    return new Promise((resolve) => {
      const body = new HttpParams().set('email', email).set('password', password).set('rememberMe', rememberMe.toString());
      this.http.post<LoginResult>(URL_FRONTEND + '/login', body, {headers: new HttpHeaders().set('Content-Type', 'application/x-www-form-urlencoded')}).subscribe(
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
          this.log("login catch", error);
          this.loggedIn = false;
          resolve(LoginResult.UNKNOWN);
        });
    });
  }

  register(email: string, password: string, rememberMe: boolean): Promise<RegisterResult> {
    return new Promise((resolve) => {
      const body = new HttpParams().set(`email`, email).set(`password`, password).set('rememberMe', rememberMe.toString());
      this.http.post<RegisterResult>(URL_FRONTEND + '/createunverifieduser', body, {headers: new HttpHeaders().set('Content-Type', 'application/x-www-form-urlencoded')}).subscribe(
        registerResult => {
          if (registerResult == RegisterResult.OK) {
            this.loggedIn = true;
          }
          resolve(registerResult);
        },
        error => {
          this.log("register catch", error);
          this.loggedIn = false;
          resolve(RegisterResult.UNKNOWN_ERROR);
        });
    });
  }

  verifyEmail(email: string): Promise<boolean> {
    return new Promise((resolve) => {
      this.http.get<boolean>(URL_FRONTEND + '/isemailfree/' + encodeURI(email)).subscribe(
        valid => resolve(valid),
        error => {
          this.log("verifyEmail catch", error);
          resolve(false);
        });
    });
  }

  verifyEmailLink(verificationId: string): Promise<boolean> {
    return new Promise((resolve) => {
      this.http.post<boolean>(URL_FRONTEND + '/verifyemaillink', new HttpParams().set(`verificationId`, verificationId), {headers: new HttpHeaders().set('Content-Type', 'application/x-www-form-urlencoded')}).subscribe(
        success => {
          if (success) {
            this.loggedIn = true;
          }
          resolve(success);
        },
        error => {
          this.log("verifyemaillink catch", error);
          resolve(false);
        }
      );
    });
  }

  sendEmailForgotPassword(email: string): Promise<boolean> {
    return new Promise((resolve) => {
      this.http.post<boolean>(URL_FRONTEND + '/sendemailforgotpassword', new HttpParams().set(`email`, email), {headers: new HttpHeaders().set('Content-Type', 'application/x-www-form-urlencoded')}).subscribe(
        sent => resolve(sent),
        error => {
          this.log("sendemailforgotpassword catch", error);
          resolve(false);
        }
      )
    });
  }

  savePassword(password: string, uuid: string): Promise<boolean> {
    return new Promise((resolve) => {
      const body = new HttpParams().set(`password`, password).set(`uuid`, uuid);
      this.http.post<boolean>(URL_FRONTEND + '/savepassword', body, {headers: new HttpHeaders().set('Content-Type', 'application/x-www-form-urlencoded')}).subscribe(
        success => {
          if (success) {
            this.loggedIn = true;
          }
          resolve(success)
        },
        error => {
          this.log("savepassword catch", error);
          resolve(false);
        })
    });

  }

  logout() {
    this.http.post<void>(URL_FRONTEND + '/logout', {}).subscribe(
      () => {
        this.loggedIn = false;
      },
      error => {
        this.log("logout catch", error);
      });
    try {
      FB.logout((response) => {
        // user is now logged out
      });
    } catch (err) {
      this.log("FB.logout catch", err);
    }
  }

  trackNavigation(url: string) {
    this.http.post<boolean>(URL_FRONTEND + '/tracknavigation', new HttpParams().set(`url`, url), {headers: new HttpHeaders().set('Content-Type', 'application/x-www-form-urlencoded')}).subscribe(
      () => {
      },
      error => {
        this.log("verifyemaillink catch", error);
      }
    );
  }

  static validateEmail(email) {
    if (email == null || email == "") {
      return false;
    }
    let re = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
    return re.test(String(email).toLowerCase());
  }
}
