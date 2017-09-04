export const LOCALHOST_PREFIX = '';
//export const LOCALHOST_PREFIX = 'http://localhost:8080';
export const URL_TRACKING = LOCALHOST_PREFIX + '/rest/trackerbackend';
export const URL_PLANET_MGMT = LOCALHOST_PREFIX + '/rest/servergameenginemgmt';

export class Common {

  public static handleError(error: any): Promise<any> {
    return Promise.reject(error.message || error);
  }

}
