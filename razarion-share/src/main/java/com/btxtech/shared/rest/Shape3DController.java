package com.btxtech.shared.rest;

import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.datatypes.shape.VertexContainerBuffer;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path(CommonUrl.SHAPE_3D_CONTROLLER)
public interface Shape3DController {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path(CommonUrl.SHAPE_3D_PROVIDER_GET_VERTEX_BUFFER)
    List<VertexContainerBuffer> getVertexBuffer();
}
