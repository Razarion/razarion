package com.btxtech.shared.rest;

import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.datatypes.shape.Shape3D;
import com.btxtech.shared.datatypes.shape.Shape3DComposite;
import com.btxtech.shared.datatypes.shape.Shape3DConfig;
import com.btxtech.shared.datatypes.shape.VertexContainerBuffer;

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
@Path(CommonUrl.SHAPE_3D_PROVIDER)
public interface Shape3DEditorController {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path(CommonUrl.SHAPE_3D_PROVIDER_GET_VERTEX_BUFFER)
    List<VertexContainerBuffer> getVertexBuffer();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("getshape3ds")
    List<Shape3D> getShape3Ds();

    @POST
    @Path("create")
    @Produces(MediaType.APPLICATION_JSON)
    Shape3D create();

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("colladaConvert/{id}")
    Shape3DComposite colladaConvert(@PathParam("id") int id, String colladaString);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("save")
    void save(Shape3DConfig shape3DConfig);

    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("delete/{id}")
    void delete(@PathParam("id") int id);
}
