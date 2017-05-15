package com.btxtech.server.persistence.surface;

import com.btxtech.shared.dto.SlopeNode;
import com.btxtech.shared.datatypes.Vertex;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
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
    @GeneratedValue(strategy = GenerationType.TABLE)
    private Integer id;
    private int segmentIndex;
    private int rowIndex;
    @Embedded
    private Vertex position;
    private double slopeFactor;

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
        return slopeNode;
    }

    public void fromSlopeNode(int segmentIndex, int rowIndex, SlopeNode slopeNode) {
        this.segmentIndex = segmentIndex;
        this.rowIndex = rowIndex;
        position = slopeNode.getPosition();
        slopeFactor = slopeNode.getSlopeFactor();
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
