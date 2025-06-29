package com.btxtech.server.rest.ui;

import com.btxtech.server.model.Roles;
import com.btxtech.server.model.ui.Image;
import com.btxtech.server.model.ui.ImageGalleryItem;
import com.btxtech.server.service.engine.PlanetCrudPersistence;
import com.btxtech.server.service.ui.ImagePersistence;
import jakarta.annotation.security.RolesAllowed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
    private final Logger logger = LoggerFactory.getLogger(ImageController.class);
    private final ImagePersistence imagePersistence;
    private final PlanetCrudPersistence planetCrudPersistence;

    public ImageController(ImagePersistence imagePersistence, PlanetCrudPersistence planetCrudPersistence) {
        this.imagePersistence = imagePersistence;
        this.planetCrudPersistence = planetCrudPersistence;
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
            Image image = imagePersistence.getImage(id);
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

    @GetMapping(value = "minimap/{planetId}", produces = {"image/jpeg", "image/png", "image/gif"})
    public ResponseEntity<byte[]> getMiniMapImage(@PathVariable("planetId") int planetId) {
        try {
            byte[] data = planetCrudPersistence.getMiniMapImage(planetId);
            return ResponseEntity
                    .ok()
                    .lastModified(ZonedDateTime.now())
                    .body(data);
        } catch (Throwable e) {
            logger.warn("Can not loadCold MiniMapImage for planetId: {}", planetId, e);
            throw e;
        }
    }

    @RolesAllowed(Roles.ADMIN)
    @GetMapping(value = "image-gallery", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ImageGalleryItem> getImageGalleryItems() {
        try {
            return imagePersistence.getImageGalleryItems();
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
            throw e;
        }
    }

    @RolesAllowed(Roles.ADMIN)
    @PostMapping(value = "upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    void upload(@RequestParam("images") MultipartFile[] images) {
        try {
            byte[] bytes = inputStreamToArray(images[0].getInputStream());
            String type = contentTypeFromArray(bytes);
            imagePersistence.createImage(bytes, type);
        } catch (IOException ex) {
            logger.warn(ex.getMessage(), ex);
            throw new RuntimeException(ex);
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
            throw e;
        }
    }

    @RolesAllowed(Roles.ADMIN)
    @PostMapping(value = "update/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    void update(@PathVariable("id") int id, @RequestParam("images") MultipartFile[] images) {
        try {
            byte[] bytes = inputStreamToArray(images[0].getInputStream());
            String type = contentTypeFromArray(bytes);
            imagePersistence.save(id, bytes, type);
        } catch (IOException ex) {
            logger.warn(ex.getMessage(), ex);
            throw new RuntimeException(ex);
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
            throw e;
        }
    }

    @DeleteMapping(value = "delete/{id}")
    @RolesAllowed(Roles.ADMIN)
    void delete(@PathVariable("id") int id) {
        try {
            imagePersistence.delete(id);
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
            throw e;
        }
    }

    private String contentTypeFromArray(byte[] bytes) throws IOException {
        return URLConnection.guessContentTypeFromStream(new BufferedInputStream(new ByteArrayInputStream(bytes)));
    }

}
