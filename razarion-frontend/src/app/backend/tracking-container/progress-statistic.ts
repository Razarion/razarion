import {TrackingContainerAnalyzer} from './tracking-container-analyzer';

export function createStatistics(trackingContainerAnalyzer: TrackingContainerAnalyzer): ProgressStatistic[] {
  const homeCount = trackingContainerAnalyzer.getDistinctHomePageRequests().length;
  const gameCount = trackingContainerAnalyzer.getGamePageRequests().length;
  // One stage between opening the game page and building a base: the engine is up and the player
  // could play. The former User created / Engine init / Engine started rows tracked internals of
  // the boot sequence, not whether anybody got that far.
  const gameStarted = trackingContainerAnalyzer.getGameStarted().length;
  const baseCreated = trackingContainerAnalyzer.getBaseCreated().length;
  let progressStatistics = [
    new ProgressStatistic("Home", homeCount),
    new ProgressStatistic("Game", gameCount, homeCount),
    new ProgressStatistic("Engine running", gameStarted, gameCount),
    new ProgressStatistic("Initial Base created", baseCreated, gameStarted),
  ];
  progressStatistics.push(...trackingContainerAnalyzer.generateLevelQuestStatistics(baseCreated));
  return progressStatistics;
}

export class ProgressStatistic {
  percent?: number;

  constructor(public readonly name: String, public readonly count: number, lastCount?: number) {
    if (lastCount !== undefined && lastCount > 0) {
      this.percent = Math.round(count / lastCount * 100);
    }
  }
}
