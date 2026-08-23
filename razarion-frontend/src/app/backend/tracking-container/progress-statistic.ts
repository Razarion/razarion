import {TrackingContainerAnalyzer} from './tracking-container-analyzer';

/**
 * The funnel table, top to bottom.
 * <p>
 * It has two halves and they do not describe the same population. Everything above Game rests on
 * the landing pixel, which only fires when the page carries a query string; everything from Game
 * down also holds the visitors who arrived over a plain link and never fired one. The two are
 * joined by "Game (from Home)", which counts the visitors that appear in both - so the landing
 * page's own conversion is measured on one population, and no row below is a share of a number it
 * could never reach.
 */
export function createStatistics(trackingContainerAnalyzer: TrackingContainerAnalyzer): ProgressStatistic[] {
  const homeCount = trackingContainerAnalyzer.countHome();
  const gameCount = trackingContainerAnalyzer.countGame();
  // One stage between opening the game page and building a base: the engine is up and the player
  // could play. The former User created / Engine init / Engine started rows tracked internals of
  // the boot sequence, not whether anybody got that far.
  const gameStarted = trackingContainerAnalyzer.countEngineRunning();
  const baseCreated = trackingContainerAnalyzer.getBaseCreatedUserIds().length;
  let progressStatistics: ProgressStatistic[] = [];
  if (trackingContainerAnalyzer.getView() === 'all') {
    // Context, never a stage and never a percentage base: LANDING is recorded without the pixel
    // and so describes a wider population than every row below it.
    progressStatistics.push(new ProgressStatistic("Landing (context, not a funnel stage)",
      trackingContainerAnalyzer.countLanding()));
  }
  progressStatistics.push(
    new ProgressStatistic("Home (landing pixel)", homeCount),
    // Both of the next two are measured against Home rather than chained to each other. Chaining
    // the game row to Play clicked would be the tidier funnel, but every period before the click
    // was tracked has none at all, and that row would lose its percentage there - the one number
    // this table is actually read for. Measured against Home, the gap between the two is the loss
    // on the way from the button to the game.
    new ProgressStatistic("Play clicked", trackingContainerAnalyzer.countPlayClicked(), homeCount),
    new ProgressStatistic("Game (from Home)", trackingContainerAnalyzer.countGameFromHome(), homeCount),
    // No percentage: this is where the population widens. A visitor who arrived over a plain link
    // fires no pixel and produces no /game record either - the server only writes one when the url
    // carries a query string - so their game visit is known from the startup records alone, and
    // there is no Home number they could ever be a share of.
    new ProgressStatistic("Game (total)", gameCount),
    new ProgressStatistic("Engine running", gameStarted, gameCount),
    new ProgressStatistic("Initial Base created", baseCreated, gameStarted));
  progressStatistics.push(...trackingContainerAnalyzer.generateLevelQuestStatistics(baseCreated));
  return progressStatistics;
}

export class ProgressStatistic {
  percent?: number;

  /**
   * The reference is whatever population this row is a share of - usually the stage above it, but
   * not always: the quest rows are shares of the level they belong to, because they are sorted by
   * size and have no row above them in any meaningful sense.
   */
  constructor(public readonly name: String, public readonly count: number, reference?: number) {
    if (reference !== undefined && reference > 0) {
      this.percent = Math.round(count / reference * 100);
    }
  }
}
