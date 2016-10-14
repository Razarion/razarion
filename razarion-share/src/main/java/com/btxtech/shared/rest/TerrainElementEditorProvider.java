package com.btxtech.shared.rest;

import com.btxtech.shared.dto.GroundConfig;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.dto.TerrainObjectConfig;
import com.btxtech.shared.gameengine.datatypes.config.SlopeConfig;

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
@Path(RestUrl.TERRAIN_ELEMENT_SERVICE_PATH)
public interface TerrainElementEditorProvider {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("getSlopeNameIds")
    List<ObjectNameId> getSlopeNameIds();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("loadSlopeConfig/{id}")
    SlopeConfig loadSlopeConfig(@PathParam("id") int id);

    @PUT
    @Path("saveSlopeConfig")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    SlopeConfig saveSlopeConfig(SlopeConfig slopeConfig);

    @DELETE
    @Path("deleteSlopeConfig")
    @Consumes(MediaType.APPLICATION_JSON)
    void deleteSlopeConfig(SlopeConfig slopeConfig);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("loadGroundConfig")
    GroundConfig loadGroundConfig();

    @PUT
    @Path("saveGroundConfig")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    GroundConfig saveGroundConfig(GroundConfig slopeConfig);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("getTerrainObjectNameIds")
    @Deprecated
    List<ObjectNameId> getTerrainObjectNameIds();

    @POST
    @Path("createTerrainObjectConfig")
    @Produces(MediaType.APPLICATION_JSON)
    TerrainObjectConfig createTerrainObjectConfig();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("readTerrainObjectConfig/{id}")
    @Deprecated
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
}
