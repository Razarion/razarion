package com.btxtech.client;

/**
 * Created by Beat
 * 29.06.2015.
 */
public class ImageDescriptor {
    private String url;
    private int width;
    private int height;

    public ImageDescriptor(String url, int width, int height) {
        this.url = url;
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public String getUrl() {
        return url;
    }

    public int getQuadraticEdge() {
        if (width != height) {
            throw new IllegalStateException("Width != height (" + width + ":" + height + ")" + " for image: " + url);
        }
        return width;
    }
}
