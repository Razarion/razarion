export const LOCALHOST_PREFIX = '';
// export const LOCALHOST_PREFIX = 'http://localhost:8080';
export const URL_BACKEND_PROVIDER = LOCALHOST_PREFIX + '/rest/backend';
export const URL_PLANET_MGMT = LOCALHOST_PREFIX + '/rest/servergameenginemgmt';

export class Common {
  public static handleError(error: any): Promise<any> {
    return Promise.reject(error.message || error);
  }
}

export class HumanPlayerId {
  playerId: number;
  userId: number;
}

export class SimpleUserBackend {
  humanPlayerId: HumanPlayerId;
  name: string;
}

export class QuestBackendInfo {
  id: number;
  internalName: string;
}

export class DecimalPosition {
  x: number;
  y: number;
}
