package com.btxtech.server.terrain.surface;

import com.btxtech.shared.dto.SlopeNode;
import com.btxtech.shared.primitives.Vertex;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by Beat
 * 13.02.2016.
 */
@Entity
@Table(name = "SLOPE_NODE")
public class SlopeNodeEntity {
    @Id
    @GeneratedValue
    private Long id;
    private int segmentIndex;
    private int rowIndex;
    @Embedded
    private Vertex position;
    private double slopeFactor;
    private double normShift;

    public int getSegmentIndex() {
        return segmentIndex;
    }

    public int getRowIndex() {
        return rowIndex;
    }

    public SlopeNode toSlopeNode() {
        SlopeNode slopeNode = new SlopeNode();
        slopeNode.setPosition(position);
        slopeNode.setSlopeFactor(slopeFactor);
        slopeNode.setNormShift(normShift);
        return slopeNode;
    }

    public void fromSlopeNode(int segmentIndex, int rowIndex, SlopeNode slopeNode) {
        this.segmentIndex = segmentIndex;
        this.rowIndex = rowIndex;
        position = slopeNode.getPosition();
        slopeFactor = slopeNode.getSlopeFactor();
        normShift = slopeNode.getNormShift();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SlopeNodeEntity that = (SlopeNodeEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
}
