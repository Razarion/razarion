import { Injectable } from "@angular/core";
import { HttpClient, HttpHeaders, HttpParams } from "@angular/common/http";
import { RegisterResult, URL_FRONTEND } from "../common";
import { Router } from "@angular/router";
import {
  ClientLogRecord,
  FrontendControllerClient,
  LoginResult,
  UserRequest
} from "../generated/razarion-share";
import { TypescriptGenerator } from "../backend/typescript-generator";

@Injectable()
export class FrontendService {
  private language: string = "";
  loggedIn?: boolean;
  private cookieAllowed: boolean = false;
  private frontendControllerClient: FrontendControllerClient;

  constructor(private httpClient: HttpClient, private router: Router) {
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
    this.frontendControllerClient = new FrontendControllerClient(TypescriptGenerator.generateHttpClientAdapter(httpClient));
  }

  static validateEmail(email: any) {
    if (email == null || email == "") {
      return false;
    }
    let re = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
    return re.test(String(email).toLowerCase());
  }

  isCookieAllowed(): boolean {
    return this.cookieAllowed;
  }

  autoLogin(): Promise<boolean> {
    if (this.loggedIn) {
      return Promise.resolve(this.loggedIn);
    }
    return new Promise((resolve) => {
      this.frontendControllerClient.isLoggedIn()
        .then(frontendLoginState => {
          try {
            if (frontendLoginState !== undefined) {
              this.language = frontendLoginState.language;
              if (frontendLoginState.loggedIn) {
                this.loggedIn = true;
                resolve(true);
              } else {
                this.loggedIn = false;
                resolve(false);
              }
            } else {
              this.log("loginState === undefined", null);
              this.loggedIn = false;
              resolve(false);
            }
          } catch (err) {
            this.log("Handle isloggedin response", err);
            this.loggedIn = false;
            resolve(false);
          }
        })
        .catch(err => {
          this.log("isloggedin catch", err);
          this.loggedIn = false;
          resolve(false);
        });
    });
  }

  log(message: string, error: any): void {
    let clientLogRecord: ClientLogRecord = {
      message: message,
      url: JSON.stringify(this.router.url).toString(),
      error: ""
    };

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
        clientLogRecord.error = errorMessage;
      } catch (innerErr) {
        clientLogRecord.error = "Error handling error: '" + "" + innerErr + "' Original error '" + error.toString() + "'";
      }
    }
    this.frontendControllerClient.log(clientLogRecord);
  }

  getLanguage(): string {
    return this.language;
  }

  login(email: string, password: string, rememberMe: boolean): Promise<LoginResult | null> {
    return new Promise((resolve) => {
      let userRequest: UserRequest = {
        email: email,
        password: password,
        rememberMe: rememberMe
      }
      this.frontendControllerClient.loginUser(userRequest)
        .then(loginResult => {
          if (loginResult == LoginResult.OK) {
            this.loggedIn = true;
            resolve(loginResult);
          } else {
            this.loggedIn = false;
            resolve(loginResult);
          }
        })
        .catch(error => {
          this.log("login catch", error);
          this.loggedIn = false;
          resolve(null);
        });
    });
  }

  register(email: string, password: string, rememberMe: boolean): Promise<RegisterResult> {
    return new Promise((resolve) => {
      const body = new HttpParams().set(`email`, email).set(`password`, password).set('rememberMe', rememberMe.toString());
      this.httpClient.post<RegisterResult>(URL_FRONTEND + '/createunverifieduser', body, { headers: new HttpHeaders().set('Content-Type', 'application/x-www-form-urlencoded') }).subscribe(
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
      this.httpClient.get<boolean>(URL_FRONTEND + '/isemailfree/' + encodeURIComponent(email)).subscribe(
        valid => resolve(valid),
        error => {
          this.log("verifyEmail catch", error);
          resolve(false);
        });
    });
  }

  verifyEmailLink(verificationId: string): Promise<boolean> {
    return new Promise((resolve) => {
      this.httpClient.post<boolean>(URL_FRONTEND + '/verifyemaillink', new HttpParams().set(`verificationId`, verificationId), { headers: new HttpHeaders().set('Content-Type', 'application/x-www-form-urlencoded') }).subscribe(
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
      this.httpClient.post<boolean>(URL_FRONTEND + '/sendemailforgotpassword', new HttpParams().set(`email`, email), { headers: new HttpHeaders().set('Content-Type', 'application/x-www-form-urlencoded') }).subscribe(
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
      this.httpClient.post<boolean>(URL_FRONTEND + '/savepassword', body, { headers: new HttpHeaders().set('Content-Type', 'application/x-www-form-urlencoded') }).subscribe(
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
    this.frontendControllerClient.logout().then(() => {
      this.loggedIn = false;
    }).catch(error => {
      this.log("logout catch", error);
    });
  }

  trackNavigation(url: string) {
    this.httpClient.post<boolean>(URL_FRONTEND + '/tracknavigation', new HttpParams().set(`url`, url), { headers: new HttpHeaders().set('Content-Type', 'application/x-www-form-urlencoded') }).subscribe(
      () => {
      },
      error => {
        this.log("verifyemaillink catch", error);
      }
    );
  }

  clearRemeberMe() {
    this.frontendControllerClient.clearRememberMe();
  }

}
