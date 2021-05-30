export const LOCALHOST_PREFIX = '';
// export const LOCALHOST_PREFIX = 'http://localhost:8080';
export const URL_FRONTEND = LOCALHOST_PREFIX + '/rest/frontend';
export const SERVER_GAME_ENGINE_PATH = LOCALHOST_PREFIX + '/rest/server-game-engine';
export const URL_BACKEND_PROVIDER = LOCALHOST_PREFIX + '/rest/backend';
export const URL_PLANET_MGMT = LOCALHOST_PREFIX + '/rest/servergameenginemgmt';

export interface FrontendLoginState {
  loggedIn: boolean;
  language: string;
}

export interface FbAuthResponse {
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
