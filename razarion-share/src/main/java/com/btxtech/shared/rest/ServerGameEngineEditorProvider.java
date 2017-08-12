package com.btxtech.shared.rest;

import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.dto.ResourceRegionConfig;
import com.btxtech.shared.dto.ServerLevelQuestConfig;
import com.btxtech.shared.dto.StartRegionConfig;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * Created by Beat
 * on 28.07.2017.
 */
@Path(RestUrl.SERVER_GAME_ENGINE_EDITOR_PROVIDER_PATH)
public interface ServerGameEngineEditorProvider {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("readStartRegionObjectNameIds")
    List<ObjectNameId> readStartRegionObjectNameIds();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("readStartRegionConfig/{id}")
    StartRegionConfig readStartRegionConfig(@PathParam("id") int id);

    @POST
    @Path("createStartRegionConfig")
    @Produces(MediaType.APPLICATION_JSON)
    StartRegionConfig createStartRegionConfig();

    @PUT
    @Path("updateStartRegionConfig")
    @Consumes(MediaType.APPLICATION_JSON)
    void updateStartRegionConfig(StartRegionConfig startRegionConfig);

    @DELETE
    @Path("deleteStartRegionConfig")
    @Consumes(MediaType.APPLICATION_JSON)
    void deleteStartRegionConfig(int id);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("readLevelQuestConfigObjectNameIds")
    List<ObjectNameId> readLevelQuestConfigObjectNameIds();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("readLevelQuestConfig/{id}")
    ServerLevelQuestConfig readLevelQuestConfig(@PathParam("id") int id);

    @POST
    @Path("createLevelQuestConfig")
    @Produces(MediaType.APPLICATION_JSON)
    ServerLevelQuestConfig createLevelQuestConfig();

    @PUT
    @Path("updateLevelQuestConfig")
    @Consumes(MediaType.APPLICATION_JSON)
    void updateLevelQuestConfig(ServerLevelQuestConfig serverLevelQuestConfig);

    @DELETE
    @Path("deleteLevelQuestConfig")
    @Consumes(MediaType.APPLICATION_JSON)
    void deleteLevelQuestConfig(int id);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("readQuestConfigObjectNameIds/{levelQuestId}")
    List<ObjectNameId> readQuestConfigObjectNameIds(@PathParam("levelQuestId") int levelQuestId);

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("createQuestConfig/{levelQuestId}")
    QuestConfig createQuestConfig(@PathParam("levelQuestId") int levelQuestId);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("readQuestConfig/{levelQuestId}/{questId}")
    QuestConfig readQuestConfig(@PathParam("levelQuestId") int levelQuestId, @PathParam("questId") int questId);

    @PUT
    @Path("updateLevelQuestConfig/{levelQuestId}")
    @Consumes(MediaType.APPLICATION_JSON)
    void updateQuestConfig(@PathParam("levelQuestId") int levelQuestId, QuestConfig questConfig);

    @DELETE
    @Path("deleteQuestConfig/{levelQuestId}/{questId}")
    @Consumes(MediaType.APPLICATION_JSON)
    void deleteQuestConfig(@PathParam("levelQuestId") int levelQuestId, @PathParam("questId") int questId);

    @PUT
    @Path("swapQuestConfig/{levelQuestId}/{index1}/{index2}")
    @Consumes(MediaType.APPLICATION_JSON)
    void swapQuestConfig(@PathParam("levelQuestId") int levelQuestId, @PathParam("index1") int index1, @PathParam("index2") int index2);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("readResourceRegionObjectNameIds")
    List<ObjectNameId> readResourceRegionObjectNameIds();

    @POST
    @Path("createResourceRegionConfig")
    @Produces(MediaType.APPLICATION_JSON)
    ResourceRegionConfig createResourceRegionConfig();

    @DELETE
    @Path("deleteResourceRegionConfig")
    @Consumes(MediaType.APPLICATION_JSON)
    void deleteResourceRegionConfig(int resourceRegionConfigId);

    @PUT
    @Path("updateResourceRegionConfig")
    @Consumes(MediaType.APPLICATION_JSON)
    void updateResourceRegionConfig(ResourceRegionConfig resourceRegionConfig);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("readResourceRegionConfig/{resourceRegionConfigId}")
    ResourceRegionConfig readResourceRegionConfig(@PathParam("resourceRegionConfigId") int resourceRegionConfigId);
}