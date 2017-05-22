package com.btxtech.shared.rest;

import com.btxtech.shared.dto.ColdGameUiControlConfig;
import com.btxtech.shared.dto.WarmGameUiControlConfig;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Created by Beat
 * 06.07.2016.
 */
@Path(RestUrl.GAME_UI_CONTROL_PATH)
public interface GameUiControlProvider {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path(RestUrl.COLD)
    ColdGameUiControlConfig loadGameUiControlConfig();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path(RestUrl.WARM)
    WarmGameUiControlConfig loadWarmGameUiControlConfig();
}
