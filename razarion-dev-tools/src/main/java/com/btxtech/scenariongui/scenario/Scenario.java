package com.btxtech.scenariongui.scenario;

import com.btxtech.ExtendedGraphicsContext;
import com.btxtech.shared.datatypes.DecimalPosition;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 25.06.2016.
 */
public abstract class Scenario {


    abstract public void render(ExtendedGraphicsContext extendedGraphicsContext);

    /**
     * Override in subclasses
     */
    public void init() {
    }

    /**
     * Override in subclasses
     *
     * @param position mouse position
     * @return true if scene should be redraw
     */
    public boolean onMouseDown(DecimalPosition position) {
        return false;
    }

    /**
     * Override in subclasses
     *
     * @param position mouse position
     * @return true if scene should be redraw
     */
    public boolean onMouseMove(DecimalPosition position) {
        return false;
    }

    public void onGenerate() {
        System.out.println("---- onGenerate() not overridden ---");
    }

    public void onCmd1() {
        System.out.println("---- onCmd1() not overridden ---");
    }
}
