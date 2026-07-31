import {
  PageRequest,
  PageRequestType,
  TrackingContainer,
  UserActivity,
  UserActivityType
} from '../../generated/razarion-share';
import {ProgressStatistic} from './progress-statistic';

/**
 * The click-id field the funnel is correlated by. Reddit tags its links with rdt_cid (stored as
 * rdtCid), X (Twitter) with twclid. Switching this recomputes the whole funnel for that platform.
 */
export type ClickIdField = 'rdtCid' | 'twclid';

export class TrackingContainerAnalyzer {
  private trackingContainer!: TrackingContainer;
  private clickIdField: ClickIdField = 'rdtCid';

  setTrackingContainer(trackingContainer: TrackingContainer) {
    this.trackingContainer = trackingContainer;
  }

  setClickIdField(clickIdField: ClickIdField) {
    this.clickIdField = clickIdField;
  }

  private clickId(pageRequest: PageRequest): string {
    return pageRequest[this.clickIdField];
  }

  getDistinctHomePageRequests(): PageRequest[] {
    const seenClickIds = new Set<string>();
    const result: PageRequest[] = [];

    for (const pageRequest of this.trackingContainer.pageRequests) {
      if (pageRequest.pageRequestType !== PageRequestType.HOME) {
        continue;
      }

      if (seenClickIds.has(this.clickId(pageRequest))) {
        continue;
      }

      seenClickIds.add(this.clickId(pageRequest));
      result.push(pageRequest);
    }

    return result;
  }

  getGamePageRequests() {
    return this.getDistinctHomePageRequests().filter(pageRequest => {
      return this.getGames4Home(pageRequest).length > 0
    })
  }

  /**
   * Home visits whose visitor pressed "Play Now". Correlated by click id like every other stage
   * here, so it describes the same population as the Home row it is measured against.
   */
  getPlayClickedPageRequests() {
    return this.getDistinctHomePageRequests().filter(pageRequest => {
      return this.getPlayClicks4Home(pageRequest).length > 0
    })
  }

  getPlayClicks4Home(homePageRequest: PageRequest) {
    return this.trackingContainer.pageRequests.filter((pageRequest: PageRequest) => {
      return pageRequest.pageRequestType === PageRequestType.HOME_PLAY_CLICKED && this.clickId(pageRequest) === this.clickId(homePageRequest);
    });
  }

  getGames4Home(homePageRequest: PageRequest) {
    return this.trackingContainer.pageRequests.filter((pageRequest: PageRequest) => {
      return pageRequest.pageRequestType === PageRequestType.GAME && this.clickId(pageRequest) === this.clickId(homePageRequest);
    });
  }

  /**
   * The only startup stage worth a funnel row: the client booted through, so the engine runs and
   * the player could play. The stages before it say nothing about that - a finished startup task
   * only means the boot got going, and a terminated startup counts the failed boots too.
   */
  getGameStarted() {
    return this.getGamePageRequests().filter(pageRequest => this.getSuccessfulStartupByClickId(this.clickId(pageRequest)).length > 0)
  }

  getBaseCreated() {
    const seenUserIds = new Set<string>();
    let baseCreated: UserActivity[] = []
    this.getGamePageRequests().forEach(pageRequest => {
      const userActivities = this.getUserCreatedByHttpSessionId(pageRequest.httpSessionId);
      if (userActivities != null && userActivities.length > 0) {
        const userId = userActivities[0].userId;
        if (seenUserIds.has(userId)) {
          // Count only the initial base per user (a user who lost and re-created a base must not be counted again)
          return;
        }
        if (this.getBaseCreatedByUserId(userId).length > 0) {
          seenUserIds.add(userId);
          baseCreated.push(userActivities[0]);
        }
      }
    });
    return baseCreated;
  }

  /**
   * The level and quest rows below the funnel.
   * <p>
   * Levels are a chain - nobody reaches level 3 without level 2 - so each level is measured
   * against the one before it. The quests inside a level are not: a player passes them in their
   * own order, and the rows are shown by size rather than by that order. Every quest is therefore
   * measured against the players who reached its level, which is a fixed reference and reads the
   * same however the rows are sorted.
   * <p>
   * They used to be chained to each other instead, in the order the quest ids happened to appear
   * in the activity list, and then re-sorted by count for display - so a row's percentage referred
   * to whichever quest came before it in the raw data, not to the row above it. A quest passed
   * more often than its accidental predecessor showed over 100%.
   */
  generateLevelQuestStatistics(baseCreatedCount: number) {
    let levels: number[] = [];
    let levelQuests: Map<number, Map<number, number>> = new Map<number, Map<number, number>>()
    let maxLevelNumber = 0;


    this.getBaseCreated().forEach((baseCreated) => {
      this.getLevelUpsUserId(baseCreated.userId).forEach(userActivity => {
        const levelNumber = Number(userActivity.detail);
        let count = levels[levelNumber];
        if (count === undefined) {
          count = 0;
        }
        count++;
        levels[levelNumber] = count;
        if (maxLevelNumber < levelNumber) {
          maxLevelNumber = levelNumber;
        }
      });
      this.getQuestsPassedUserId(baseCreated.userId).forEach(userActivity => {
        if (userActivity.detail2 !== undefined) {
          const levelNumber = Number(userActivity.detail2);
          if (maxLevelNumber < levelNumber) {
            maxLevelNumber = levelNumber;
          }
          let questCount = levelQuests.get(levelNumber);
          if (questCount === undefined) {
            questCount = new Map<number, number>();
            levelQuests.set(levelNumber, questCount);
          }
          const questId = Number(userActivity.detail);
          let count = questCount.get(questId);
          if (count === undefined) {
            count = 0;
          }
          count++;
          questCount.set(questId, count);
        }
      });
    });

    let progressStatistics: ProgressStatistic[] = [];
    // Level 1 emits no LEVEL_UP - it comes with the first base - so the players who reached it are
    // the players who built one, and its quests are measured against that.
    let levelReached = baseCreatedCount;
    for (let levelNumber = 1; levelNumber <= maxLevelNumber; levelNumber++) {
      const levelUpCount = levels[levelNumber];
      if (levelUpCount !== undefined) {
        progressStatistics.push(new ProgressStatistic(`Level ${levelNumber}`, levelUpCount, levelReached));
        levelReached = levelUpCount;
      }
      const levelQuestMap = levelQuests.get(levelNumber);
      if (levelQuestMap !== undefined) {
        let questProgressStatistics: ProgressStatistic[] = []
        levelQuestMap.forEach((count, questId) => {
          if (count !== undefined) {
            questProgressStatistics.push(
              new ProgressStatistic(`Quest ${questId} (Level ${levelNumber})`, count, levelReached));
          }
        });
        // Display order only - it no longer moves any percentage.
        questProgressStatistics.sort((a, b) => b.count - a.count);
        progressStatistics.push(...questProgressStatistics);
      }
    }

    return progressStatistics;
  }

  private getSuccessfulStartupByClickId(clickId: string) {
    return this.trackingContainer.startupTerminatedJson.filter(startupTerminatedJson => startupTerminatedJson.successful && startupTerminatedJson[this.clickIdField] === clickId);
  }

  private getUserCreatedByHttpSessionId(httpSessionId: string) {
    return this.trackingContainer.userActivities.filter(userActivity => userActivity.userActivityType === UserActivityType.USER_CREATED && userActivity.httpSessionId === httpSessionId);
  }

  private getBaseCreatedByUserId(userId: string) {
    return this.trackingContainer.userActivities.filter(userActivity => userActivity.userActivityType === UserActivityType.BASE_CREATED && userActivity.userId === userId);
  }

  private getLevelUpsUserId(userId: string) {
    return this.trackingContainer.userActivities.filter(userActivity => userActivity.userActivityType === UserActivityType.LEVEL_UP && userActivity.userId === userId);
  }

  private getQuestsPassedUserId(userId: string) {
    return this.trackingContainer.userActivities.filter(userActivity => userActivity.userActivityType === UserActivityType.QUEST_PASSED && userActivity.userId === userId);
  }
}
