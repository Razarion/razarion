package com.btxtech.server.web;

import com.btxtech.server.mgmt.ServerMgmt;
import com.btxtech.shared.datatypes.ServerState;

import javax.inject.Inject;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;

/**
 * Created by Beat
 * on 05.03.2018.
 */
@WebServlet(urlPatterns = "/ignore", loadOnStartup = 1)
public class MonitorServlet extends HttpServlet{

    private ServerMgmt serverMgmt;

    @Inject
    public MonitorServlet(ServerMgmt serverMgmt) {
        this.serverMgmt = serverMgmt;
    }

    @Override
    public void destroy() {
        serverMgmt.setServerState(ServerState.SHUTTING_DOWN);
    }
}
