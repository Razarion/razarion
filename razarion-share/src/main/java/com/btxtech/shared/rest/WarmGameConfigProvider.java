package com.btxtech.shared.rest;

import com.btxtech.shared.dto.WarmGameUiControlConfig;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Created by Beat
 * 25.04.2017.
 */
@Path(RestUrl.WARM_GAME_CONFIG_PROVIDER)
public interface WarmGameConfigProvider {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    WarmGameUiControlConfig loadWarmGameConfigTask();

}
