package com.btxtech.client.terrain;

import com.btxtech.client.ImageDescriptor;
import com.btxtech.game.jsre.client.common.Index;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by Beat
 * 20.04.2015.
 */
public class Terrain {
    private static Terrain INSTANCE = new Terrain();
    public static final ImageDescriptor ROCK_IMAGE = new ImageDescriptor("rock.jpg", 512, 512);
    public static final ImageDescriptor ROCK_1_IMAGE = new ImageDescriptor("Rock1.png", 128, 128);
    public static final ImageDescriptor ROCK_2_IMAGE = new ImageDescriptor("Rock2.png", 256, 256);
    public static final ImageDescriptor GRASS_IMAGE = new ImageDescriptor("grass.jpg", 512, 512);
    public static final ImageDescriptor SAND_1 = new ImageDescriptor("sand1.png", 256, 256);

    // public static List<Index> corners = Arrays.asList(new Index(364, 120), new Index(177, 190), new Index(160, 307), new Index(170, 525), new Index(270, 610), new Index(385, 650), new Index(610, 660), new Index(708, 603), new Index(808, 438), new Index(726, 240), new Index(483, 93));
    // public static List<Index> corners = Arrays.asList(new Index(44, 58), new Index(178, 516), new Index(954, 114));
    // public static List<Index> corners = Arrays.asList(new Index(256, 396), new Index(677, 396), new Index(557, 66));
    // public static List<Index> corners = Arrays.asList(new Index(256, 396), new Index(677, 396), new Index(508, 200));
    // public static List<Index> corners = Arrays.asList(new Index(253, 421), new Index(677, 396), new Index(508, 200));
    // private List<Index> corners = Arrays.asList(new Index(253, 292), new Index(716, 236), new Index(536, 552));
    private List<Index> corners = Arrays.asList(new Index(572, 504), new Index(381, 628), new Index(177, 372), new Index(249, 328), new Index(368, 365), new Index(438, 323), new Index(574, 321), new Index(596, 442));
    private Plateau plateau;
    private Ground ground = new Ground(0, 0, 0, 1000, 1000);

    public static Terrain getInstance() {
        return INSTANCE;
    }

    public Terrain() {
        plateau = new Plateau(corners);
    }

    public List<Index> getCorners() {
        return Collections.unmodifiableList(corners);
    }

    public void setCorners(List<Index> corners) {
        this.corners = new ArrayList<>(corners);
        plateau.updateCorners(corners);
    }

    public Ground getGround() {
        return ground;
    }

    public PlateauTop getPlateauTop() {
        return plateau.getPlateauTop();
    }

    public Plateau getPlateau() {
        return plateau;
    }

    public double getRoughness() {
        return plateau.getRoughness();
    }

    public void setRoughness(double roughness) {
        plateau.setRoughness(roughness);
    }
}
