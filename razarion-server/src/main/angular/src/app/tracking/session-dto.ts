export class SessionTracker {
  time: Date;
  id: string;
  fbAdRazTrack: string;
  remoteHost: string;
  userAgent: string;
}

export class SessionDetail {
  time: Date;
  id: string;
  fbAdRazTrack: string;
  userAgent: string;
  gameSessionDetails: GameSessionDetail[];
  pageDetails: PageDetail[];
}

export class GameSessionDetail {
  time: Date;
  id: string;
  sessionId: string;
}

export class SearchConfig {
  fromDate: Date;
}

export class PageDetail {
  time: Date;
  page: string;
  parameters: string;
  uri: string;
}
