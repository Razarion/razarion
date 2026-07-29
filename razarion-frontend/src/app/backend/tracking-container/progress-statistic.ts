import {TrackingContainerAnalyzer} from './tracking-container-analyzer';

export function createStatistics(trackingContainerAnalyzer: TrackingContainerAnalyzer): ProgressStatistic[] {
  const homeCount = trackingContainerAnalyzer.getDistinctHomePageRequests().length;
  const playClickedCount = trackingContainerAnalyzer.getPlayClickedPageRequests().length;
  const gameCount = trackingContainerAnalyzer.getGamePageRequests().length;
  // One stage between opening the game page and building a base: the engine is up and the player
  // could play. The former User created / Engine init / Engine started rows tracked internals of
  // the boot sequence, not whether anybody got that far.
  const gameStarted = trackingContainerAnalyzer.getGameStarted().length;
  const baseCreated = trackingContainerAnalyzer.getBaseCreated().length;
  let progressStatistics = [
    new ProgressStatistic("Home", homeCount),
    // Both of the next two are measured against Home rather than chained to each other. Chaining
    // Game to Play clicked would be the tidier funnel, but every period before this was tracked
    // has no clicks at all, and the Game row would lose its percentage there - the one number
    // this table is actually read for. Measured against Home, the gap between the two rows is
    // the loss on the way from the button to the game.
    new ProgressStatistic("Play clicked", playClickedCount, homeCount),
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
