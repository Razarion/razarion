package com.btxtech.server.rest;

import com.btxtech.server.marketing.MarketingService;
import com.btxtech.shared.rest.RestUrl;
import com.btxtech.shared.system.ExceptionHandler;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by Beat
 * 22.03.2017.
 */
@Path(RestUrl.MARKETING)
public class Marketing {
    @Inject
    private MarketingService marketingService;
    @Inject
    private ExceptionHandler exceptionHandler;

    @GET
    @Consumes(MediaType.TEXT_PLAIN)
    @Path(RestUrl.FB_CLICK_TRACKING_TAGS_RECEIVER)
    public Response clickTrackerReceiver(@QueryParam("fb_adgroup_id") String adId) {
        try {
            marketingService.onClickTrackerReceived(adId);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
        return Response.status(HttpServletResponse.SC_OK).build();
    }

}
