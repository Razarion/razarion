package com.btxtech.server;

import javax.servlet.annotation.WebServlet;

/**
 * Created by Beat
 * 18.04.2015.
 */
@WebServlet(urlPatterns = "/razarion_client_worker/remote_logging")
public class WorkerGwtSuperDevModeLoggingServlet extends AbstractGwtSuperDevModeLoggingServlet {
    private final static String MODULE_NAME = "com.btxtech.Razarion-Client-Worker";
    private final static String SIMPLE_MODULE_NAME = "razarion_client_worker";

    @Override
    protected String getModuleName() {
        return MODULE_NAME;
    }

    @Override
    protected String getSimpleModuleName() {
        return SIMPLE_MODULE_NAME;
    }
}
