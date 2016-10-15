package com.btxtech.shared.rest;

import com.btxtech.shared.datatypes.shape.Shape3D;
import com.btxtech.shared.datatypes.shape.Shape3DConfig;
import com.btxtech.shared.dto.ClipConfig;

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
 * 14.10.2016.
 */
@Path(RestUrl.CLIP_PROVIDER)
public interface ClipProvider {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("all")
    List<ClipConfig> read();

    @POST
    @Path("create")
    @Produces(MediaType.APPLICATION_JSON)
    ClipConfig create();

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("save")
    void update(ClipConfig clipConfig);

    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("delete/{id}")
    void delete(@PathParam("id") int id);

}
