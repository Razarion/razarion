package com.btxtech.server.rest;

import com.btxtech.server.mgmt.ServerMgmt;
import com.btxtech.shared.Constants;
import com.btxtech.shared.datatypes.ServerState;
import com.btxtech.shared.rest.ServerMgmtProvider;
import com.btxtech.shared.system.ExceptionHandler;

import javax.inject.Inject;

/**
 * Created by Beat
 * on 16.02.2018.
 */
public class ServerMgmtProviderImpl implements ServerMgmtProvider {
    @Inject
    private ServerMgmt serverMgmt;
    @Inject
    private ExceptionHandler exceptionHandler;

    @Override
    // Enum as return value not working. Not proper JSON. Too many quotation marks.
    public String getServerStatus() {
        try {
            return serverMgmt.getServerState().toString();
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            return ServerState.UNKNOWN.toString();
        }
    }

    @Override
    public int getInterfaceVersion() {
        return Constants.INTERFACE_VERSION;
    }


}
