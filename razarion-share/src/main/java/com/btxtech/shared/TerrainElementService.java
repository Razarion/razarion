package com.btxtech.shared;

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
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Map;

/**
 * Created by Beat
 * 15.08.2015.
 */
@Path(RestUrl.TERRAIN_ELEMENT_SERVICE_PATH)
public interface TerrainElementService {
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
    List<ObjectNameId> getTerrainObjectNameIds();

    @POST
    @Path("saveTerrainObject")
    @Consumes(MediaType.APPLICATION_JSON)
    void saveTerrainObject(@QueryParam("id") int id, @QueryParam("colladaString") String colladaString, Map<String, Integer> textures);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("colladaConvert")
    TerrainObjectConfig colladaConvert(@QueryParam("terrainObjectId") int terrainObjectId, @QueryParam("colladaString") String colladaString);

}
