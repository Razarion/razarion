import {HumanPlayerId} from "../Common";

export enum OnlineInfoType {
  NORMAL,
  ORPHAN,
  UNKNOWN
}

export class OnlineInfo {
  time: Date;
  duration: number;
  humanPlayerId: HumanPlayerId;
  sessionId: string;
  multiplayerPlanet: string;
  multiplayerDate: Date;
  multiplayerDuration: number;
  type: OnlineInfoType;
}
