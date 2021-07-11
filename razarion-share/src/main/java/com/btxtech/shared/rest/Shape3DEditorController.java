package com.btxtech.shared.rest;

import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.datatypes.shape.Shape3DComposite;
import com.btxtech.shared.datatypes.shape.config.Shape3DConfig;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Created by Beat
 * 16.08.2016.
 */
@Path(CommonUrl.SHAPE_3D_EDITOR_PATH)
public interface Shape3DEditorController extends CrudController<Shape3DConfig> {
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("colladaConvert")
    Shape3DComposite colladaConvert(Shape3DConfig shape3DConfig);

}
