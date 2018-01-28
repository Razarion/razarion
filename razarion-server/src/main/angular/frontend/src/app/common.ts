// export const LOCALHOST_PREFIX = '';
export const LOCALHOST_PREFIX = 'http://localhost:8080';
export const URL_FRONTEND = LOCALHOST_PREFIX + '/rest/frontend';

export class Common {
  public static handleError(error: any) {
    console.log("Error: " + error)
  }
}


export class LogonState {

}

export class FrontendUser {
  id: number;
  name: string;
  password: string;
  email: string;
}
