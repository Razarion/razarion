import {HumanPlayerId} from "../common";

export enum OnlineInfoType {
  NORMAL,
  ORPHAN,
  EXCEPTION,
  NO_SESSION,
  UNKNOWN
}

export class OnlineInfo {
  time: Date;
  duration: number;
  humanPlayerId: HumanPlayerId;
  name: string;
  sessionId: string;
  sessionTime: Date;
  multiplayerPlanet: string;
  multiplayerDate: Date;
  multiplayerDuration: number;
  type: OnlineInfoType;
}
