package com.btxtech.server.rest.ui;

import com.btxtech.server.model.ui.BabylonMaterialEntity;
import com.btxtech.server.service.AbstractBaseEntityCrudService;
import com.btxtech.server.service.ui.BabylonMaterialService;
import com.btxtech.server.user.SecurityCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Date;

import static com.btxtech.shared.CommonUrl.BABYLON_MATERIAL_CONTROLLER;

@RestController
@RequestMapping(BABYLON_MATERIAL_CONTROLLER)
public class BabylonMaterialController extends AbstractBaseController<BabylonMaterialEntity> {
    private final Logger logger = LoggerFactory.getLogger(BabylonMaterialController.class);

    @Inject
    private BabylonMaterialService babylonMaterialPersistence;

    @Override
    protected AbstractBaseEntityCrudService<BabylonMaterialEntity> getEntityCrudPersistence() {
        return babylonMaterialPersistence;
    }

    @GetMapping(value = "/data/{id}", produces = "application/json")
    public Response getData(@PathParam("id") int id) {
        try {
            return Response.ok(babylonMaterialPersistence.getData(id),
                    MediaType.APPLICATION_OCTET_STREAM).lastModified(new Date()).build();
        } catch (Throwable e) {
            logger.warn("Can not load BabylonMaterialEntity for id: " + id, e);
            throw e;
        }
    }

    @PUT
    @Path("upload/{id}")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @SecurityCheck
    public void uploadData(@PathParam("id") int id, byte[] data) {
        babylonMaterialPersistence.setData(id, data);
    }
}
