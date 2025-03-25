package com.btxtech.shared.rest;

import com.btxtech.shared.CommonUrl;
import com.google.gwt.core.shared.GwtIncompatible;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

/**
 * Created by Beat
 * 15.06.2016.
 */
@ApplicationPath(CommonUrl.APPLICATION_PATH)
@GwtIncompatible
public class RestApplication extends Application {
}
