package com.btxtech.server.rest.ui;

import com.btxtech.server.model.Roles;
import com.btxtech.server.model.ui.BabylonMaterialEntity;
import com.btxtech.server.rest.AbstractBaseController;
import com.btxtech.server.service.AbstractBaseEntityCrudService;
import com.btxtech.server.service.ui.BabylonMaterialService;
import jakarta.annotation.security.RolesAllowed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rest/babylon-material")
public class BabylonMaterialController extends AbstractBaseController<BabylonMaterialEntity> {
    private final Logger logger = LoggerFactory.getLogger(BabylonMaterialController.class);
    private final BabylonMaterialService babylonMaterialPersistence;

    public BabylonMaterialController(BabylonMaterialService babylonMaterialPersistence) {
        this.babylonMaterialPersistence = babylonMaterialPersistence;
    }

    @Override
    protected AbstractBaseEntityCrudService<BabylonMaterialEntity> getBaseEntityCrudService() {
        return babylonMaterialPersistence;
    }

    @GetMapping(value = "/data/{id}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> getData(@PathVariable("id") int id) {
        try {
            return ResponseEntity
                    .ok()
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
                    .body(babylonMaterialPersistence.getData(id));
        } catch (Throwable e) {
            logger.warn("Can not load BabylonMaterialEntity for id: " + id, e);
            throw e;
        }
    }

    @RolesAllowed(Roles.ADMIN)
    @PostMapping(value = "upload/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void uploadData(@PathVariable("id") int id, @RequestBody byte[] data) {
        babylonMaterialPersistence.setData(id, data);
    }
}
