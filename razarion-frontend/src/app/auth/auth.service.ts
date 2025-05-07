import {Injectable} from '@angular/core';
import {AuthControllerClient, HttpClient as HttpClientAdapter, RestResponse} from '../generated/razarion-share';
import {HttpClient} from '@angular/common/http';
import {jwtDecode, JwtPayload} from 'jwt-decode';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  constructor(private httpClient: HttpClient) {
  }

  isLoggedIn(): boolean {
    return !!localStorage.getItem("app.token");
  }

  isAdmin(): boolean {
    if (!this.isLoggedIn()) {
      return false;
    }
    const decodedToken = jwtDecode<JwtPayload>(localStorage.getItem("app.token")!);
    const roleFromRoute = "ROLE_ADMIN"
    const roles = (<any>decodedToken).scope;

    if (roles!.includes(",")) {
      if (roles === roleFromRoute) {
        return true;
      }
    } else {
      const roleArray = roles!.split(",");
      for (let role of roleArray) {
        if (role === roleFromRoute) {
          return true;
        }
      }
    }
    return false;
  }

  getUserName(): string {
    return (<JwtPayload>jwtDecode<JwtPayload>(localStorage.getItem("app.token")!)).sub || "<unknown>";
  }

  login(username: string, password: string): Promise<string> {
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
            httpClient.request(requestConfig.method, requestConfig.url, httpOptions).subscribe({
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
