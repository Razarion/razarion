import {
  PageRequest,
  PageRequestType,
  TrackingContainer,
  UserActivity,
  UserActivityType
} from '../../generated/razarion-share';
import {ProgressStatistic} from './progress-statistic';

export class TrackingContainerAnalyzer {
  private trackingContainer!: TrackingContainer;

  setTrackingContainer(trackingContainer: TrackingContainer) {
    this.trackingContainer = trackingContainer;
  }

  getDistinctHomePageRequests(): PageRequest[] {
    const seenRdtCids = new Set<string>();
    const result: PageRequest[] = [];

    for (const pageRequest of this.trackingContainer.pageRequests) {
      if (pageRequest.pageRequestType !== PageRequestType.HOME) {
        continue;
      }

      if (seenRdtCids.has(pageRequest.rdtCid)) {
        continue;
      }

      seenRdtCids.add(pageRequest.rdtCid);
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
      return pageRequest.pageRequestType === PageRequestType.GAME && pageRequest.rdtCid === homePageRequest.rdtCid;
    });
  }

  getGameEngineInits() {
    return this.getGamePageRequests().filter(pageRequest => this.getStartupTaskJsonByRdtCid(pageRequest.rdtCid).length > 0)
  }

  getStartupTerminatedJsons() {
    return this.getGamePageRequests().filter(pageRequest => this.getStartupTerminatedJsonByRdtCid(pageRequest.rdtCid).length > 0)
  }

  getUserCreated() {
    return this.getGamePageRequests().filter(pageRequest => this.getUserCreatedByHttpSessionId(pageRequest.httpSessionId).length > 0)
  }

  getBaseCreated() {
    let baseCreated: UserActivity[] = []
    this.getGamePageRequests().forEach(pageRequest => {
      const userActivities = this.getUserCreatedByHttpSessionId(pageRequest.httpSessionId);
      if (userActivities != null && userActivities.length > 0) {
        if (this.getBaseCreatedByUserId(userActivities[0].userId)) {
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

  private getStartupTaskJsonByRdtCid(rdtCid: string) {
    return this.trackingContainer.startupTaskJsons.filter(startupTaskJson => startupTaskJson.rdtCid === rdtCid);
  }

  private getStartupTerminatedJsonByRdtCid(rdtCid: string) {
    return this.trackingContainer.startupTerminatedJson.filter(startupTerminatedJson => startupTerminatedJson.rdtCid === rdtCid);
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
