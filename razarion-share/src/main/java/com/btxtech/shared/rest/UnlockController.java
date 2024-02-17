package com.btxtech.shared.rest;

import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.gameengine.datatypes.config.LevelUnlockConfig;
import com.btxtech.shared.gameengine.datatypes.packets.UnlockResultInfo;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * Created by Beat
 * on 23.09.2017.
 */
@Path(CommonUrl.UNLOCK_CONTROLLER_PATH)
public interface UnlockController {
    @POST
    @Path("unlockViaCrystals/{levelUnlockConfigId}")
    @Produces(MediaType.APPLICATION_JSON)
    UnlockResultInfo unlockViaCrystals(@PathParam("levelUnlockConfigId") int levelUnlockConfigId);

    @GET
    @Path("available-level-unlockConfigs")
    @Produces(MediaType.APPLICATION_JSON)
    List<LevelUnlockConfig> getAvailableLevelUnlockConfigs();

}
