// export const LOCALHOST_PREFIX = '';
export const LOCALHOST_PREFIX = 'http://localhost:8080';
export const URL_TRACKING = LOCALHOST_PREFIX + '/rest/trackerbackend';
export const URL_PLANET_MGMT = LOCALHOST_PREFIX + '/rest/servergameenginemgmt';
export const URL_SERVER_MGMT = LOCALHOST_PREFIX + '/rest/servermgmtprovider';

export class Common {
  public static handleError(error: any): Promise<any> {
    return Promise.reject(error.message || error);
  }
}

export class HumanPlayerId {
  playerId: number;
  userId: number;
}

export class QuestBackendInfo {
  id: number;
  internalName: string;
}

export class DecimalPosition {
  x: number;
  y: number;
}
