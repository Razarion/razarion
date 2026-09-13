package com.btxtech.shared.gameengine.datatypes.itemtype;

import jsinterop.annotations.JsType;
import org.teavm.flavour.json.JsonPersistable;

/**
 * User: Razarion contributors
 * Date: 17.11.2009
 * Time: 23:23:38
 */
@JsType
@JsonPersistable
public class HarvesterType {
    private int range;
    private double progress;

    public int getRange() {
        return range;
    }

    public void setRange(int range) {
        this.range = range;
    }

    public HarvesterType range(int range) {
        this.range = range;
        return this;
    }

    public double getProgress() {
        return progress;
    }

    public void setProgress(double progress) {
        this.progress = progress;
    }

    public HarvesterType progress(double progress) {
        this.progress = progress;
        return this;
    }
}
