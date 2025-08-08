import {Injectable} from '@angular/core';
import {
  HttpClient as HttpClientAdapter,
  RegisterResult,
  RestResponse,
  UserControllerClient
} from '../generated/razarion-share';
import {HttpClient} from '@angular/common/http';
import {jwtDecode, JwtPayload} from 'jwt-decode';
import {TypescriptGenerator} from '../backend/typescript-generator';
import {Router} from '@angular/router';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private userControllerClient: UserControllerClient;
  public registerPending = false;

  constructor(private httpClient: HttpClient,
              private router: Router) {
    this.userControllerClient = new UserControllerClient(TypescriptGenerator.generateHttpClientAdapter(httpClient))
  }

  public async checkToken(): Promise<void> {
    if (this.getAppToken() === null) {
      return Promise.resolve();
    }
    try {
      await this.userControllerClient.checkToken();
      return await Promise.resolve();
    } catch (error: any) {
      const status = error?.status ?? error?.response?.status;
      if (status !== 401) {
        console.error("Error checking token:", error);
      }
      this.router.navigate(['invalid-token']);
      return await Promise.resolve();
    }
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

  getAuthenticatedUserName(): string {
    const token = localStorage.getItem("app.token");
    if (!token) {
      return "<unknown>";
    }

    try {
      const payload = jwtDecode<JwtPayload>(token);
      return payload.sub || "<unknown>";
    } catch (e) {
      console.error("Invalid JWT token", e);
      return "<unknown>";
    }
  }

  async login(username: string, password: string): Promise<void> {
    this.logout();

    const httpClient = this.httpClient;
    let userController = new UserControllerClient(new class implements HttpClientAdapter {
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
              next: (token: any) => {
                resolve(token);
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

    const token_2 = await userController.auth();
    localStorage.setItem("app.token", token_2);
  }

  logout() {
    localStorage.removeItem("app.token");
    localStorage.removeItem("app.roles");
  }

  async register(email: string, password: string): Promise<RegisterResult> {
    const result = await this.userControllerClient.registerByEmail({email, password});
    if (result === RegisterResult.OK) {
      this.registerPending = true;
    }
    return result;
  }

  async deleteUser(): RestResponse<void> {
    await this.userControllerClient.deleteUser();
  }
}
