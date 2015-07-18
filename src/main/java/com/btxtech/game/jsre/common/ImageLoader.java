package com.btxtech.game.jsre.common;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.user.client.Timer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * User: beat
 * Date: 16.08.2011
 * Time: 12:37:09
 */

/**
 * This is a very tricky class. Different browsers handle the image loading different
 */
public class ImageLoader<T> {
    private static final int SEND_DEBUG_DELAY = 20000;
    private Listener<T> listener;

    public interface Listener<T> {
        void onLoaded(Map<T, ImageElement> loadedImageElements, Collection<T> failed);
    }

    private Map<T, ImageElement> loadedImages;
    private Collection<T> failed;
    private Map<String, T> imagesToLoad = new HashMap<String, T>();
    private Logger log = Logger.getLogger(ImageLoader.class.getName());
    private Timer timer;

    static {
        exportStaticMethod();
    }

    public boolean isLoaded() {
        return imagesToLoad.isEmpty();
    }

    public void addImageUrl(String url, T userObject) {
        imagesToLoad.put(url, userObject);
    }

    public void startLoading(Listener<T> listener) {
        this.listener = listener;
        loadedImages = new HashMap<>();
        failed = new ArrayList<T>();
        for (final String url : imagesToLoad.keySet()) {
            loadImageNative(this, url);
        }
        if (!isLoaded()) {
            startTimer();
        }
    }

    private void onImageLoaded(String url, ImageElement imageElement) {
        T userObject = imagesToLoad.remove(url);
        if (userObject == null) {
            log.warning("ImageLoader.onImageLoaded(): url is not in imagesToLoad: " + url);
            return;
        }
        loadedImages.put(userObject, imageElement);
        checkIfFinished();
    }

    private void onImageError(String url) {
        T userObject = imagesToLoad.remove(url);
        if (userObject == null) {
            log.warning("ImageLoader.onImageError(): url is not in imagesToLoad: " + url);
            return;
        }
        failed.add(userObject);
        checkIfFinished();
    }

    private void checkIfFinished() {
        if (isLoaded()) {
            if (timer != null) {
                timer.cancel();
                timer = null;
            }
            if (listener != null) {
                listener.onLoaded(loadedImages, failed);
            }
        }
    }

    private void startTimer() {
        if (timer != null) {
            timer.cancel();
        }
//        timer = new TimerPerfmon(PerfmonEnum.IMAGE_LOADER) {
//
//            @Override
//            public void runPerfmon() {
//                timer = null;
//                checkProgress();
//            }
//        };
//        timer.schedule(SEND_DEBUG_DELAY);

        timer = new Timer() {
            @Override
            public void run() {
                timer = null;
                checkProgress();
            }
        };
        timer.schedule(SEND_DEBUG_DELAY);

    }

    private void checkProgress() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Images still not loaded: ");
        stringBuilder.append(imagesToLoad.size());
        stringBuilder.append('\n');
        for (String url : imagesToLoad.keySet()) {
            stringBuilder.append(url);
            stringBuilder.append('\n');
        }
        // TODO GwtCommon.sendDebug(GwtCommon.DEBUG_CATEGORY_IMAGE_LOADER, stringBuilder.toString());
        GWT.log(stringBuilder.toString());
        startTimer();
    }

    /**
     * To fire the onload event in GWT, the image must be added to to DOM. That's why the native is used
     */
    private native void loadImageNative(ImageLoader imageLoader, String url) /*-{
        var img = new Image();
        img.onload = function () {
            $wnd.RazOnImageLoadedCallback(imageLoader, url, img);
        };
        img.onerror = function () {
            $wnd.RazOnImageFailedCallback(imageLoader, url);
        };
        img.src = url;
    }-*/;

    private static native void exportStaticMethod() /*-{
        $wnd.RazOnImageLoadedCallback = $entry(@com.btxtech.game.jsre.common.ImageLoader::onImageLoadedNativeCallback(Lcom/btxtech/game/jsre/common/ImageLoader;Ljava/lang/String;Lcom/google/gwt/core/client/JavaScriptObject;));
        $wnd.RazOnImageFailedCallback = $entry(@com.btxtech.game.jsre.common.ImageLoader::onImageFailedNativeCallback(Lcom/btxtech/game/jsre/common/ImageLoader;Ljava/lang/String;));
    }-*/;

    public static void onImageLoadedNativeCallback(ImageLoader imageLoader, String url, JavaScriptObject imageFromJS) {
        imageLoader.onImageLoaded(url, imageFromJS.<ImageElement>cast());
    }

    public static void onImageFailedNativeCallback(ImageLoader imageLoader, String url) {
        imageLoader.onImageError(url);
    }

}
