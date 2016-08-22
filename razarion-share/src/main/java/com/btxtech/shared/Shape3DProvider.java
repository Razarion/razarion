package com.btxtech.shared;

import com.btxtech.shared.datatypes.shape.Shape3D;
import com.btxtech.shared.datatypes.shape.Shape3DConfig;

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
 * 16.08.2016.
 */
@Path(RestUrl.EDITOR_HELPER_PATH)
public interface Shape3DProvider {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("imagegallery")
    List<Shape3D> getShape3Ds();

    @POST
    @Path("create")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    Shape3D create(String colladaString);

    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("colladaConvert")
    Shape3D colladaConvert(String colladaString);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("colladaConvert")
    void save(Shape3DConfig shape3DConfig);

    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("delete/{id}")
    void delete(@PathParam("id") int id);

}
