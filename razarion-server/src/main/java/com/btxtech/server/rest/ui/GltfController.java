package com.btxtech.server.rest.ui;

import com.btxtech.server.model.Roles;
import com.btxtech.server.model.ui.GltfEntity;
import com.btxtech.server.rest.AbstractBaseController;
import com.btxtech.server.service.AbstractBaseEntityCrudService;
import com.btxtech.server.service.NoSuchEntityException;
import com.btxtech.server.service.ContentDigest;
import com.btxtech.server.service.ui.GltfService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
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

import java.time.Duration;
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
    /**
     * For the url that carries the digest. A year, public, immutable: the path names the content,
     * so the answer cannot go stale - a different model is a different url. The backend config caps
     * this at seven days (maxTtl), which is the safety net rather than the intent.
     */
    private static final CacheControl IMMUTABLE =
            CacheControl.maxAge(Duration.ofDays(365)).cachePublic().immutable();
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
    public ResponseEntity<Resource> getGlb(@PathVariable("id") int id,
                                           @RequestHeader(value = HttpHeaders.IF_NONE_MATCH, required = false)
                                           String ifNoneMatch) {
        try {
            String digest = gltfService.getGlbDigest(id);
            if (digest == null) {
                // No bytes to tag. Answer as before rather than inventing a tag for nothing.
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
                        .body(body(id));
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
                    .body(body(id));
        } catch (NoSuchEntityException e) {
            // Not there is not broken. The 404 says which.
            throw e;
        } catch (Throwable e) {
            logger.log(Level.SEVERE, "Can not load GltfEntity for id: " + id, e);
            throw e;
        }
    }


    /**
     * The same model, at a url that names its content.
     * <p>
     * The conditional read above keeps the file out of the wire only for someone who already holds
     * it. Measured over a day: 154 requests for this model, 19 of them answered 304. A visitor who
     * arrives from an advertisement arrives once and has nothing to revalidate against, so 88% of
     * them downloaded eleven megabytes from us-central1 - which is most of what a start weighs, and
     * they are the population that gives up waiting.
     * <p>
     * With the digest in the path the response may be public and immutable, so Cloud CDN can hold
     * it at an edge near the player. Eight files answer every start and every player wants the same
     * eight, which is about as cacheable as a workload gets. The freshness guarantee is not
     * weakened, it moves: an edited model gets a new digest and therefore a new url, and the old
     * one is never asked for again.
     * <p>
     * A digest that does not match is not an error. It is a client holding a url from before an
     * edit; it gets the current bytes under the revalidating header, which is exactly what the
     * plain path does, and its next configuration carries the new digest.
     */
    @GetMapping(value = "/glb/{id}/{digest}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<Resource> getGlbByDigest(@PathVariable("id") int id,
                                                   @PathVariable("digest") String digest) {
        try {
            String current = gltfService.getGlbDigest(id);
            boolean fresh = current != null && current.equals(digest);
            return ResponseEntity
                    .ok()
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
                    .eTag(ContentDigest.eTag(current == null ? digest : current))
                    .cacheControl(fresh ? IMMUTABLE : REVALIDATE)
                    .body(body(id));
        } catch (NoSuchEntityException e) {
            throw e;
        } catch (Throwable e) {
            logger.log(Level.SEVERE, "Can not load GltfEntity for id: " + id, e);
            throw e;
        }
    }

    /**
     * The bytes as a {@link Resource} rather than a {@code byte[]}, which is what makes this
     * response range-capable.
     * <p>
     * Spring MVC answers {@code Range} requests and advertises {@code Accept-Ranges: bytes} only
     * for a {@code Resource} return value; for a {@code byte[]} it writes the whole body and says
     * nothing about ranges. That distinction is not cosmetic. Cloud CDN stores a response larger
     * than 10 MiB only if the origin can serve ranges, because that is how it fills its cache in
     * chunks - and without it the model was never cached at an edge at all. Measured over 72 hours
     * on the 10.83 MiB model: 685 requests to the immutable digest url, 458 of them complete
     * deliveries, cacheLookup true every single time, and zero cache fills. Every player, anywhere,
     * paid the trip to us-central1. The 6.05 MiB model that replaced it filled on its first
     * request, because it fits under the limit.
     * <p>
     * Fitting under the limit is luck, not a guarantee - nothing fails and nothing is logged when a
     * model grows past it again. Serving ranges removes the limit instead of living beneath it.
     * <p>
     * A {@code ByteArrayResource} and not a streaming one: the blob is already fully in memory by
     * the time it gets here, so wrapping it costs nothing and a range is then served as a region of
     * that array.
     */
    private Resource body(int id) {
        // A row with no model at all answers as it always did, with no body. ByteArrayResource
        // rejects a null array, so the check has to happen here rather than at the call sites.
        byte[] glb = gltfService.getGlb(id);
        return glb != null ? new ByteArrayResource(glb) : null;
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
