export const LOCALHOST_PREFIX = '';
// export const LOCALHOST_PREFIX = 'http://localhost:8080';
export const URL_FRONTEND = LOCALHOST_PREFIX + '/rest/frontend';

export class FrontendLoginState {
  loggedIn: boolean;
  language: string;
}

export class FbAuthResponse {
  accessToken: string;
  expiresIn: number;
  signedRequest: string;
  userID: string;
}
