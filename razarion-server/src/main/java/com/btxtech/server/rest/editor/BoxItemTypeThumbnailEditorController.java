package com.btxtech.server.rest.editor;

import com.btxtech.server.rest.ui.ImageController;
import com.btxtech.server.service.engine.BoxItemTypeThumbnailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * Studio editor endpoint for per-BoxItemType thumbnail metadata. Mirrors
 * BaseItemTypeThumbnailEditorController one-to-one.
 */
@RestController
@RequestMapping("/rest/editor/box_item_type/thumbnail")
@PreAuthorize("hasAuthority('ADMIN')")
public class BoxItemTypeThumbnailEditorController {
    private final Logger logger = LoggerFactory.getLogger(BoxItemTypeThumbnailEditorController.class);
    private final BoxItemTypeThumbnailService service;

    public BoxItemTypeThumbnailEditorController(BoxItemTypeThumbnailService service) {
        this.service = service;
    }

    @GetMapping("/config")
    public List<BoxItemTypeThumbnailConfig> readAll() {
        return service.readAll();
    }

    @GetMapping("/config/{id}")
    public BoxItemTypeThumbnailConfig read(@PathVariable("id") int id) {
        return service.read(id);
    }

    @PostMapping("/config")
    public void update(@RequestBody BoxItemTypeThumbnailConfig config) {
        service.update(config);
    }

    @PostMapping(value = "/image/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void uploadImage(@PathVariable("id") int id, @RequestParam("image") MultipartFile image) {
        try {
            byte[] bytes = ImageController.inputStreamToArray(image.getInputStream());
            service.uploadImage(id, bytes);
        } catch (IOException e) {
            logger.warn("Thumbnail image upload failed for boxItemType {}", id, e);
            throw new RuntimeException(e);
        }
    }

    @DeleteMapping("/{id}")
    public void reset(@PathVariable("id") int id) {
        service.reset(id);
    }
}
