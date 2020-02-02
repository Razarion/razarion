package com.btxtech.shared.rest;

import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.dto.ColdGameUiControlConfig;
import com.btxtech.shared.dto.GameUiControlInput;
import com.btxtech.shared.dto.WarmGameUiControlConfig;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Created by Beat
 * 06.07.2016.
 */
@Path(CommonUrl.GAME_UI_CONTROL_PATH)
public interface GameUiControlController {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path(CommonUrl.COLD)
    ColdGameUiControlConfig loadGameUiControlConfig(GameUiControlInput gameUiControlInput);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path(CommonUrl.WARM)
    WarmGameUiControlConfig loadWarmGameUiControlConfig();
}
