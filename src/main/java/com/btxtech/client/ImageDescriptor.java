package com.btxtech.client;

/**
 * Created by Beat
 * 29.06.2015.
 */
public class ImageDescriptor {
    public static final ImageDescriptor ROCK_IMAGE = new ImageDescriptor("rock.jpg", 512, 512);
    public static final ImageDescriptor ROCK_1_IMAGE = new ImageDescriptor("Rock1.png", 128, 128);
    public static final ImageDescriptor ROCK_2_IMAGE = new ImageDescriptor("Rock2.png", 256, 256);
    public static final ImageDescriptor GRASS_1 = new ImageDescriptor("grass.jpg", 512, 512);
    public static final ImageDescriptor GRASS_2 = new ImageDescriptor("Grass2.png", 512, 512);
    public static final ImageDescriptor SAND_1 = new ImageDescriptor("sand1.png", 256, 256);
    public static final ImageDescriptor SAND_2 = new ImageDescriptor("sand2.png", 512, 512);
    public static final ImageDescriptor ROCK_2 = new ImageDescriptor("rock02.png", 512, 512);
    public static final ImageDescriptor ROCK_4 = new ImageDescriptor("rock04.png", 512, 512);
    public static final ImageDescriptor ROCK_5 = new ImageDescriptor("rock05.png", 512, 512);
    public static final ImageDescriptor BUMP_MAP_01 = new ImageDescriptor("BumpMap01.png", 512, 512);
    public static final ImageDescriptor BUMP_MAP_02 = new ImageDescriptor("BumpMap02.png", 512, 512);
    public static final ImageDescriptor BUMP_MAP_03 = new ImageDescriptor("BumpMap03.jpg", 512, 512);
    public static final ImageDescriptor BUMP_MAP_04 = new ImageDescriptor("BumpMap04.jpg", 512, 512);
    public static final ImageDescriptor BUMP_MAP_05 = new ImageDescriptor("BumpMap05.png", 512, 512);
    public static final ImageDescriptor BUMP_MAP_06 = new ImageDescriptor("BumpMap06.png", 512, 512);
    public static final ImageDescriptor BUMP_MAP_07 = new ImageDescriptor("BumpMap07.png", 512, 512);
    public static final ImageDescriptor BUMP_MAP_GROUND_1 = new ImageDescriptor("GroundBM1.png", 512, 512);
    public static final ImageDescriptor BUMP_MAP_GROUND_2 = new ImageDescriptor("BumpMap08.png", 512, 512);
    public static final ImageDescriptor GROUND_1 = new ImageDescriptor("Ground1.png", 512, 512);
    public static final ImageDescriptor GROUND_2 = new ImageDescriptor("Ground2.png", 512, 512);
    public static final ImageDescriptor GROUND_5 = new ImageDescriptor("Ground5.png", 512, 512);
    public static final ImageDescriptor GROUND_BM_5 = new ImageDescriptor("Ground5Bm.png", 512, 512);
    public static final ImageDescriptor GRASS_ROCK_1 = new ImageDescriptor("GrassRock01.png", 512, 512);
    public static final ImageDescriptor BLEND_1 = new ImageDescriptor("blend1.png", 512, 512);
    public static final ImageDescriptor BLEND_2 = new ImageDescriptor("blend2.png", 512, 512);
    public static final ImageDescriptor BLEND_3 = new ImageDescriptor("blend3.png", 512, 512);
    public static final ImageDescriptor BLEND_4 = new ImageDescriptor("blend4.png", 512, 512);
    public static final ImageDescriptor BUSH_1 = new ImageDescriptor("bush-texture.png", 1024, 1024);
    public static final ImageDescriptor TREE_01 = new ImageDescriptor("tree128.png", 128, 128);
    public static final ImageDescriptor BRANCH_01 = new ImageDescriptor("branch1.png", 512, 512);
    public static final ImageDescriptor TREE_TEXTURE_01 = new ImageDescriptor("TreeTexture01.png", 512, 512);
    public static final ImageDescriptor CHESS_TEXTURE_08 = new ImageDescriptor("chess08.jpg", 512, 512);
    public static final ImageDescriptor CHESS_TEXTURE_32 = new ImageDescriptor("chess32.jpg", 512, 512);
    public static final ImageDescriptor CHESS_TEXTURE_128 = new ImageDescriptor("chess128.jpg", 512, 512);
    public static final ImageDescriptor CHESS_TEXTURE_256 = new ImageDescriptor("chess256.jpg", 512, 512);
    public static final ImageDescriptor TEX_DEV_1 = new ImageDescriptor("texdev1.jpg", 256, 256);
    public static final ImageDescriptor TEX_DEV_2 = new ImageDescriptor("texdev2.jpg", 256, 256);
    public static final ImageDescriptor PICTURE_1 = new ImageDescriptor("Picture1.png", 512, 512);
    public static final ImageDescriptor PICTURE_2 = new ImageDescriptor("Picture2.png", 512, 512);
    public static final ImageDescriptor PICTURE_3 = new ImageDescriptor("Picture3.jpg", 128, 128);
    public static final ImageDescriptor BEACH_01 = new ImageDescriptor("beach01.png", 512, 512);
    public static final ImageDescriptor OLD_GAME_UNITS = new ImageDescriptor("OldGameUnitTexture.png", 512, 512);
    public static final ImageDescriptor UNIT_TEXTURE_O1 = new ImageDescriptor("UnitTexture.png", 512, 512);
    public static final ImageDescriptor CIRCLE_1 = new ImageDescriptor("circle.png", 512, 512);
    public static final ImageDescriptor CIRCLE_2 = new ImageDescriptor("circle2.png", 512, 512);
    public static final ImageDescriptor GREY = new ImageDescriptor("grey.jpg", 512, 512);

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
