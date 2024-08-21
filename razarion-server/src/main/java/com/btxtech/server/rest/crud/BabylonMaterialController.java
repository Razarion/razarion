package com.btxtech.server.rest.crud;

import com.btxtech.server.persistence.AbstractEntityCrudPersistence;
import com.btxtech.server.persistence.BabylonMaterialCrudPersistence;
import com.btxtech.server.persistence.BabylonMaterialEntity;
import com.btxtech.server.user.SecurityCheck;
import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.system.ExceptionHandler;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Date;

@Path(CommonUrl.BABYLON_MATERIAL_CONTROLLER)
public class BabylonMaterialController extends BaseEntityController<BabylonMaterialEntity> {
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private BabylonMaterialCrudPersistence babylonMaterialCrudPersistence;

    @Override
    protected AbstractEntityCrudPersistence<BabylonMaterialEntity> getEntityCrudPersistence() {
        return babylonMaterialCrudPersistence;
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("data/{id}")
    public Response getData(@PathParam("id") int id) {
        try {
            return Response.ok(babylonMaterialCrudPersistence.getData(id),
                    MediaType.APPLICATION_OCTET_STREAM).lastModified(new Date()).build();
        } catch (Throwable e) {
            exceptionHandler.handleException("Can not load BabylonMaterialEntity for id: " + id, e);
            throw e;
        }
    }

    @PUT
    @Path("upload/{id}")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @SecurityCheck
    public void uploadData(@PathParam("id") int id, byte[] data) {
        babylonMaterialCrudPersistence.setData(id, data);
    }
}
