package com.btxtech.shared.rest;

import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import org.dominokit.rest.shared.request.service.annotations.RequestFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * Created by Beat
 * on 29.08.2017.
 */
@Path(CommonUrl.QUEST_CONTROLLER_PATH)
@RequestFactory
public interface QuestController {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("readMyOpenQuests")
    List<QuestConfig> readMyOpenQuests();

    @POST
    @Path("activateQuest/{id}")
    void activateQuest(@PathParam("id") int questId);

    @POST
    @Path("activateNextPossibleQuest")
    void activateNextPossibleQuest();
}
