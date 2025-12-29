import {TrackingContainerAnalyzer} from './tracking-container-analyzer';

export function createStatistics(trackingContainerAnalyzer: TrackingContainerAnalyzer): ProgressStatistic[] {
  const homeCount = trackingContainerAnalyzer.getHomePageRequests().length;
  const gameCount = trackingContainerAnalyzer.getGamePageRequests().length;
  const engineInit = trackingContainerAnalyzer.getGameEngineInits().length;
  const engineStartups = trackingContainerAnalyzer.getStartupTerminatedJsons().length;
  const userCreated = trackingContainerAnalyzer.getUserCreated().length;
  const baseCreated = trackingContainerAnalyzer.getBaseCreated().length;
  return [
    new ProgressStatistic("Home", homeCount),
    new ProgressStatistic("Game", gameCount, homeCount),
    new ProgressStatistic("Engine init", engineInit, gameCount),
    new ProgressStatistic("Engine started", engineStartups, engineInit),
    new ProgressStatistic("User created", userCreated, engineStartups),
    new ProgressStatistic("Base created", baseCreated, userCreated),
    new ProgressStatistic("Quest 1", 0),
    new ProgressStatistic("Quest 2", 0),
    new ProgressStatistic("Level 1", 0),
  ];
}

export class ProgressStatistic {
  percent?: number;

  constructor(public readonly name: String, public readonly count: number, lastCount?: number) {
    if (lastCount !== undefined) {
      this.percent = Math.round(count / lastCount * 100);
    }
  }
}
