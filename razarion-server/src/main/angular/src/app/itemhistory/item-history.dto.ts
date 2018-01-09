import {DecimalPosition} from "../Common";

export class ItemTracking {
  timeStamp: Date;
  type: ItemTrackingType;
  targetBaseId: number;
  targetBaseBotId: number;
  targetHumanPlayerId: number;
  actorBaseId: number;
  actorBaseBotId: number;
  actorHumanPlayerId: number;
  itemId: number;
  decimalPosition: DecimalPosition;
  itemTypeId: number;
  actorItemId: number;
  date: Date;
  name: string;
  id: number;
  playerId: number;
  sessionId: string;
}

export enum ItemTrackingType {
  SERVER_START = 'SERVER_START',
  BASE_CREATED = 'BASE_CREATED',
  BASE_DELETE = 'BASE_DELETE',
  BASE_ITEM_SPAWN = 'BASE_ITEM_SPAWN',
  BASE_ITEM_SPAWN_DIRECTLY = 'BASE_ITEM_SPAWN_DIRECTLY',
  BASE_ITEM_BUILT = 'BASE_ITEM_BUILT',
  BASE_ITEM_FACTORIZED = 'BASE_ITEM_FACTORIZED',
  BASE_ITEM_KILLED = 'BASE_ITEM_KILLED',
  BASE_ITEM_REMOVED = 'BASE_ITEM_REMOVED',
  RESOURCE_ITEM_CREATED = 'RESOURCE_ITEM_CREATED',
  RESOURCE_ITEM_DELETED = 'RESOURCE_ITEM_DELETED',
  BOX_ITEM_CREATED = 'BOX_ITEM_CREATED',
  BOX_ITEM_DELETED = 'BOX_ITEM_DELETED',
}
