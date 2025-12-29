import {
  PageRequest,
  PageRequestType,
  TrackingContainer,
  UserActivity,
  UserActivityType
} from '../../generated/razarion-share';

export class TrackingContainerAnalyzer {
  private trackingContainer!: TrackingContainer;

  setTrackingContainer(trackingContainer: TrackingContainer) {
    this.trackingContainer = trackingContainer;
  }

  getHomePageRequests() {
    return this.trackingContainer.pageRequests.filter((pageRequest: PageRequest) => {
      return pageRequest.pageRequestType === PageRequestType.HOME;
    });
  }

  getGamePageRequests() {
    return this.trackingContainer.pageRequests.filter((pageRequest: PageRequest) => {
      return pageRequest.pageRequestType === PageRequestType.HOME && this.getGames4Home(pageRequest).length > 0;
    });
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
        if (this.getUserBaseCreatedByUserId(userActivities[0].userId)) {
          baseCreated.push(userActivities[0]);
        }
      }
    });
    return baseCreated;
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

  private getUserBaseCreatedByUserId(userId: string) {
    return this.trackingContainer.userActivities.filter(userActivity => userActivity.userActivityType === UserActivityType.BASE_CREATED && userActivity.userId === userId);
  }
}
