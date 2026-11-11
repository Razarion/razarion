package com.btxtech.server.rest.ui;

import com.btxtech.server.model.engine.AudioLibraryEntity;
import com.btxtech.server.rest.AbstractBaseController;
import com.btxtech.server.service.ContentDigest;
import com.btxtech.server.service.ui.AudioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rest/audio")
public class AudioController extends AbstractBaseController<AudioLibraryEntity> {
    private static final CacheControl REVALIDATE = CacheControl.noCache().mustRevalidate();
    private final Logger logger = LoggerFactory.getLogger(AudioController.class);
    private final AudioService audioPersistence;

    public AudioController(AudioService audioPersistence) {
        this.audioPersistence = audioPersistence;
    }

    /**
     * A sound file, as a conditional GET (2026-11-06). Before, NoCacheRestFilter's no-store made
     * every game start download every sound again. Same contract as the models: the browser may
     * keep the file but must revalidate it, so an edited sound still reaches the player at once.
     * The tag is the content's digest, taken per request - the bytes are read from the database
     * either way, what the tag saves is the transfer.
     */
    @GetMapping(value = "{id}", produces = {"audio/mpeg"})
    ResponseEntity<byte[]> getAudio(@PathVariable("id") int id,
                                    @RequestHeader(value = HttpHeaders.IF_NONE_MATCH, required = false) String ifNoneMatch) {
        try {
            byte[] audio = audioPersistence.getAudio(id);
            if (audio == null) {
                return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE).build();
            }
            String eTag = ContentDigest.eTag(ContentDigest.of(audio));
            if (ContentDigest.matches(ifNoneMatch, eTag)) {
                return ResponseEntity.status(HttpStatus.NOT_MODIFIED).eTag(eTag).cacheControl(REVALIDATE).build();
            }
            return ResponseEntity
                    .ok()
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
                    .eTag(eTag)
                    .cacheControl(REVALIDATE)
                    .body(audio);
        } catch (Throwable e) {
            logger.warn("Can not load audio for id: " + id, e);
            throw e;
        }
    }

    @Override
    protected AudioService getBaseEntityCrudService() {
        return audioPersistence;
    }
}
