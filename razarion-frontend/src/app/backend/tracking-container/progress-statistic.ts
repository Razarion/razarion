import {QuestRowInfo, TrackingContainerAnalyzer} from './tracking-container-analyzer';

/**
 * The funnel, as two tables.
 * <p>
 * They do not describe the same population, and that is why they are two. The landing page table
 * rests on the landing pixel, which only fires when the page carries a query string; the game table
 * also holds the visitors who arrived over a plain link and never fired one. Both used to be one
 * list, joined by "Game (from Home)" and "Game (total)" - two rows that looked like consecutive
 * stages and were in fact the same stage counted over two populations.
 */
export interface FunnelTables {
  /**
   * Requests for the landing page, all visitors - context for the 'all' view, never a stage and
   * never a percentage base. Undefined in a platform view.
   */
  landingRequests?: number;
  landingPage: ProgressStatistic[];
  game: ProgressStatistic[];
}

export const LANDING_SEEN = 'Landing page seen';
export const PLAY_CLICKED = 'Play Now clicked';
export const LANDING_GAME_OPENED = 'Game opened';
export const GAME_OPENED = 'Game opened (incl. direct links)';
export const ENGINE_RUNNING = 'Engine running';
export const BASE_CREATED = 'Initial base created';

export function createStatistics(trackingContainerAnalyzer: TrackingContainerAnalyzer,
                                 questInfo: (questId: number) => QuestRowInfo | undefined = () => undefined): FunnelTables {
  const homeCount = trackingContainerAnalyzer.countHome();
  const gameCount = trackingContainerAnalyzer.countGame();
  // One stage between opening the game page and building a base: the engine is up and the player
  // could play. The former User created / Engine init / Engine started rows tracked internals of
  // the boot sequence, not whether anybody got that far.
  const gameStarted = trackingContainerAnalyzer.countEngineRunning();
  const baseCreated = trackingContainerAnalyzer.getBaseCreatedUserIds().length;
  return {
    // Shown in the 'all' view only: LANDING is recorded without the pixel and so describes a wider
    // population than every row of either table.
    landingRequests: trackingContainerAnalyzer.getView() === 'all' ? trackingContainerAnalyzer.countLanding() : undefined,
    landingPage: [
      new ProgressStatistic(LANDING_SEEN, homeCount),
      // Both of the next two are measured against the page rather than chained to each other.
      // Chaining the game row to the click would be the tidier funnel, but every period before the
      // click was tracked has none at all, and that row would lose its percentage there - the one
      // number this table is actually read for. The click beacon can also be lost in the
      // navigation it starts, so the game row may be the larger one.
      new ProgressStatistic(PLAY_CLICKED, trackingContainerAnalyzer.countPlayClicked(), homeCount, LANDING_SEEN),
      new ProgressStatistic(LANDING_GAME_OPENED, trackingContainerAnalyzer.countGameFromHome(), homeCount, LANDING_SEEN)
    ],
    game: [
      // No percentage: the start of this table. A visitor who arrived over a plain link fires no
      // pixel and produces no /game record either - the server only writes one when the url
      // carries a query string - so their game visit is known from the startup records alone, and
      // there is no landing page number they could ever be a share of.
      new ProgressStatistic(GAME_OPENED, gameCount),
      new ProgressStatistic(ENGINE_RUNNING, gameStarted, gameCount, GAME_OPENED),
      new ProgressStatistic(BASE_CREATED, baseCreated, gameStarted, ENGINE_RUNNING),
      ...trackingContainerAnalyzer.generateLevelQuestStatistics(baseCreated, questInfo)
    ]
  };
}

export class ProgressStatistic {
  percent?: number;

  /**
   * The reference is whatever population this row is a share of - usually the stage above it, but
   * not always: the quest rows are shares of the level they belong to. Which one it is was never
   * written next to the number, so the table read as a funnel where it was not one; referenceName
   * says it.
   */
  constructor(public readonly name: String, public readonly count: number, reference?: number,
              public readonly referenceName?: string) {
    if (reference !== undefined && reference > 0) {
      this.percent = Math.round(count / reference * 100);
    }
  }
}
