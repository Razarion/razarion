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
  name: string;
  sessionId: string;
  multiplayerPlanet: string;
  multiplayerDate: Date;
  multiplayerDuration: number;
  type: OnlineInfoType;
}
