package com.btxtech.server.rest.ui;

import com.btxtech.server.model.Roles;
import com.btxtech.server.model.ui.GltfEntity;
import com.btxtech.server.rest.AbstractBaseController;
import com.btxtech.server.service.AbstractBaseEntityCrudService;
import com.btxtech.server.service.ContentDigest;
import com.btxtech.server.service.ui.GltfService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
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
    /**
     * May be kept, must be asked about. The pair of this and an entity tag is what replaces the
     * blanket no-store for this one response - see {@link #getGlb}. Deliberately not private: a
     * CDN revalidating on behalf of many players is the same guarantee and less origin traffic.
     */
    private static final CacheControl REVALIDATE = CacheControl.noCache().mustRevalidate();
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

    /**
     * The models, as a conditional GET.
     * <p>
     * This one response is eleven megabytes and every game start asked for it in full, because
     * {@code NoCacheRestFilter} marks everything under /rest/ as no-store. That header protects
     * something real - an edited model has to reach the browser, and a cached copy that outlives
     * the edit would leave players looking at the previous world. But no-store forbids keeping the
     * file at all, which is a heavier instrument than the guarantee needs.
     * <p>
     * {@code no-cache} keeps the guarantee and drops the cost: the browser may hold the file but
     * must ask before every use, and the answer is an entity tag comparison rather than a
     * download. Unchanged is 304 and nothing on the wire; changed is a full 200. Stale is not a
     * reachable state, on any cache between here and the player.
     */
    @GetMapping(value = "/glb/{id}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> getGlb(@PathVariable("id") int id,
                                         @RequestHeader(value = HttpHeaders.IF_NONE_MATCH, required = false)
                                         String ifNoneMatch) {
        try {
            String digest = gltfService.getGlbDigest(id);
            if (digest == null) {
                // No bytes to tag. Answer as before rather than inventing a tag for nothing.
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
                        .body(gltfService.getGlb(id));
            }
            String eTag = ContentDigest.eTag(digest);
            if (ContentDigest.matches(ifNoneMatch, eTag)) {
                return ResponseEntity.status(HttpStatus.NOT_MODIFIED)
                        .eTag(eTag)
                        .cacheControl(REVALIDATE)
                        .build();
            }
            return ResponseEntity
                    .ok()
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
                    .eTag(eTag)
                    .cacheControl(REVALIDATE)
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
