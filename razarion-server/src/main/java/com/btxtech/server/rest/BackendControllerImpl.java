package com.btxtech.server.rest;

import com.btxtech.server.mgmt.ItemTrackingDescription;
import com.btxtech.server.mgmt.OnlineInfo;
import com.btxtech.server.mgmt.ServerMgmt;
import com.btxtech.server.mgmt.UserBackendInfo;
import com.btxtech.server.persistence.history.HistoryPersistence;
import com.btxtech.server.persistence.history.UserHistoryEntry;
import com.btxtech.server.persistence.item.ItemTrackerAccess;
import com.btxtech.server.persistence.item.ItemTracking;
import com.btxtech.server.persistence.item.ItemTrackingSearch;
import com.btxtech.server.persistence.server.ServerGameEngineCrudPersistence;
import com.btxtech.server.persistence.tracker.SearchConfig;
import com.btxtech.server.persistence.tracker.SessionDetail;
import com.btxtech.server.persistence.tracker.SessionTracker;
import com.btxtech.server.persistence.tracker.TrackerPersistence;
import com.btxtech.server.user.NewUser;
import com.btxtech.server.user.SecurityCheck;
import com.btxtech.server.user.SecurityException;
import com.btxtech.server.user.UserService;
import com.btxtech.shared.dto.StartupTaskJson;
import com.btxtech.shared.dto.StartupTerminatedJson;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.rest.BackendController;
import com.btxtech.shared.system.ExceptionHandler;

import javax.inject.Inject;
import javax.ws.rs.ForbiddenException;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Beat on 29.05.2017.
 */
public class BackendControllerImpl implements BackendController {
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private TrackerPersistence trackerPersistence;

    @Override
    @SecurityCheck
    public List<StartupTerminatedJson> loadStartupTerminatedJson() {
        try {
            return trackerPersistence.loadStartupTerminatedJson();
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }

    @Override
    @SecurityCheck
    public List<StartupTaskJson> loadStartupTaskJson(String gameSessionUuid) {
        try {
            return trackerPersistence.loadStartupTaskJson(gameSessionUuid);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }

}
