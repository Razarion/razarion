package com.btxtech.shared.rest;

import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.gameengine.datatypes.packets.UnlockResultInfo;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Created by Beat
 * on 23.09.2017.
 */
@Path(CommonUrl.UNLOCK_PROVIDER_PATH)
public interface UnlockProvider {
    @POST
    @Path("unlockViaCrystals/{levelUnlockConfigId}")
    @Produces(MediaType.APPLICATION_JSON)
    UnlockResultInfo unlockViaCrystals(@PathParam("levelUnlockConfigId") int levelUnlockConfigId);

}
