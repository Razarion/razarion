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
  unlockedBackendInfos: UnlockedBackendInfo[];
  gameHistoryEntries: GameHistoryEntry[];
}

export class UnlockedBackendInfo {
  id: number;
  internalName: string;
}

export class GameHistoryEntry {
  date: Date;
  description: string;
}
