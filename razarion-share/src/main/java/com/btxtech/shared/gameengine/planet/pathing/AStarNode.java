package com.btxtech.shared.gameengine.planet.pathing;

import com.btxtech.shared.gameengine.planet.terrain.container.PathingNodeWrapper;

/**
 * Created by beat
 * on 10.11.2014.
 */
public class AStarNode implements Comparable<AStarNode> {
    private final PathingNodeWrapper pathingNodeWrapper;
    private double f = 0;
    private double g; // Cost to this node
    private AStarNode predecessor;

    AStarNode(PathingNodeWrapper pathingNodeWrapper) {
        this.pathingNodeWrapper = pathingNodeWrapper;
    }

    @Override
    public int hashCode() {
        return pathingNodeWrapper.hashCode();
    }

    public PathingNodeWrapper getPathingNodeWrapper() {
        return pathingNodeWrapper;
    }

    public double getF() {
        return f;
    }

    public void setF(double f) {
        this.f = f;
    }

    public double getG() {
        return g;
    }

    public void setG(double g) {
        this.g = g;
    }

    public void setPredecessor(AStarNode predecessor) {
        this.predecessor = predecessor;
    }

    public AStarNode getPredecessor() {
        return predecessor;
    }

    @Override
    public int compareTo(AStarNode o) {
        return Double.compare(f, o.f);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AStarNode node = (AStarNode) o;

        return pathingNodeWrapper.equals(node.pathingNodeWrapper);
    }
}
