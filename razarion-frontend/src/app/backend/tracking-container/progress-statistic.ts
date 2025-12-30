import {TrackingContainerAnalyzer} from './tracking-container-analyzer';

export function createStatistics(trackingContainerAnalyzer: TrackingContainerAnalyzer): ProgressStatistic[] {
  const homeCount = trackingContainerAnalyzer.getDistinctHomePageRequests().length;
  const gameCount = trackingContainerAnalyzer.getGamePageRequests().length;
  const engineInit = trackingContainerAnalyzer.getGameEngineInits().length;
  const engineStartups = trackingContainerAnalyzer.getStartupTerminatedJsons().length;
  const userCreated = trackingContainerAnalyzer.getUserCreated().length;
  const baseCreated = trackingContainerAnalyzer.getBaseCreated().length;
  let progressStatistics = [
    new ProgressStatistic("Home", homeCount),
    new ProgressStatistic("Game", gameCount, homeCount),
    new ProgressStatistic("User created", userCreated, gameCount),
    new ProgressStatistic("Engine init", engineInit, userCreated),
    new ProgressStatistic("Engine started", engineStartups, engineInit),
    new ProgressStatistic("Base created", baseCreated, engineStartups),
  ];
  progressStatistics.push(...trackingContainerAnalyzer.generateLevelQuestStatistics(baseCreated));
  return progressStatistics;
}

export class ProgressStatistic {
  percent?: number;

  constructor(public readonly name: String, public readonly count: number, lastCount?: number) {
    if (lastCount !== undefined) {
      this.percent = Math.round(count / lastCount * 100);
    }
  }
}
