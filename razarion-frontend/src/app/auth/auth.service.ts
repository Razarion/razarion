import {Injectable} from '@angular/core';
import {AuthControllerClient, HttpClient as HttpClientAdapter, RestResponse} from '../generated/razarion-share';
import {HttpClient} from '@angular/common/http';
import {jwtDecode, JwtPayload} from 'jwt-decode';
import {TypescriptGenerator} from '../backend/typescript-generator';
import {Router} from '@angular/router';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private authControllerClient: AuthControllerClient;


  constructor(private httpClient: HttpClient,
              private router: Router) {
    this.authControllerClient = new AuthControllerClient(TypescriptGenerator.generateHttpClientAdapter(httpClient))
  }

  public checkToken(): Promise<void> {
    if (this.getAppToken() === null) {
      return Promise.resolve();
    }
    return this.authControllerClient.checkToken()
      .then(() => {
        return Promise.resolve();
      })
      .catch((error) => {
        const status = error?.status ?? error?.response?.status;
        if (status !== 401) {
          console.error("Error checking token:", error);
        }
        this.router.navigate(['invalid-token']);
        return Promise.resolve();
      });
  }

  getAppToken(): string | null {
    return localStorage.getItem("app.token");
  }

  isLoggedIn(): boolean {
    return !!this.getAppToken();
  }

  isAdmin(): boolean {
    if (!this.isLoggedIn()) {
      return false;
    }
    const decodedToken = jwtDecode<JwtPayload>(localStorage.getItem("app.token")!);
    const roleFromRoute = "ROLE_ADMIN"
    const roles = (<any>decodedToken).scope;

    if (!roles) {
      return false;
    }

    if (roles.includes(",")) {
      const roleArray = roles.split(",");
      for (let role of roleArray) {
        if (role === roleFromRoute) {
          return true;
        }
      }
    } else {
      if (roles === roleFromRoute) {
        return true;
      }
    }
    return false;
  }

  getUserName(): string {
    return (<JwtPayload>jwtDecode<JwtPayload>(localStorage.getItem("app.token")!)).sub || "<unknown>";
  }

  login(username: string, password: string): Promise<string> {
    this.logout();

    const httpClient = this.httpClient;
    let authController = new AuthControllerClient(new class implements HttpClientAdapter {

        request<R>(requestConfig: {
          method: string;
          url: string;
          queryParams?: any;
          data?: any;
          copyFn?: ((data: R) => R) | undefined;
        }): RestResponse<R> {
          return new Promise((resolve, error) => {
            const httpOptions = {
              headers: {
                Authorization: 'Basic ' + window.btoa(username + ':' + password)
              },
              responseType: 'text' as 'text',
              body: requestConfig.data
            };
            httpClient.request(requestConfig.method, "/" + requestConfig.url, httpOptions).subscribe({
              next: (objectNameIds: any) => {
                resolve(objectNameIds);
              },
              error: (err: any) => {
                console.log(err);
                error(err);
              }
            });
          });
        }
      }
    );

    return authController.auth()
  }

  logout() {
    localStorage.removeItem("app.token");
    localStorage.removeItem("app.roles");
  }
}
