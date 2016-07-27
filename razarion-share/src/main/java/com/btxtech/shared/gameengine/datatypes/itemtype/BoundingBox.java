package com.btxtech.shared.gameengine.datatypes.itemtype;


import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.gameengine.datatypes.syncobject.SyncItemArea;
import com.btxtech.shared.utils.MathHelper;

import java.io.Serializable;

/**
 * User: beat
 * Date: 17.08.2011
 * Time: 13:00:05
 */
public class BoundingBox implements Serializable {
    private int radius;
    private int diameter;
    private double[] angels;
    private int cosmeticAngelIndex;
    private double cosmeticAngel;

    /**
     * Used by GWT
     */
    protected BoundingBox() {
    }

    public BoundingBox(int radius, double[] angels) {
        this.radius = radius;
        diameter = 2 * radius;
        this.angels = angels;
        if (angels.length == 0) {
            cosmeticAngelIndex = 0;
            cosmeticAngel = 0;
        } else {
            cosmeticAngelIndex = angels.length / 8;
            cosmeticAngel = angels[cosmeticAngelIndex];
        }
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
        diameter = 2 * radius;
    }

    public int getDiameter() {
        return diameter;
    }

    public boolean isTurnable() {
        return angels.length > 1;
    }

    public double getCosmeticAngel() {
        return cosmeticAngel;
    }

    public int getCosmeticAngelIndex() {
        return cosmeticAngelIndex;
    }

    public SyncItemArea createSyntheticSyncItemArea(Index destination) {
        SyncItemArea syncItemArea = new SyncItemArea(this, null);
        syncItemArea.setPositionNoCheck(destination);
        return syncItemArea;
    }

    public SyncItemArea createSyntheticSyncItemArea(Index destination, double angel) {
        SyncItemArea syncItemArea = createSyntheticSyncItemArea(destination);
        syncItemArea.setAngel(angel);
        return syncItemArea;
    }

    public double getAllowedAngel(double angel) {
        angel = MathHelper.normaliseAngel(angel);
        double angel1 = angels[0];
        for (double angel2 : angels) {
            double result = MathHelper.closerToAngel(angel, angel1, angel2);
            if (angel2 == result) {
                angel1 = angel2;
            }
        }
        return angel1;
    }

    public double getAllowedAngel(double angel, double exceptThatAngel) {
        angel = MathHelper.normaliseAngel(angel);
        exceptThatAngel = MathHelper.normaliseAngel(exceptThatAngel);
        if (MathHelper.compareWithPrecision(angel, exceptThatAngel)) {
            for (int i = 0; i < angels.length; i++) {
                double allowedAngel = angels[i];
                if (MathHelper.compareWithPrecision(allowedAngel, exceptThatAngel)) {
                    double prevAngel = angels[i == 0 ? angels.length - 1 : i - 1];
                    double nextAngel = angels[i == angels.length - 1 ? 0 : i + 1];
                    if (MathHelper.getAngel(angel, prevAngel) < MathHelper.getAngel(angel, nextAngel)) {
                        return prevAngel;
                    } else {
                        return nextAngel;
                    }
                }
            }
            throw new IllegalArgumentException("exceptThatAngel is unknown:" + exceptThatAngel);
        } else {
            for (int i = 0; i < angels.length; i++) {
                double allowedAngel = angels[i];
                if (MathHelper.compareWithPrecision(allowedAngel, exceptThatAngel)) {
                    if (MathHelper.isCounterClock(angel, exceptThatAngel)) {
                        return angels[i == 0 ? angels.length - 1 : i - 1];
                    } else {
                        return angels[i == angels.length - 1 ? 0 : i + 1];
                    }
                }
            }
            throw new IllegalArgumentException("exceptThatAngel is unknown:" + exceptThatAngel);
        }
    }

    public double[] getAngels() {
        return angels;
    }

    public void setAngels(double[] angels) {
        this.angels = angels;
    }

    public int getAngelCount() {
        if (angels.length == 0) {
            return 1;
        } else {
            return angels.length;
        }
    }

    /**
     * @param angelIndex 0..x
     * @return allowed angel
     */
    public double angelIndexToAngel(int angelIndex) {
        return angels[angelIndex];
    }

    public int angelToAngelIndex(double angel) {
        if (angels.length == 0) {
            return 0;
        }
        // TODO slow !!! Is called in every render frame
        angel = getAllowedAngel(angel);
        for (int i = 0; i < angels.length; i++) {
            double allowedAngel = angels[i];
            if (MathHelper.compareWithPrecision(allowedAngel, angel)) {
                return i;
            }

        }
        throw new IllegalArgumentException("angelToImageNr angel is unknown:" + angel);
    }


    @Override
    public String toString() {
        return "BoundingBox: radius: " + radius + " angels: " + angels.length;
    }
}
