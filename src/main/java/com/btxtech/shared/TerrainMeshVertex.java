package com.btxtech.shared;

import com.btxtech.game.jsre.client.common.Index;
import com.btxtech.shared.primitives.Vertex;
import org.jboss.errai.common.client.api.annotations.Portable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

/**
 * Created by Beat
 * 06.12.2015.
 */
@Entity
@Portable
@Table(name = "TERRAIN_MESH_VERTEX")
@IdClass(TerrainMeshVertexId.class)
public class TerrainMeshVertex {
    @Id
    private int meshIndexX;
    @Id
    private int meshIndexY;
    private double x;
    private double y;
    private double z;
    private double edge;
    private double slopeFactor;

    /**
     * Used by JPA & errai
     *
     */
    public TerrainMeshVertex() {
    }

    public TerrainMeshVertex(Index meshIndex, Vertex vertex, double edge, double slopeFactor) {
        meshIndexX = meshIndex.getX();
        meshIndexY = meshIndex.getY();
        x = vertex.getX();
        y = vertex.getY();
        z = vertex.getZ();
        this.edge = edge;
        this.slopeFactor = slopeFactor;
    }

    public int getMeshIndexX() {
        return meshIndexX;
    }

    public void setMeshIndexX(int meshPosX) {
        this.meshIndexX = meshPosX;
    }

    public int getMeshIndexY() {
        return meshIndexY;
    }

    public void setMeshIndexY(int meshPosy) {
        this.meshIndexY = meshPosy;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public double getEdge() {
        return edge;
    }

    public void setEdge(double edge) {
        this.edge = edge;
    }

    public double getSlopeFactor() {
        return slopeFactor;
    }

    public void setSlopeFactor(double slopeFactor) {
        this.slopeFactor = slopeFactor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TerrainMeshVertex that = (TerrainMeshVertex) o;
        return meshIndexX == that.meshIndexX && meshIndexY == that.meshIndexY;
    }

    @Override
    public int hashCode() {
        int result = meshIndexX;
        result = 31 * result + meshIndexY;
        return result;
    }
}
