package com.btxtech.client;

/**
 * Created by Beat
 * 29.06.2015.
 */
public class ImageDescriptor {
    public static final ImageDescriptor ROCK_IMAGE = new ImageDescriptor("rock.jpg", 512, 512);
    public static final ImageDescriptor ROCK_1_IMAGE = new ImageDescriptor("Rock1.png", 128, 128);
    public static final ImageDescriptor ROCK_2_IMAGE = new ImageDescriptor("Rock2.png", 256, 256);
    public static final ImageDescriptor GRASS_IMAGE = new ImageDescriptor("grass.jpg", 512, 512);
    public static final ImageDescriptor SAND_1 = new ImageDescriptor("sand1.png", 256, 256);
    public static final ImageDescriptor SAND_2 = new ImageDescriptor("sand2.png", 512, 512);
    public static final ImageDescriptor BLEND_1 = new ImageDescriptor("blend1.png", 512, 512);
    public static final ImageDescriptor BLEND_2 = new ImageDescriptor("blend2.png", 512, 512);
    public static final ImageDescriptor BUSH_1 = new ImageDescriptor("bush-texture.png", 1024, 1024);
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
