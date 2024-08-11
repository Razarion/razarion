export const APPLICATION_PATH = '/rest';
export const GZ_PATH = APPLICATION_PATH + '/gz';
export const EDITOR_PATH = APPLICATION_PATH + '/editor';
export const URL_FRONTEND = APPLICATION_PATH + '/frontend';
export const SERVER_GAME_ENGINE_PATH = APPLICATION_PATH + '/server-game-engine';
export const URL_BACKEND_PROVIDER = APPLICATION_PATH + '/backend';
export const URL_PLANET_MGMT = APPLICATION_PATH + '/servergameenginemgmt';
export const URL_IMAGE = APPLICATION_PATH + '/image';
export const URL_MODEL = APPLICATION_PATH + '/model';
export const URL_THREE_JS_MODEL = GZ_PATH + '/three-js-model';
export const URL_THREE_JS_MODEL_EDITOR = EDITOR_PATH + '/three-js-model';
export const URL_THREE_JS_MODEL_PACK_EDITOR = EDITOR_PATH + '/three-js-model-pack-editor';
export const UPDATE_RADIUS_REST_CALL = EDITOR_PATH + '/terrain-object/update-radius';
export const SLOPE_EDITOR_PATH = EDITOR_PATH + '/slope';
export const DRIVEWAY_EDITOR_PATH = EDITOR_PATH + '/driveway';
export const SERVER_GAME_ENGINE_EDITOR = EDITOR_PATH + '/server-game-engine'
export const BASE_ITEM_TYPE_EDITOR_PATH = EDITOR_PATH + '/base_item_type';
export const RESOURCE_ITEM_TYPE_EDITOR_PATH = EDITOR_PATH + '/resource_item_type';
export const LEVEL_EDITOR_PATH = EDITOR_PATH + '/level';
export const GROUND_EDITOR_PATH = EDITOR_PATH + '/ground';
export const WATER_EDITOR_PATH = EDITOR_PATH + '/water';
export const PLANET_EDITOR_PATH = EDITOR_PATH + "/planet";
export const TERRAIN_EDITOR = APPLICATION_PATH + "/planeteditor";
export const READ_TERRAIN_SLOPE_POSITIONS = TERRAIN_EDITOR + "/readTerrainSlopePositions";
export const UPDATE_SLOPES_TERRAIN_EDITOR = TERRAIN_EDITOR + "/updateSlopes";
export const UPDATE_MINI_MAP_IMAGE = TERRAIN_EDITOR + "/updateMiniMapImage";

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




