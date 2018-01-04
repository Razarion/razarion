export class SearchConfig {
  fromDate: Date;
  botFilter: boolean;
}

export class SessionTracker {
  time: Date;
  id: string;
  gameAttempts: number;
  successGameAttempts: number;
  fbAdRazTrack: string;
  remoteHost: string;
  userAgent: string;
  pageHits: number;
}

export class SessionDetail {
  time: Date;
  id: string;
  fbAdRazTrack: string;
  userAgent: string;
  remoteHost: string;
  remoteAddr: string;
  gameSessionDetails: GameSessionDetail[];
  pageDetails: PageDetail[];
}

export class PageDetail {
  time: Date;
  page: string;
  parameters: string;
  uri: string;
}

export class GameSessionDetail {
  time: Date;
  id: string;
  sessionId: string;
  startupTaskDetails: StartupTaskDetail[];
  startupTerminatedDetail: StartupTerminatedDetail;
  inGameTracking: boolean;
  sceneTrackerDetails: SceneTrackerDetail[];
  perfmonTrackerDetails: PerfmonTrackerDetail[];
  perfmonTerrainTileDetails: PerfmonTerrainTileDetail[];
}

export class StartupTaskDetail {
  taskEnum: string;
  clientStartTime: Date;
  duration: number;
  error: string;
}

export class StartupTerminatedDetail {
  successful: boolean;
  totalTime: number;
  timeStamp: Date;
}

export class SceneTrackerDetail {
  clientStartTime: Date;
  internalName: string;
  duration: number;
}

export class PerfmonTrackerDetail {
  clientStartTime: Date;
  type: string;
  frequency: number;
  duration: number;
}

export class PerfmonTerrainTileDetail {
  clientStartTime: Date;
  positionX: number;
  positionY: number;
  duration: number;
}
