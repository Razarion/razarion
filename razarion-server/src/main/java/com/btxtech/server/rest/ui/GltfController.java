package com.btxtech.server.rest.ui;

import com.btxtech.server.model.ui.GltfEntity;
import com.btxtech.server.service.AbstractBaseEntityCrudService;
import com.btxtech.server.service.ui.GltfService;
import com.btxtech.server.user.SecurityCheck;
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
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
@RequestMapping("/rest/gltf/")
public class GltfController extends AbstractBaseController<GltfEntity> {
    private final Logger logger = Logger.getLogger(GltfController.class.getName());
    @Inject
    private GltfService gltfCrudPersistence;

    public static GltfEntity jpa2JsonStatic(GltfEntity gltfEntity) {
        Map<String, Integer> materialGltfNames = new HashMap<>();
        if (gltfEntity.getGltfBabylonMaterials() != null) {
            gltfEntity.getGltfBabylonMaterials()
                    .forEach(gltfBabylonMaterial -> materialGltfNames.put(gltfBabylonMaterial.getGltfMaterialName(), gltfBabylonMaterial.getBabylonMaterialEntity().getId()));
        }
        gltfEntity.setMaterialGltfNames(materialGltfNames);
        return gltfEntity;
    }

    @Override
    protected AbstractBaseEntityCrudService<GltfEntity> getEntityCrudPersistence() {
        return gltfCrudPersistence;
    }

    @GetMapping(value = "/glb/{id}", produces = MediaType.APPLICATION_OCTET_STREAM)
    public ResponseEntity<byte[]> getGlb(@PathVariable("id") int id) {
        try {
            return ResponseEntity
                    .ok()
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM)
                    .body(gltfCrudPersistence.getGlb(id));
        } catch (Throwable e) {
            logger.log(Level.SEVERE, "Can not load GltfEntity for id: " + id, e);
            throw e;
        }
    }

    @PUT
    @Path("upload-glb/{id}")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @SecurityCheck
    public void uploadGlb(@PathParam("id") int id, byte[] data) {
        gltfCrudPersistence.setGlb(id, data);
    }

    @Override
    protected GltfEntity jpa2Json(GltfEntity gltfEntity) {
        return jpa2JsonStatic(gltfEntity);
    }

}
