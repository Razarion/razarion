package com.btxtech.shared.rest;

import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.datatypes.shape.ThreeJsModelConfig;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import java.io.InputStream;
import java.util.Map;

@Path(CommonUrl.THREE_JS_MODEL_EDITOR_PATH)
public interface ThreeJsModelEditorController extends CrudController<ThreeJsModelConfig> {
    @PUT
    @Path("upload/{id}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    void upload(@PathParam("id") int id, Map<String, InputStream> formData);

}
