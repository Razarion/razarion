export const APPLICATION_PATH = '/rest';
export const EDITOR_PATH = APPLICATION_PATH + '/editor';
export const URL_PLANET_MGMT = APPLICATION_PATH + '/servergameenginemgmt';
export const URL_IMAGE = APPLICATION_PATH + '/image';
export const URL_GLTF = APPLICATION_PATH + "/gltf";
export const UPDATE_RADIUS_REST_CALL = EDITOR_PATH + '/terrain-object/update-radius';
export const BASE_ITEM_TYPE_EDITOR_PATH = EDITOR_PATH + '/base_item_type';
export const RESOURCE_ITEM_TYPE_EDITOR_PATH = EDITOR_PATH + '/resource_item_type';
export const GROUND_EDITOR_PATH = EDITOR_PATH + '/ground';
export const WATER_EDITOR_PATH = EDITOR_PATH + '/water';
export const PLANET_EDITOR_PATH = EDITOR_PATH + "/planet";
export const UPDATE_MINI_MAP_IMAGE = EDITOR_PATH + "/planeteditor/updateMiniMapImage";

export enum RegisterResult {
  USER_ALREADY_LOGGED_IN = 'USER_ALREADY_LOGGED_IN',
  INVALID_EMAIL = 'INVALID_EMAIL',
  EMAIL_ALREADY_USED = 'EMAIL_ALREADY_USED',
  INVALID_PASSWORD = 'INVALID_PASSWORD',
  OK = 'OK',
  UNKNOWN_ERROR = 'UNKNOWN_ERROR'
}

export function getImageUrl(id: number): string {
  return `${URL_IMAGE}/${id}`
}

export function getUpdateUrl(imageId: number): string {
  return `${URL_IMAGE}/update/${imageId}`
}

export function getMiniMapPlanetUrl(planetId: number): string {
  return `${URL_IMAGE}/minimap/${planetId}`
}

export function getUpdateMiniMapPlanetUrl(planetId: number): string {
  return `${UPDATE_MINI_MAP_IMAGE}/${planetId}`
}




