package com.btxtech.shared.rest;

import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.dto.BoxRegionConfig;
import com.btxtech.shared.dto.ResourceRegionConfig;
import com.btxtech.shared.dto.ServerGameEngineConfig;
import com.btxtech.shared.dto.ServerLevelQuestConfig;
import com.btxtech.shared.dto.StartRegionConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotConfig;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path(CommonUrl.SERVER_GAME_ENGINE_EDITOR_PATH)
public interface ServerGameEngineEditorController extends CrudController<ServerGameEngineConfig> {
    @POST
    @Path("update/resourceRegionConfig/{serverGameEngineConfigId}")
    @Consumes(MediaType.APPLICATION_JSON)
    void updateResourceRegionConfig(@PathParam("serverGameEngineConfigId") int serverGameEngineConfigId, List<ResourceRegionConfig> resourceRegionConfigs);

    @POST
    @Path("update/startRegionConfig/{serverGameEngineConfigId}")
    @Consumes(MediaType.APPLICATION_JSON)
    void updateStartRegionConfig(@PathParam("serverGameEngineConfigId") int serverGameEngineConfigId, List<StartRegionConfig> startRegionConfigs);

    @POST
    @Path("update/botConfig/{serverGameEngineConfigId}")
    @Consumes(MediaType.APPLICATION_JSON)
    void updateBotConfig(@PathParam("serverGameEngineConfigId") int serverGameEngineConfigId, List<BotConfig> botConfigs);

    @POST
    @Path("update/serverLevelQuestConfig/{serverGameEngineConfigId}")
    @Consumes(MediaType.APPLICATION_JSON)
    void updateServerLevelQuestConfig(@PathParam("serverGameEngineConfigId") int serverGameEngineConfigId, List<ServerLevelQuestConfig> serverLevelQuestConfigs);

    @POST
    @Path("update/boxRegionConfig/{serverGameEngineConfigId}")
    @Consumes(MediaType.APPLICATION_JSON)
    void updateBoxRegionConfig(@PathParam("serverGameEngineConfigId") int serverGameEngineConfigId, List<BoxRegionConfig> boxRegionConfigs);
}
