package com.btxtech.server.rest;

import com.btxtech.server.persistence.tracker.TrackerPersistence;
import com.btxtech.server.user.SecurityCheck;
import com.btxtech.shared.dto.StartupTaskJson;
import com.btxtech.shared.dto.StartupTerminatedJson;
import com.btxtech.shared.rest.BackendController;
import com.btxtech.shared.system.ExceptionHandler;

import javax.inject.Inject;
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
