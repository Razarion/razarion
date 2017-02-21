package com.btxtech.server;

import javax.servlet.annotation.WebServlet;

/**
 * Created by Beat
 * 18.04.2015.
 */
@Deprecated
@WebServlet(urlPatterns = "/razarion_client/remote_logging")
public class ClientGwtSuperDevModeLoggingServlet extends AbstractGwtSuperDevModeLoggingServlet {
    private final static String MODULE_NAME = "com.btxtech.Razarion-Client";
    private final static String SIMPLE_MODULE_NAME = "razarion_client";

    @Override
    protected String getModuleName() {
        return MODULE_NAME;
    }

    @Override
    protected String getSimpleModuleName() {
        return SIMPLE_MODULE_NAME;
    }
}
