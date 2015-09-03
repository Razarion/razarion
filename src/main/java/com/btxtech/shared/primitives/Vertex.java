package com.btxtech.shared.primitives;

import com.btxtech.game.jsre.client.common.DecimalPosition;
import org.jboss.errai.common.client.api.annotations.Portable;

import java.util.Collection;
import java.util.List;

/**
 * Created by Beat
 * 05.04.2015.
 */
@Portable
public class Vertex {
    private double x;
    private double y;
    private double z;

    // Used by Errai
    public Vertex() {
    }

    public Vertex(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public Vertex add(double x, double y, double z) {
        return new Vertex(this.x + x, this.y + y, this.z + z);
    }

    public Vertex add(Vertex other) {
        return new Vertex(x + other.x, y + other.y, z + other.z);
    }

    public Vertex sub(Vertex other) {
        return new Vertex(x - other.x, y - other.y, z - other.z);
    }

    public Vertex multiply(double w) {
        return new Vertex(x * w, y * w, z * w);
    }

    public Vertex divide(double w) {
        return new Vertex(x / w, y / w, z / w);
    }

    public double magnitude() {
        return Math.sqrt(x * x + y * y + z * z);
    }

    public Vertex normalize(double length) {
        return multiply(length / magnitude());
    }

    public double distance(Vertex vertex) {
        return Math.sqrt(Math.pow(x - vertex.x, 2) + Math.pow(y - vertex.y, 2) + Math.pow(z - vertex.z, 2));
    }

    public double dot(Vertex vertex) {
        return x * vertex.x + y * vertex.y + z * vertex.z;
    }

    public Vertex cross(Vertex vertexB) {
        return new Vertex(y * vertexB.z - z * vertexB.y,
                z * vertexB.x - x * vertexB.z,
                x * vertexB.y - y * vertexB.x);
    }

    public Vertex cross(Vertex vertexB, Vertex vertexC) {
        return vertexB.sub(this).cross(vertexC.sub(this));
    }

    /**
     * Project the source to the canvas. This vertex is the origin.
     *
     * @param canvas the target of the projection
     * @param source the source of the projection
     * @return distance positive if in the same direction as canvas
     */
    public double projection(Vertex canvas, Vertex source) {
        if (canvas.magnitude() == 0.0) {
            throw new IllegalArgumentException("Magnitude of canvas is not allowed to be 0");
        }

        Vertex shiftCanvas = canvas.sub(this);
        Vertex shiftSource = source.sub(this);

        double dotProduct = shiftCanvas.dot(shiftSource);

        return dotProduct / shiftCanvas.magnitude();
    }

    public double unsignedAngle(Vertex other) {
        return Math.acos(dot(other) / (magnitude() * other.magnitude()));
    }

    public double unsignedAngle(Vertex start, Vertex end) {
        Vertex normStart = start.sub(this);
        Vertex normEnd = end.sub(this);
        return Math.acos(normEnd.dot(normStart) / (normEnd.magnitude() * normStart.magnitude()));
    }

    public Vertex interpolate(double distance, Vertex directionTo) {
        Vertex direction = directionTo.sub(this);
        return direction.multiply(distance / direction.magnitude()).add(this);
    }

    public List<Double> appendTo(List<Double> doubleList) {
        doubleList.add(x);
        doubleList.add(y);
        doubleList.add(z);
        return doubleList;
    }

    public DecimalPosition toXY() {
        return new DecimalPosition(x, y);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Vertex vertex = (Vertex) o;
        return Double.compare(vertex.x, x) == 0 && Double.compare(vertex.y, y) == 0 && Double.compare(vertex.z, z) == 0;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(x);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(y);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(z);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    public static int getComponentsPerVertex() {
        return 3;
    }

    @Override
    public String toString() {
        return "Vertex{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }

    public String testString() {
        return "new Vertex(" + getX() + ", " + getY() + ", " + getZ() + ")";
    }

    public static Vertex sum(Collection<Vertex> vertices) {
        Vertex sum = new Vertex(0, 0, 0);
        for (Vertex vertex : vertices) {
            sum = sum.add(vertex);
        }
        return sum;
    }
}