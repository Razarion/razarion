package com.btxtech.server.rest;

import com.btxtech.server.persistence.ImagePersistence;
import com.btxtech.server.persistence.ImagePersistence.Image;
import com.btxtech.server.persistence.PlanetCrudPersistence;
import com.btxtech.shared.dto.ImageGalleryItem;
import com.btxtech.shared.rest.ImageProvider;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.shared.utils.CollectionUtils;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by Beat
 * 15.06.2016.
 */
public class ImageProviderImpl implements ImageProvider {
    @Inject
    private ImagePersistence imagePersistence;
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private PlanetCrudPersistence planetCrudPersistence;

    @Override
    public Response getImage(int id) {
        try {
            Image image = imagePersistence.getImage(id);
            MediaType mediaType = null;
            try {
                mediaType = MediaType.valueOf(image.getType());
            } catch (Throwable t) {
                exceptionHandler.handleException("Can not get MediaType for image for id: " + id, t);
            }
            return Response.ok(image.getData(), mediaType).lastModified(new Date()).build();
        } catch (Throwable e) {
            exceptionHandler.handleException("Can not loadCold image for id: " + id, e);
            throw e;
        }
    }

    @Override
    public List<ImageGalleryItem> getImageGalleryItems() {
        try {
            return imagePersistence.getImageGalleryItems();
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    @Transactional
    public ImageGalleryItem getImageGalleryItem(int id) {
        try {
            return imagePersistence.getImageGalleryItem(id);
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    @Transactional
    public void uploadImage(Map<String, InputStream> files) {
        try {
            files.forEach((ignore, inputStream) -> {
                try {
                    byte[] bytes = inputStreamToArray(inputStream);
                    String type = contentTypeFromArray(bytes);
                    imagePersistence.createImage(bytes, type);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    @Transactional
    public void update(int id, Map<String, InputStream> files) {
        try {
            if (files.size() != 1) {
                throw new IllegalArgumentException("More them one image saved");
            }
            InputStream inputStream = CollectionUtils.getFirst(files.values());
            byte[] bytes = inputStreamToArray(inputStream);
            String type = contentTypeFromArray(bytes);
            imagePersistence.save(id, bytes, type);
        } catch (IOException e) {
            exceptionHandler.handleException(e);
            throw new RuntimeException(e);
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    @Transactional
    public void delete(int id) {
        try {
            imagePersistence.delete(id);
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    public Response getMiniMapImage(int planetId) {
        try {
            return Response.ok(planetCrudPersistence.getMiniMapImage(planetId)).lastModified(new Date()).build();
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
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

    private String contentTypeFromArray(byte[] bytes) throws IOException {
        return URLConnection.guessContentTypeFromStream(new BufferedInputStream(new ByteArrayInputStream(bytes)));
    }
}
