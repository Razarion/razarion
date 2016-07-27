package com.btxtech.shared.gameengine;

import com.btxtech.shared.gameengine.datatypes.command.BaseCommand;

import javax.inject.Singleton;

/**
 * Created by Beat
 * 18.07.2016.
 */
@Singleton
public class UserTrackingService {
    // TODO void saveBrowserDetails(DbSessionDetail dbSessionDetail);

    // TODO void pageAccess(String pageName, String additional);

    // TODO List<SessionOverviewDto> getSessionOverviewDtos(UserTrackingFilter filter);

    // TODO List<SessionOverviewDto> getSessionOverviewDtos(User user);

    // TODO SessionDetailDto getSessionDetailDto(String sessionId);

    // TODO List<User> getNewUsers(NewUserTrackingFilter newUserTrackingFilter);

    public void saveUserCommand(BaseCommand baseCommand) {
        throw new UnsupportedOperationException();
    }

    // TODO void onUserCreated(User user);

    // TODO void onUserVerified(User user);

    // TODO void onUnverifiedUserRemoved(User user);

    // TODO void onUserLoggedIn(User user, UserState userState);

    // TODO void onUserLoggedOut(User user);

    // TODO void onPasswordForgotRequested(User user, String forgotPasswordUuid);

    // TODO void onPasswordForgotRequestedRemoved(DbForgotPassword dbForgotPassword);

    // TODO void onPasswordReset(User user, String forgotPasswordUuid);

    // TODO void onBaseCreated(User user, String baseName);

    // TODO void onBaseDefeated(User user, Base base);

    // TODO void onBaseSurrender(User user, Base base);

    // TODO void onUserEnterGame(User user);

    // TODO void onUserLeftGame(User user);

    // TODO void onUserLeftGameNoSession(User user);

    // TODO void trackChatMessage(ChatMessage chatMessage);

    // TODO void trackWindowsClosed(String startUUid);

    // TODO void onJavaScriptDetected(Boolean html5Support);

    // TODO boolean isJavaScriptDetected();

    // TODO boolean isHtml5Support();

    // TODO void onTutorialProgressChanged(TutorialConfig.TYPE type, String startUuid, int taskId, int dbId, String tutorialTaskName, long duration, long clientTimeStamp);

    // TODO void onEventTrackingStart(EventTrackingStart eventTrackingStart);

    // TODO void onEventTrackerItems(Collection<EventTrackingItem> eventTrackingItems, Collection<SyncItemInfo> syncItemInfos, Collection<SelectionTrackingItem> selectionTrackingItems, Collection<TerrainScrollTracking> terrainScrollTrackings, Collection<BrowserWindowTracking> browserWindowTrackings, Collection<DialogTracking> dialogTrackings);

    // TODO List<DbEventTrackingItem> getDbEventTrackingItem(String startUuid);

    // TODO DbEventTrackingStart getDbEventTrackingStart(String startUuid);

    // TODO void saveStartupTask(StartupTaskInfo startupTaskInfo, String startUuid, Integer levelTaskId);

    // TODO void saveStartupTerminated(boolean successful, long totalTime, String startUuid, Integer levelTaskId);

    // TODO RealGameTrackingInfo getGameTracking(GameHistoryFrame gameHistoryFrame, GameHistoryFilter gameHistoryFilter);

    // TODO TutorialTrackingInfo getTutorialTrackingInfo(LifecycleTrackingInfo lifecycleTrackingInfo);

    // TODO List<DbSelectionTrackingItem> getDbSelectionTrackingItems(String startUuid);

    // TODO List<DbSyncItemInfo> getDbSyncItemInfos(String startUuid);

    // TODO List<DbScrollTrackingItem> getDbScrollTrackingItems(String startUuid);

    // TODO List<DbBrowserWindowTracking> getDbBrowserWindowTrackings(String startUuid);

    // TODO List<DbDialogTracking> getDbDialogTrackings(String startUuid);

    // TODO LifecycleTrackingInfo getLifecycleTrackingInfo(String startUuid);

    // TODO long calculateInGameTime(User user);

    // TODO int getLoginCount(User user);

    // TODO List<NewUserDailyDto> getNewUserDailyDto(NewUserDailyTrackingFilter newUserDailyTrackingFilter);

    // TODO TutorialStatisticDto getTutorialStatistic(QuestTrackingFilter questTrackingFilter);

    // TODO QuestStatisticDto getQuestStatistic(QuestTrackingFilter questTrackingFilter);

}
