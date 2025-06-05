package com.btxtech.server.rest.ui;

import com.btxtech.server.model.engine.AudioLibraryEntity;
import com.btxtech.server.rest.AbstractBaseController;
import com.btxtech.server.service.ui.AudioPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rest/audio")
public class AudioController extends AbstractBaseController<AudioLibraryEntity> {
    private final Logger logger = LoggerFactory.getLogger(AudioController.class);
    private AudioPersistence audioPersistence;

    public AudioController(AudioPersistence audioPersistence) {
        this.audioPersistence = audioPersistence;
    }

    @GetMapping(value = "{id}", produces = {"audio/mpeg"})
    ResponseEntity<byte[]> getAudio(@PathVariable("id") int id) {
        try {
            return ResponseEntity
                    .ok()
                    .header(HttpHeaders.CONTENT_TYPE, org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE)
                    .body(audioPersistence.getAudio(id));
        } catch (Throwable e) {
            logger.warn("Can not load BabylonMaterialEntity for id: " + id, e);
            throw e;
        }
    }

    @Override
    protected AudioPersistence getEntityCrudPersistence() {
        return audioPersistence;
    }
}
