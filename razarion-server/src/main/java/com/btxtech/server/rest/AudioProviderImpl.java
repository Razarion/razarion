package com.btxtech.server.rest;

import com.btxtech.server.DataUrlDecoder;
import com.btxtech.server.persistence.AudioPersistence;
import com.btxtech.shared.dto.AudioConfig;
import com.btxtech.shared.dto.AudioItemConfig;
import com.btxtech.shared.rest.AudioProvider;
import com.btxtech.shared.system.ExceptionHandler;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.List;

/**
 * Created by Beat
 * 24.12.2016.
 */
public class AudioProviderImpl implements AudioProvider {
    @Inject
    private AudioPersistence audioPersistence;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private ExceptionHandler exceptionHandler;

    @Override
    public Response getAudio(int id) throws Exception {
        try {
            return Response.ok(audioPersistence.getAudio(id)).lastModified(new Date()).build();
        } catch (Throwable e) {
            exceptionHandler.handleException("Can not load audio for id: " + id, e);
            throw e;
        }
    }

    @Override
    public List<AudioItemConfig> getAllAudios() {
        try {
            return audioPersistence.getAllAudios();
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    public void createAudio(String dataUrl) {
        try {
            audioPersistence.createAudio(new DataUrlDecoder(dataUrl));
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    public void save(List<AudioItemConfig> audioItemConfigs) {
        try {
            audioPersistence.save(audioItemConfigs);
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }
}
