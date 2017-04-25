package com.btxtech.shared.rest;

import com.btxtech.shared.dto.FacebookUserLoginInfo;
import com.btxtech.shared.dto.GameUiControlConfig;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Created by Beat
 * 25.04.2017.
 */
@Path(RestUrl.PLANET_PROVIDER)
public interface PlanetProvider {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    PlanetConfig loadWarmPlanetConfig();

}
