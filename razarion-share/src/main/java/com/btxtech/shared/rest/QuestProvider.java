package com.btxtech.shared.rest;

import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * Created by Beat
 * on 29.08.2017.
 */
@Path(CommonUrl.QUEST_PROVIDER_PATH)
public interface QuestProvider {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("readMyOpenQuests")
    List<QuestConfig> readMyOpenQuests();

    @POST
    @Path("activateQuest")
    void activateQuest(int questId);

    @POST
    @Path("activateNextPossibleQuest")
    void activateNextPossibleQuest();
}
