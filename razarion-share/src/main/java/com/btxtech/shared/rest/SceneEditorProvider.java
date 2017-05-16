package com.btxtech.shared.rest;

import com.btxtech.shared.dto.AudioItemConfig;
import com.btxtech.shared.dto.SceneConfig;

import javax.ws.rs.Consumes;
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

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("{gameUiControlConfigId}")
    void saveAllScenes(@PathParam("gameUiControlConfigId") int gameUiControlConfigId, List<SceneConfig> sceneConfigs);
}
