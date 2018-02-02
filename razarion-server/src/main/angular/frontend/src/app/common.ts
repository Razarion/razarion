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

export enum RegisterResult {
  USER_ALREADY_LOGGED_IN = 'USER_ALREADY_LOGGED_IN',
  INVALID_EMAIL = 'INVALID_EMAIL',
  EMAIL_ALREADY_USED = 'EMAIL_ALREADY_USED',
  INVALID_PASSWORD = 'INVALID_PASSWORD',
  OK = 'OK',
  UNKNOWN_ERROR = 'UNKNOWN_ERROR'
}

export enum LoginResult {
  WRONG_PASSWORD = 'WRONG_PASSWORD',
  WRONG_EMAIL = 'WRONG_EMAIL',
  OK = 'OK',
  UNKNOWN = 'UNKNOWN'
}
