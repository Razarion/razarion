package com.btxtech.client.imageservice;

import com.btxtech.shared.ImageService;
import com.btxtech.shared.RestUrl;
import com.btxtech.shared.dto.ImageGalleryItem;
import com.google.gwt.dom.client.ImageElement;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.ErrorCallback;
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
    private Caller<ImageService> imageService;
    private Map<Integer, ImageElement> imageElementLibrary = new HashMap<>();
    private Map<Integer, ImageGalleryItem> imageGalleryItemLibrary = new HashMap<>();
    private Map<Integer, Collection<ImageListener>> imageElementListeners = new HashMap<>();
    private Map<Integer, Collection<ImageGalleryListener>> imageGalleryListeners = new HashMap<>();
    private Set<ImageGalleryItem> changed = new HashSet<>();
    private Collection<ChangeListener> changeListeners = new ArrayList<>();

    public void requestImage(final int id, ImageListener listener) {
        if(id == 0) {
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
        }, new ErrorCallback<Object>() {
            @Override
            public boolean error(Object message, Throwable throwable) {
                logger.log(Level.SEVERE, "getImageGalleryItem failed: " + message, throwable);
                return false;
            }
        }).getImageGalleryItem();
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
        imageService.call(new RemoteCallback<Void>() {
            @Override
            public void callback(Void aVoid) {
                getImageGalleryItems(imageGalleryItemListener);
            }
        }, new ErrorCallback<Object>() {
            @Override
            public boolean error(Object message, Throwable throwable) {
                logger.log(Level.SEVERE, "uploadImage failed: " + message, throwable);
                return false;
            }
        }).uploadImage(dataUrl);
    }

    public void save(final ImageGalleryItemListener imageGalleryItemListener) {
        if (changed.isEmpty()) {
            return;
        }
        Map<Integer, String> dataUrls = new HashMap<>();
        for (ImageGalleryItem imageGalleryItem : changed) {
            dataUrls.put(imageGalleryItem.getId(), imageElementLibrary.get(imageGalleryItem.getId()).getSrc());
        }
        imageService.call(new RemoteCallback<Void>() {
            @Override
            public void callback(Void aVoid) {
                reload(imageGalleryItemListener);
            }
        }, new ErrorCallback<Object>() {
            @Override
            public boolean error(Object message, Throwable throwable) {
                logger.log(Level.SEVERE, "save failed: " + message, throwable);
                return false;
            }
        }).save(dataUrls);
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

    private void loadImage(final int id, final boolean loadLoadImageGalleyItem) {
        ImageLoader<Integer> imageLoader = new ImageLoader<>();
        imageLoader.addImageUrl(RestUrl.getImageServiceUrl(id), id);
        imageLoader.startLoading(new ImageLoader.Listener<Integer>() {
            @Override
            public void onLoaded(Map<Integer, ImageElement> loadedImageElements, Collection<Integer> failed) {
                if (!failed.isEmpty()) {
                    throw new IllegalStateException("Failed loading image with id: " + id);
                }
                ImageElement imageElement = loadedImageElements.get(id);
                if (imageElement == null) {
                    throw new IllegalStateException("Failed loading texture");
                }
                addImage(id, imageElement, loadLoadImageGalleyItem);
            }
        });
    }

    private void loadImageGalleyItem(final int id, final ImageElement imageElement) {
        imageService.call(new RemoteCallback<ImageGalleryItem>() {
            @Override
            public void callback(ImageGalleryItem imageGalleryItem) {
                addImageGalleryItem(id, imageGalleryItem, imageElement);
            }
        }, new ErrorCallback<Object>() {
            @Override
            public boolean error(Object message, Throwable throwable) {
                logger.log(Level.SEVERE, "getImageGalleryItem failed: " + message, throwable);
                return false;
            }
        }).getImageGalleryItem(id);
    }

    private void addImage(int id, ImageElement imageElement, boolean loadLoadImageGalleyItem) {
        imageElementLibrary.put(id, imageElement);
        fireImageElementListeners(id, imageElement);
        if (loadLoadImageGalleyItem) {
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
        for (ImageGalleryListener listener : imageGalleryListeners.get(id)) {
            listener.onLoaded(imageElement, imageGalleryItem);
        }
    }

    private void fireChanged() {
        for (ChangeListener changeListener : changeListeners) {
            changeListener.onChanged(changed);
        }
    }
}
