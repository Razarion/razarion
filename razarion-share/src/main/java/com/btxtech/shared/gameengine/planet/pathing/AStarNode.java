package com.btxtech.shared.gameengine.planet.pathing;

import com.btxtech.shared.datatypes.Index;

/**
 * Created by beat
 * on 10.11.2014.
 */
class AStarNode implements Comparable<AStarNode> {
    private Index tileIndex;
    private double f = 0;
    private double g; // Cost to this node
    private AStarNode predecessor;

    AStarNode(Index tileIndex) {
        this.tileIndex = tileIndex;
    }

    @Override
    public int hashCode() {
        return tileIndex.hashCode();
    }

    public Index getTileIndex() {
        return tileIndex;
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

        return tileIndex.equals(node.tileIndex);
    }
}
