package com.btxtech.shared.rest;

import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.dto.TerrainObjectConfig;
import com.btxtech.shared.dto.WaterConfig;

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
 * 15.08.2015.
 */
@Path(CommonUrl.TERRAIN_ELEMENT_SERVICE_PATH)
public interface TerrainElementEditorProvider {
    @PUT
    @Path("saveWaterConfig")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    WaterConfig saveWaterConfig(WaterConfig waterConfig);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("getTerrainObjectNameIds")
    List<ObjectNameId> getTerrainObjectNameIds();

    @POST
    @Path("createTerrainObjectConfig")
    @Produces(MediaType.APPLICATION_JSON)
    TerrainObjectConfig createTerrainObjectConfig();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("readTerrainObjectConfig/{id}")
    TerrainObjectConfig readTerrainObjectConfig(@PathParam("id") int id);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("readTerrainObjectConfigs")
    List<TerrainObjectConfig> readTerrainObjectConfigs();

    @PUT
    @Path("saveTerrainObjectConfig")
    @Consumes(MediaType.APPLICATION_JSON)
    void saveTerrainObjectConfig(TerrainObjectConfig terrainObjectConfig);

    @DELETE
    @Path("deleteTerrainObjectConfig")
    @Consumes(MediaType.APPLICATION_JSON)
    void deleteTerrainObjectConfig(TerrainObjectConfig terrainObjectConfig);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("readDrivewayObjectNameIds")
    List<ObjectNameId> readDrivewayObjectNameIds();
}
