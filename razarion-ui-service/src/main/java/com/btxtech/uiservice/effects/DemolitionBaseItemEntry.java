package com.btxtech.uiservice.effects;


/**
 * Created by Beat
 * 10.02.2017.
 */
public class DemolitionBaseItemEntry {
    private int demolitionStep;

    public DemolitionBaseItemEntry() {
        demolitionStep = -1;
    }

    public int getDemolitionStep() {
        return demolitionStep;
    }

    public void setDemolitionStep(int demolitionStep) {
        this.demolitionStep = demolitionStep;
    }

    public void disposeParticles() {
    }
}
