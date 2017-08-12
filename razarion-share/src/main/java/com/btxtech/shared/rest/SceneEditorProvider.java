package com.btxtech.shared.rest;

import com.btxtech.shared.dto.ObjectNameId;

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
 * 24.12.2016.
 */
@Path(RestUrl.SCENE_EDITOR_PATH)
public interface SceneEditorProvider {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("readSceneConfigObjectNameIds/{gameUiControlConfigId}")
    List<ObjectNameId> readSceneConfigObjectNameIds(@PathParam("gameUiControlConfigId") int gameUiControlConfigId);

    @POST
    @Path("createSceneConfig/{gameUiControlConfigId}")
    @Produces(MediaType.APPLICATION_JSON)
    void createSceneConfig(@PathParam("gameUiControlConfigId") int gameUiControlConfigId);

    @PUT
    @Path("swapQuestConfig/{gameUiControlConfigId}/{index1}/{index2}")
    @Consumes(MediaType.APPLICATION_JSON)
    void swapSceneConfig(@PathParam("gameUiControlConfigId") int gameUiControlConfigId, @PathParam("index1") int index1, @PathParam("index2") int index2);

    @DELETE
    @Path("deleteSceneConfig/{gameUiControlConfigId}/{sceneConfigId}")
    @Consumes(MediaType.TEXT_PLAIN)
    void deleteSceneConfig(@PathParam("gameUiControlConfigId") int gameUiControlConfigId, @PathParam("sceneConfigId") int sceneConfigId);
}
