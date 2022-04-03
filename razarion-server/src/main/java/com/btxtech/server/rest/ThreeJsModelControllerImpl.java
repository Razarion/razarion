package com.btxtech.server.rest;

import com.btxtech.server.persistence.ThreeJsModelCrudPersistence;
import com.btxtech.shared.rest.ThreeJsModelController;
import com.btxtech.shared.system.ExceptionHandler;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.util.Date;

/**
 * Created by Beat
 * 15.06.2016.
 */
public class ThreeJsModelControllerImpl implements ThreeJsModelController {
    @Inject
    private ThreeJsModelCrudPersistence threeJsModelCrudPersistence;
    @Inject
    private ExceptionHandler exceptionHandler;

    @Override
    public Response getThreeJsModel(int id) {
        try {
            byte[] threeJsModel = threeJsModelCrudPersistence.getThreeJsModel(id);
            return Response.ok(threeJsModel).lastModified(new Date()).build();
        } catch (Throwable e) {
            exceptionHandler.handleException("Can not load ThreeJsModel for id: " + id, e);
            throw e;
        }
    }
}
