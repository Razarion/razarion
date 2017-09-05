import {HumanPlayerId, QuestBackendInfo} from "../Common";

export class UserBackendInfo {
  humanPlayerId: HumanPlayerId;
  registerDate: Date;
  facebookId: string;
  name: string;
  activeQuest: QuestBackendInfo;
  completedQuests: QuestBackendInfo[];
  levelNumber: number;
  xp: number;
  crystals: number;
}
