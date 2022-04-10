package com.btxtech.shared.rest;

import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.datatypes.shape.ThreeJsModelConfig;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.Map;

@Path(CommonUrl.THREE_JS_MODEL_PATH)
public interface ThreeJsModelController {
    @GET
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    Response getThreeJsModel(@PathParam("id") int id);
}
