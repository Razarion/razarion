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

  getGames4Home(homePageRequest: PageRequest) {
    return this.trackingContainer.pageRequests.filter((pageRequest: PageRequest) => {
      return pageRequest.pageRequestType === PageRequestType.GAME && this.clickId(pageRequest) === this.clickId(homePageRequest);
    });
  }

  getGameEngineInits() {
    return this.getGamePageRequests().filter(pageRequest => this.getStartupTaskJsonByClickId(this.clickId(pageRequest)).length > 0)
  }

  getStartupTerminatedJsons() {
    return this.getGamePageRequests().filter(pageRequest => this.getStartupTerminatedJsonByClickId(this.clickId(pageRequest)).length > 0)
  }

  getUserCreated() {
    return this.getGamePageRequests().filter(pageRequest => this.getUserCreatedByHttpSessionId(pageRequest.httpSessionId).length > 0)
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

  generateLevelQuestStatistics(lastCount: number) {
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
    for (let levelNumber = 1; levelNumber <= maxLevelNumber; levelNumber++) {
      const levelUpCount = levels[levelNumber];
      if (levelUpCount !== undefined) {
        progressStatistics.push(new ProgressStatistic(`Level ${levelNumber}`, levelUpCount, lastCount));
        lastCount = levelUpCount;
      }
      const levelQuestMap = levelQuests.get(levelNumber);
      if (levelQuestMap !== undefined) {
        let tmpProgressStatistics: ProgressStatistic[] = []
        levelQuestMap.forEach((count, questId) => {
          if (count !== undefined) {
            tmpProgressStatistics.push(new ProgressStatistic(`Quest ${questId} (Level ${levelNumber})`, count, lastCount));
            lastCount = count;
          }
        });
        tmpProgressStatistics.sort((a, b) => b.count - a.count);
        progressStatistics.push(...tmpProgressStatistics);
      }
    }

    return progressStatistics;
  }

  private getStartupTaskJsonByClickId(clickId: string) {
    return this.trackingContainer.startupTaskJsons.filter(startupTaskJson => startupTaskJson[this.clickIdField] === clickId);
  }

  private getStartupTerminatedJsonByClickId(clickId: string) {
    return this.trackingContainer.startupTerminatedJson.filter(startupTerminatedJson => startupTerminatedJson[this.clickIdField] === clickId);
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
