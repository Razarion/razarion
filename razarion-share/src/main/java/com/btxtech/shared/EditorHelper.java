package com.btxtech.shared;

import com.btxtech.shared.datatypes.shape.Shape3D;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Created by Beat
 * 16.08.2016.
 */
@Path(RestUrl.EDITOR_HELPER_PATH)
public interface EditorHelper {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("colladaConvert")
    Shape3D colladaConvert(String colladaString);

}
