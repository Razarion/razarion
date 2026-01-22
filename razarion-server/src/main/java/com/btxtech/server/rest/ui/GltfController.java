package com.btxtech.server.rest.ui;

import com.btxtech.server.model.Roles;
import com.btxtech.server.model.ui.GltfEntity;
import com.btxtech.server.rest.AbstractBaseController;
import com.btxtech.server.service.AbstractBaseEntityCrudService;
import com.btxtech.server.service.ui.GltfService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
@RequestMapping("/rest/gltf/")
public class GltfController extends AbstractBaseController<GltfEntity> {
    private final Logger logger = Logger.getLogger(GltfController.class.getName());
    private final GltfService gltfService;

    public GltfController(GltfService gltfService) {
        this.gltfService = gltfService;
    }

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
    protected AbstractBaseEntityCrudService<GltfEntity> getBaseEntityCrudService() {
        return gltfService;
    }

    @GetMapping(value = "/glb/{id}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> getGlb(@PathVariable("id") int id) {
        try {
            return ResponseEntity
                    .ok()
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
                    .body(gltfService.getGlb(id));
        } catch (Throwable e) {
            logger.log(Level.SEVERE, "Can not load GltfEntity for id: " + id, e);
            throw e;
        }
    }

    @PreAuthorize("hasAuthority('ADMIN')") 
    @PutMapping(value = "upload-glb/{id}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void uploadGlb(@PathVariable("id") int id, @RequestBody byte[] data) {
        gltfService.setGlb(id, data);
    }

    @Override
    protected GltfEntity jpa2Json(GltfEntity gltfEntity) {
        return jpa2JsonStatic(gltfEntity);
    }

}
