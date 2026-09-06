package com.btxtech.server.rest.ui;

import com.btxtech.server.model.Roles;
import com.btxtech.server.model.ui.BabylonMaterialEntity;
import com.btxtech.server.rest.AbstractBaseController;
import com.btxtech.server.service.AbstractBaseEntityCrudService;
import com.btxtech.server.service.NoSuchEntityException;
import com.btxtech.server.service.ContentDigest;
import com.btxtech.server.service.ui.BabylonMaterialService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/rest/babylon-material")
public class BabylonMaterialController extends AbstractBaseController<BabylonMaterialEntity> {
    /**
     * Hold it, but ask before every use. The pairing with an entity tag is what makes this safe:
     * without one the browser would have to fetch the whole thing to find out it was unchanged.
     */
    private static final CacheControl REVALIDATE = CacheControl.noCache().mustRevalidate();
    private final Logger logger = LoggerFactory.getLogger(BabylonMaterialController.class);
    private final BabylonMaterialService babylonMaterialPersistence;

    public BabylonMaterialController(BabylonMaterialService babylonMaterialPersistence) {
        this.babylonMaterialPersistence = babylonMaterialPersistence;
    }

    @Override
    protected AbstractBaseEntityCrudService<BabylonMaterialEntity> getBaseEntityCrudService() {
        return babylonMaterialPersistence;
    }

    /**
     * The materials, as a conditional GET.
     * <p>
     * Six of these are read on every game start and they weigh eight megabytes between them. That
     * is 51% of everything a player has to download before the game is playable - more than the
     * JavaScript, the WebAssembly and the images together - and every byte of it arrived again on
     * every single start, because this response said no-store.
     * <p>
     * The guarantee behind that header is real: a material edited in the node editor has to reach
     * the player, and a held copy that outlives the edit would show the wrong world. {@code
     * no-cache} plus an entity tag keeps the guarantee and drops the cost - the browser may hold
     * the file but must ask before every use, and the answer is a comparison rather than eight
     * megabytes. Unchanged is 304 and nothing on the wire; changed is a full 200.
     * <p>
     * Compressed as well, which an earlier version of this comment argued against: the entity tag
     * only helps somebody who has been here before, and almost nobody has. Measured over the Meta
     * campaign, 27.08.-04.09.2026, the median start arrives with 111 KB in its cache and downloads
     * the full eight megabytes - the paid visitor is a first visit by definition. And the payload
     * turns out to compress by 29%: it is 99% base64, and base64 is six bits carried in eight, so
     * gzip gets back very nearly what the encoding wastes. 9,145 KB become 6,517 KB.
     * <p>
     * Which is also why the type below says what it is. The bytes have always been JSON; declaring
     * them application/octet-stream is what kept them out of server.compression.mime-types and
     * meant nobody noticed for as long as they were served.
     */
    @GetMapping(value = "/data/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<byte[]> getData(@PathVariable("id") int id,
                                          @RequestHeader(value = HttpHeaders.IF_NONE_MATCH, required = false)
                                          String ifNoneMatch) {
        try {
            String digest = babylonMaterialPersistence.getDataDigest(id);
            if (digest == null) {
                // No bytes to tag. Answer as before rather than inventing a tag for nothing.
                byte[] data = babylonMaterialPersistence.getData(id);
                return ResponseEntity
                        .ok()
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .body(data);
            }
            String eTag = ContentDigest.weakETag(digest);
            if (ContentDigest.matches(ifNoneMatch, eTag)) {
                return ResponseEntity.status(HttpStatus.NOT_MODIFIED)
                        .eTag(eTag)
                        .cacheControl(REVALIDATE)
                        .build();
            }
            byte[] data = babylonMaterialPersistence.getData(id);
            return ResponseEntity
                    .ok()
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .eTag(eTag)
                    .cacheControl(REVALIDATE)
                    .body(data);
        } catch (NoSuchEntityException e) {
            // Not there is not broken. The 404 says which.
            throw e;
        } catch (Throwable e) {
            logger.warn("Can not load BabylonMaterialEntity for id: " + id, e);
            throw e;
        }
    }

    @GetMapping("sizes")
    public List<MaterialSizeInfo> getMaterialSizes() {
        return babylonMaterialPersistence.getMaterialSizes();
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping(value = "upload/{id}", consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void uploadData(@PathVariable("id") int id, @RequestBody byte[] data) {
        babylonMaterialPersistence.setData(id, data);
    }
}
