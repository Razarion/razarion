package com.btxtech.client.imageservice;

import com.btxtech.uiservice.system.boot.DeferredStartup;
import com.btxtech.shared.dto.ImageGalleryItem;
import com.btxtech.shared.rest.ImageProvider;
import com.btxtech.shared.rest.RestUrl;
import com.google.gwt.dom.client.ImageElement;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 15.06.2016.
 */
@Singleton
public class ImageUiService {
    // TODO export the CRUD part tp the CRUD service

    public interface ImageListener {
        void onLoaded(ImageElement imageElement);
    }

    public interface ImageGalleryListener {
        void onLoaded(ImageElement imageElement, ImageGalleryItem imageGalleryItem);
    }

    public interface ImageGalleryItemListener {
        void onLoaded(List<ImageGalleryItem> imageGalleryItems);
    }

    public interface ChangeListener {
        void onChanged(Collection<ImageGalleryItem> changed);
    }

    private Logger logger = Logger.getLogger(ImageListener.class.getName());
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private Caller<ImageProvider> imageService;
    private Map<Integer, ImageElement> imageElementLibrary = new HashMap<>();
    private Map<Integer, ImageGalleryItem> imageGalleryItemLibrary = new HashMap<>();
    private Map<Integer, Collection<ImageListener>> imageElementListeners = new HashMap<>();
    private Map<Integer, Collection<ImageGalleryListener>> imageGalleryListeners = new HashMap<>();
    private Set<ImageGalleryItem> changed = new HashSet<>();
    private Collection<ChangeListener> changeListeners = new ArrayList<>();
    private Set<Integer> currentlyLoading = new HashSet<>();

    public void requestImage(final int id, ImageListener listener) {
        if (id == 0) {
            throw new IllegalArgumentException("id 0 is not a valid image");
        }
        addListener(imageElementListeners, id, listener);
        ImageElement ImageElement = imageElementLibrary.get(id);
        if (ImageElement != null) {
            listener.onLoaded(ImageElement);
            return;
        }
        loadImage(id, false);
    }

    public void requestImage(final int id, ImageGalleryListener listener) {
        addListener(imageGalleryListeners, id, listener);
        ImageGalleryItem imageGalleryItem = imageGalleryItemLibrary.get(id);
        ImageElement imageElement = imageElementLibrary.get(id);
        if (imageElement != null && imageGalleryItem != null) {
            listener.onLoaded(imageElement, imageGalleryItem);
            return;
        }
        if (imageElement != null) {
            loadImageGalleyItem(id, imageElement);
        } else {
            loadImage(id, true);
        }
    }

    public void overrideImage(int id, String dataUrl, int size, String type) {
        ImageElement imageElement = imageElementLibrary.get(id);
        imageElement.setSrc(dataUrl);
        ImageGalleryItem imageGalleryItem = imageGalleryItemLibrary.get(id);
        imageGalleryItem.setSize(size);
        imageGalleryItem.setType(type);
        changed.add(imageGalleryItem);
        fireImageElementListeners(id, imageElement);
        fireImageGalleryListeners(id, imageElement, imageGalleryItem);
        fireChanged();
    }

    public void getImageGalleryItems(final ImageGalleryItemListener imageGalleryItemListener) {
        imageService.call(new RemoteCallback<List<ImageGalleryItem>>() {
            @Override
            public void callback(List<ImageGalleryItem> imageGalleryItems) {
                List<ImageGalleryItem> result = new ArrayList<>();
                for (ImageGalleryItem imageGalleryItem : imageGalleryItems) {
                    if (changed.contains(imageGalleryItem)) {
                        result.add(imageGalleryItemLibrary.get(imageGalleryItem.getId()));
                    } else {
                        result.add(imageGalleryItem);
                    }
                }
                imageGalleryItemListener.onLoaded(result);
            }
        }, (message, throwable) -> {
            logger.log(Level.SEVERE, "getImageGalleryItem failed: " + message, throwable);
            return false;
        }).getImageGalleryItems();
    }

    public void reload(ImageGalleryItemListener imageGalleryItemListener) {
        changed.clear();
        fireChanged();
        getImageGalleryItems(imageGalleryItemListener);
        imageElementLibrary.clear();
        imageGalleryItemLibrary.clear();
        for (Integer id : imageElementLibrary.keySet()) {
            loadImage(id, false);
        }
        for (Integer id : imageGalleryItemLibrary.keySet()) {
            loadImageGalleyItem(id, imageElementLibrary.get(id)); // May not in imageElementLibrary
        }
    }

    public void create(String dataUrl, final ImageGalleryItemListener imageGalleryItemListener) {
        imageService.call(aVoid -> getImageGalleryItems(imageGalleryItemListener), (message, throwable) -> {
            logger.log(Level.SEVERE, "uploadImage failed: " + message, throwable);
            return false;
        }).uploadImage(dataUrl);
    }

    public void save(final ImageGalleryItemListener imageGalleryItemListener) {
        if (changed.isEmpty()) {
            return;
        }
        Collection<ImageGalleryItem> changedCopy = new ArrayList<>(changed);
        for (ImageGalleryItem imageGalleryItem : changedCopy) {
            imageService.call(aVoid -> reload(imageGalleryItemListener), (message, throwable) -> {
                logger.log(Level.SEVERE, "save failed: " + message, throwable);
                return false;
            }).save(imageGalleryItem.getId(), imageElementLibrary.get(imageGalleryItem.getId()).getSrc());
        }
    }

    public void removeListener(int id, ImageListener listener) {
        removeListener(imageElementListeners, id, listener);
    }

    public void removeListener(int id, ImageGalleryListener listener) {
        removeListener(imageGalleryListeners, id, listener);
    }

    public void addChangeListener(ChangeListener changeListener) {
        changeListeners.add(changeListener);
    }

    public void removeChangeListener(ChangeListener changeListener) {
        changeListeners.remove(changeListener);
    }

    public Set<ImageGalleryItem> getChanged() {
        return changed;
    }

    public void preloadImages(Collection<Integer> imageIds, DeferredStartup deferredStartup) {
        ImageLoader<Integer> imageLoader = new ImageLoader<>();
        for (Integer imageId : imageIds) {
            imageLoader.addImageUrl(RestUrl.getImageServiceUrl(imageId), imageId);
        }
        imageLoader.startLoading((loadedImageElements, failed) -> {
            for (Integer imageId : failed) {
                logger.warning("Failed preload image with id: " + imageId);
            }
            for (Map.Entry<Integer, ImageElement> entry : loadedImageElements.entrySet()) {
                if (entry.getValue() == null) {
                    logger.warning("Could not preload image. ImageElement is null for id: " + entry.getKey());
                }
                imageElementLibrary.put(entry.getKey(), entry.getValue());
                currentlyLoading.remove(entry.getKey());
                fireImageElementListeners(entry.getKey(), entry.getValue());
            }
            deferredStartup.finished();
        });
    }

    private void loadImage(final int id, final boolean loadLoadImageGalleyItem) {
        if (currentlyLoading.contains(id)) {
            return;
        }
        currentlyLoading.add(id);
        ImageLoader<Integer> imageLoader = new ImageLoader<>();
        imageLoader.addImageUrl(RestUrl.getImageServiceUrl(id), id);
        imageLoader.startLoading((loadedImageElements, failed) -> {
            if (!failed.isEmpty()) {
                throw new IllegalStateException("Failed loading image with id: " + id);
            }
            ImageElement imageElement = loadedImageElements.get(id);
            if (imageElement == null) {
                throw new IllegalStateException("Failed loading texture");
            }
            currentlyLoading.remove(id);
            addImage(id, imageElement, loadLoadImageGalleyItem);
        });
    }

    private void loadImageGalleyItem(final int id, final ImageElement imageElement) {
        imageService.call(new RemoteCallback<ImageGalleryItem>() {
            @Override
            public void callback(ImageGalleryItem imageGalleryItem) {
                addImageGalleryItem(id, imageGalleryItem, imageElement);
            }
        }, (message, throwable) -> {
            logger.log(Level.SEVERE, "getImageGalleryItems failed: " + message, throwable);
            return false;
        }).getImageGalleryItem(id);
    }

    private void addImage(int id, ImageElement imageElement, boolean loadImageGalleyItem) {
        imageElementLibrary.put(id, imageElement);
        fireImageElementListeners(id, imageElement);
        if (loadImageGalleyItem) {
            loadImageGalleyItem(id, imageElement);
        }
    }

    private void addImageGalleryItem(int id, ImageGalleryItem imageGalleryItem, ImageElement imageElement) {
        imageGalleryItemLibrary.put(id, imageGalleryItem);
        fireImageGalleryListeners(id, imageElement, imageGalleryItem);
    }

    private <T> void addListener(Map<Integer, Collection<T>> idListeners, int id, T listener) {
        Collection<T> listenerCollection = idListeners.get(id);
        if (listenerCollection == null) {
            listenerCollection = new ArrayList<>();
            idListeners.put(id, listenerCollection);
        }
        listenerCollection.add(listener);

    }

    private <T> void removeListener(Map<Integer, Collection<T>> idListeners, int id, T listener) {
        Collection<T> listenerCollection = idListeners.get(id);
        if (listenerCollection == null) {
            return;
        }
        listenerCollection.remove(listener);
        if (listenerCollection.isEmpty()) {
            idListeners.remove(id);
        }
    }

    private void fireImageElementListeners(int id, ImageElement imageElement) {
        Collection<ImageListener> listeners = imageElementListeners.get(id);
        if (listeners == null) {
            return;
        }
        for (ImageListener listener : listeners) {
            listener.onLoaded(imageElement);
        }
    }

    private void fireImageGalleryListeners(int id, ImageElement imageElement, ImageGalleryItem imageGalleryItem) {
        Collection<ImageGalleryListener> listeners = imageGalleryListeners.get(id);
        if (listeners == null) {
            return;
        }
        for (ImageGalleryListener listener : listeners) {
            listener.onLoaded(imageElement, imageGalleryItem);
        }
    }

    private void fireChanged() {
        for (ChangeListener changeListener : changeListeners) {
            changeListener.onChanged(changed);
        }
    }
}
