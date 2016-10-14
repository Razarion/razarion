package com.btxtech.shared.rest;

import com.btxtech.shared.dto.StoryboardConfig;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Created by Beat
 * 06.07.2016.
 */
@Path(RestUrl.STORYBOARD_SERVICE_PATH)
public interface StoryboardProvider {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    StoryboardConfig loadStoryboard();
}
