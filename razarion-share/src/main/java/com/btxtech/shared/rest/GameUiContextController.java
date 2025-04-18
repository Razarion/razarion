package com.btxtech.shared.rest;

import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.dto.ColdGameUiContext;
import com.btxtech.shared.dto.GameUiControlInput;
import com.btxtech.shared.dto.WarmGameUiContext;
import org.dominokit.rest.shared.request.service.annotations.RequestFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * Created by Beat
 * 06.07.2016.
 */
@Path(CommonUrl.GAME_UI_CONTEXT_CONTROL_PATH)
@RequestFactory
public interface GameUiContextController {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path(CommonUrl.COLD)
    ColdGameUiContext loadColdGameUiContext(GameUiControlInput gameUiControlInput);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path(CommonUrl.WARM)
    WarmGameUiContext loadWarmGameUiContext();
}
