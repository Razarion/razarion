export class Session {
  time: Date;
  id: string;
  fbAdRazTrack: number;
  remoteHost: string;
}

export class SessionDetail {
  time: Date;
  id: string;
  fbAdRazTrack: number;
  gameSessionDetails: GameSessionDetail[];
}

export class GameSessionDetail {
  time: Date;
  id: string;
  sessionId: string;
}

export class SearchConfig {
  fromDate: Date;
}