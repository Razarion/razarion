package com.btxtech.shared.rest;

import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.gameengine.datatypes.config.LevelConfig;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * Created by Beat
 * on 22.08.2017.
 */
@Path(RestUrl.LEVEL_EDITOR_PROVIDER_PATH)
public interface LevelEditorProvider {
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("create")
    LevelConfig create();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("readObjectNameIds")
    List<ObjectNameId> readObjectNameIds();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("read/{id}")
    LevelConfig reads(@PathParam("id") int id);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("update")
    void update(LevelConfig levelConfig);

    @DELETE
    @Path("delete/{id}")
    void delete(@PathParam("id") int id);
}
