package com.btxtech.uiservice.item;

/**
 * What a quest tip needs to know about one base item, whether it is on screen or not.
 * <p>
 * Read from the tick infos the worker delivers for every item of the planet. The tips used to ask
 * the rendered instance instead, and an item off screen has none: an own unit that was never on
 * screen could not be pointed at, and a harvester off screen made the harvest tip throw.
 */
public class TipItemState {
    public int id;
    public int itemTypeId;
    public boolean own;
    public double x;
    public double y;
    public boolean idle;
    /** 1 when finished; construction sites are below. */
    public double buildup;
    /** Item types a factory has queued, the one in production first; empty for anything else. */
    public int[] factoryBuildQueue;
}
