export class SessionDetail {
  time: Date;
  id: string;
  fbAdRazTrack: number;
  gameSessionDetails: GameSessionDetail[];
}

export class GameSessionDetail {
  time: Date;
  id: string;
}
