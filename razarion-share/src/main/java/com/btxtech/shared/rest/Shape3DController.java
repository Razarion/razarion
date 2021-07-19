package com.btxtech.shared.rest;

import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.datatypes.shape.Shape3DComposite;
import com.btxtech.shared.datatypes.shape.VertexContainerBuffer;
import com.btxtech.shared.datatypes.shape.config.Shape3DConfig;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

import static com.btxtech.shared.CommonUrl.COLLADA_CONVERTER;

@Path(CommonUrl.SHAPE_3D_CONTROLLER)
public interface Shape3DController {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path(CommonUrl.SHAPE_3D_PROVIDER_GET_VERTEX_BUFFER)
    List<VertexContainerBuffer> getVertexBuffer();

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path(COLLADA_CONVERTER)
    Shape3DComposite colladaConvert(Shape3DConfig shape3DConfig);
}
