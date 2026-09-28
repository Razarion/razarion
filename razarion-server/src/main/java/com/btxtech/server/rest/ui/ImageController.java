package com.btxtech.server.rest.ui;

import com.btxtech.server.model.Roles;
import com.btxtech.server.model.ui.Image;
import com.btxtech.server.model.ui.ImageGalleryItem;
import com.btxtech.server.service.ContentDigest;
import com.btxtech.server.service.NoSuchEntityException;
import com.btxtech.server.service.engine.PlanetCrudService;
import com.btxtech.server.service.ui.ImageService;
import com.btxtech.server.service.ui.StarterMiniMapService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.time.ZonedDateTime;
import java.util.List;

@RestController
@RequestMapping("/rest/image/")
public class ImageController {
    private static final CacheControl REVALIDATE = CacheControl.noCache().mustRevalidate();
    private final Logger logger = LoggerFactory.getLogger(ImageController.class);
    private final ImageService imageService;
    private final PlanetCrudService planetCrudPersistence;

    private final StarterMiniMapService starterMiniMapService;

    public ImageController(ImageService imageService, PlanetCrudService planetCrudPersistence,
                           StarterMiniMapService starterMiniMapService) {
        this.imageService = imageService;
        this.planetCrudPersistence = planetCrudPersistence;
        this.starterMiniMapService = starterMiniMapService;
    }

    /**
     * Noob Island from the starter planet's minimap, for the loading screen's map arm (index.html).
     * Asked for before the page knows the player's planet, like the terrain prefetch. Conditional
     * like the whole minimap: the tag follows the source image.
     */
    @GetMapping(value = "minimap/starter/noob", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getStarterNoobMiniMap(@RequestHeader(value = HttpHeaders.IF_NONE_MATCH, required = false)
                                                        String ifNoneMatch) {
        try {
            String digest = starterMiniMapService.getNoobDigest();
            if (digest == null) {
                return ResponseEntity.notFound().build();
            }
            String eTag = ContentDigest.eTag(digest);
            if (ContentDigest.matches(ifNoneMatch, eTag)) {
                return ResponseEntity.status(HttpStatus.NOT_MODIFIED).eTag(eTag).cacheControl(REVALIDATE).build();
            }
            byte[] png = starterMiniMapService.getNoobImage();
            if (png == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_PNG_VALUE)
                    .eTag(eTag)
                    .cacheControl(REVALIDATE)
                    .body(png);
        } catch (Throwable e) {
            logger.warn("Can not load the starter planet's noob minimap", e);
            throw e;
        }
    }

    /**
     * What the bytes are, not what the mapping lists first. The editor saves the minimap as PNG,
     * and it went out as image/jpeg - browsers sniff past that, but anything that trusts the
     * header, a CDN re-encoding images among them, does not.
     */
    static String imageType(byte[] data) {
        if (data != null && data.length >= 4) {
            if ((data[0] & 0xFF) == 0x89 && data[1] == 'P' && data[2] == 'N' && data[3] == 'G') {
                return MediaType.IMAGE_PNG_VALUE;
            }
            if ((data[0] & 0xFF) == 0xFF && (data[1] & 0xFF) == 0xD8) {
                return MediaType.IMAGE_JPEG_VALUE;
            }
            if (data[0] == 'G' && data[1] == 'I' && data[2] == 'F') {
                return MediaType.IMAGE_GIF_VALUE;
            }
        }
        return MediaType.APPLICATION_OCTET_STREAM_VALUE;
    }

    public static byte[] inputStreamToArray(InputStream initialStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[1024];
        while ((nRead = initialStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }

        buffer.flush();
        return buffer.toByteArray();
    }

    @GetMapping(value = "{id}", produces = {"image/jpeg", "image/png", "image/gif"})
    public ResponseEntity<byte[]> getImage(@PathVariable("id") int id) {
        try {
            Image image = imageService.getImage(id);
            MediaType mediaType = MediaType.valueOf(image.getType());
            return ResponseEntity
                    .ok()
                    .header(HttpHeaders.CONTENT_TYPE, mediaType.toString())
                    .lastModified(ZonedDateTime.now())
                    .body(image.getData());
        } catch (Throwable e) {
            logger.warn("Can not loadCold image for id: {}", id, e);
            throw e;
        }
    }

    /**
     * The minimap's background, as a conditional GET - the same arrangement as the model file and
     * the height map (see ContentDigest). It went out no-store, and the client added a timestamp
     * to the url on top, so the 300 KB were downloaded afresh on every game start and never kept.
     * A re-saved image still reaches the player at once: the tag changes with the bytes.
     */
    @GetMapping(value = "minimap/{planetId}", produces = {"image/jpeg", "image/png", "image/gif"})
    public ResponseEntity<byte[]> getMiniMapImage(@PathVariable("planetId") int planetId,
                                                  @RequestHeader(value = HttpHeaders.IF_NONE_MATCH, required = false)
                                                  String ifNoneMatch) {
        try {
            String digest = planetCrudPersistence.getMiniMapDigest(planetId);
            String eTag = digest != null ? ContentDigest.eTag(digest) : null;
            if (eTag != null && ContentDigest.matches(ifNoneMatch, eTag)) {
                return ResponseEntity.status(HttpStatus.NOT_MODIFIED)
                        .eTag(eTag)
                        .cacheControl(REVALIDATE)
                        .build();
            }
            byte[] data = planetCrudPersistence.getMiniMapImage(planetId);
            ResponseEntity.BodyBuilder response = ResponseEntity
                    .ok()
                    .header(HttpHeaders.CONTENT_TYPE, imageType(data))
                    .cacheControl(REVALIDATE);
            if (eTag != null) {
                response.eTag(eTag);
            }
            return response.body(data);
        } catch (NoSuchEntityException e) {
            // A planet id that does not exist is an answer, and the 404 is it. Logging a stack
            // trace here is what made thirty-three of these indistinguishable from a fault.
            throw e;
        } catch (Throwable e) {
            logger.warn("Can not loadCold MiniMapImage for planetId: {}", planetId, e);
            throw e;
        }
    }

    @PreAuthorize("hasAuthority('ADMIN')") 
    @GetMapping(value = "image-gallery", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ImageGalleryItem> getImageGalleryItems() {
        try {
            return imageService.getImageGalleryItems();
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
            throw e;
        }
    }

    @PreAuthorize("hasAuthority('ADMIN')") 
    @PostMapping(value = "upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    void upload(@RequestParam("images") MultipartFile[] images) {
        try {
            byte[] bytes = inputStreamToArray(images[0].getInputStream());
            String type = contentTypeFromArray(bytes);
            imageService.createImage(bytes, type);
        } catch (IOException ex) {
            logger.warn(ex.getMessage(), ex);
            throw new RuntimeException(ex);
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
            throw e;
        }
    }

    @PreAuthorize("hasAuthority('ADMIN')") 
    @PostMapping(value = "update/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    void update(@PathVariable("id") int id, @RequestParam("images") MultipartFile[] images) {
        try {
            byte[] bytes = inputStreamToArray(images[0].getInputStream());
            String type = contentTypeFromArray(bytes);
            imageService.save(id, bytes, type);
        } catch (IOException ex) {
            logger.warn(ex.getMessage(), ex);
            throw new RuntimeException(ex);
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
            throw e;
        }
    }

    @DeleteMapping(value = "delete/{id}")
    @PreAuthorize("hasAuthority('ADMIN')") 
    void delete(@PathVariable("id") int id) {
        try {
            imageService.delete(id);
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
            throw e;
        }
    }

    private String contentTypeFromArray(byte[] bytes) throws IOException {
        return URLConnection.guessContentTypeFromStream(new BufferedInputStream(new ByteArrayInputStream(bytes)));
    }

}
