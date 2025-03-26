package com.btxtech.server.rest.ui;

import com.btxtech.server.model.ui.BabylonMaterialEntity;
import com.btxtech.server.service.AbstractBaseEntityCrudService;
import com.btxtech.server.service.ui.BabylonMaterialService;
import com.btxtech.server.user.SecurityCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;

@RestController
@RequestMapping("/rest/babylon-material")
public class BabylonMaterialController extends AbstractBaseController<BabylonMaterialEntity> {
    private final Logger logger = LoggerFactory.getLogger(BabylonMaterialController.class);

    @Inject
    private BabylonMaterialService babylonMaterialPersistence;

    @Override
    protected AbstractBaseEntityCrudService<BabylonMaterialEntity> getEntityCrudPersistence() {
        return babylonMaterialPersistence;
    }

    @GetMapping(value = "/data/{id}", produces = MediaType.APPLICATION_OCTET_STREAM)
    public ResponseEntity<byte[]> getData(@PathVariable("id") int id) {
        try {
            return ResponseEntity
                    .ok()
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM)
                    .body(babylonMaterialPersistence.getData(id));
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
