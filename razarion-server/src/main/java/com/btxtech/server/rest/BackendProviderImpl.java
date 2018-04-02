package com.btxtech.server.rest;

import com.btxtech.server.mgmt.OnlineInfo;
import com.btxtech.server.mgmt.ServerMgmt;
import com.btxtech.server.mgmt.UserBackendInfo;
import com.btxtech.server.persistence.history.HistoryPersistence;
import com.btxtech.server.persistence.history.UserHistoryEntry;
import com.btxtech.server.persistence.item.ItemTrackerAccess;
import com.btxtech.server.persistence.item.ItemTracking;
import com.btxtech.server.persistence.item.ItemTrackingSearch;
import com.btxtech.server.persistence.tracker.SearchConfig;
import com.btxtech.server.persistence.tracker.SessionDetail;
import com.btxtech.server.persistence.tracker.SessionTracker;
import com.btxtech.server.persistence.tracker.TrackerPersistence;
import com.btxtech.server.user.NewUser;
import com.btxtech.server.user.SecurityException;
import com.btxtech.server.user.UserService;
import com.btxtech.shared.Constants;
import com.btxtech.shared.system.ExceptionHandler;

import javax.inject.Inject;
import javax.ws.rs.ForbiddenException;
import java.util.List;

/**
 * Created by Beat on 29.05.2017.
 */
public class BackendProviderImpl implements BackendProvider {
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private TrackerPersistence trackerPersistence;
    @Inject
    private UserService userService;
    @Inject
    private HistoryPersistence historyPersistence;
    @Inject
    private ItemTrackerAccess itemTrackerAccess;
    @Inject
    private ServerMgmt serverMgmt;

    @Override
    public List<OnlineInfo> loadAllOnlines() {
        try {
            return serverMgmt.loadAllOnlines();
        } catch (SecurityException t) {
            exceptionHandler.handleException(t);
            throw new ForbiddenException(); // Unfortunately, resteasy log this exception
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }

    @Override
    public UserBackendInfo loadBackendUserInfo(int playerId) {
        try {
            return serverMgmt.loadBackendUserInfo(playerId);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }

    @Override
    public UserBackendInfo removeCompletedQuest(int playerId, int questId) {
        try {
            return serverMgmt.removeCompletedQuest(playerId, questId);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }

    @Override
    public UserBackendInfo addCompletedQuest(int playerId, int questId) {
        try {
            return serverMgmt.addCompletedQuest(playerId, questId);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }

    @Override
    public UserBackendInfo setLevelNumber(int playerId, int levelNumber) {
        try {
            return serverMgmt.setLevelNumber(playerId, levelNumber);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }

    @Override
    public UserBackendInfo setXp(int playerId, int xp) {
        try {
            return serverMgmt.setXp(playerId, xp);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }

    @Override
    public UserBackendInfo setCrystals(int playerId, int crystals) {
        try {
            return serverMgmt.setCrystals(playerId, crystals);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }

    @Override
    public UserBackendInfo removeUnlocked(int playerId, int unlockedBackendInfoId) {
        try {
            return serverMgmt.removeUnlockedItem(playerId, unlockedBackendInfoId);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }

    @Override
    public List<SessionTracker> sessions(SearchConfig searchConfig) {
        try {
            return trackerPersistence.readSessionTracking(searchConfig);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }

    @Override
    public SessionDetail sessiondetail(String id) {
        try {
            return trackerPersistence.readSessionDetail(id);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }

    @Override
    public List<NewUser> newUsers() {
        try {
            return userService.findNewUsers();
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }

    @Override
    public List<UserHistoryEntry> userHistory() {
        try {
            return historyPersistence.readLoginHistory();
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }

    @Override
    public List<ItemTracking> itemHistory(ItemTrackingSearch itemTrackingSearch) {
        try {
            return itemTrackerAccess.read(itemTrackingSearch);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }

    @Override
    public void sendRestartLifecycle() {
        try {
            serverMgmt.sendRestartLifecycle();
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }
}
