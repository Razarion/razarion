package com.btxtech.shared.datatypes;

import com.btxtech.shared.utils.MathHelper;

import javax.persistence.Embeddable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Beat
 * 05.04.2015.
 */
@Embeddable
public class Vertex {
    public static final Vertex ZERO = new Vertex(0, 0, 0);
    public static final Vertex X_NORM = new Vertex(1, 0, 0);
    public static final Vertex Y_NORM = new Vertex(0, 1, 0);
    public static final Vertex Z_NORM = new Vertex(0, 0, 1);
    public static final Vertex Z_NORM_NEG = new Vertex(0, 0, -1);

    private double x;
    private double y;
    private double z;

    // Used by Errai
    public Vertex() {
    }

    public Vertex(double x, double y, double z) {
        if (Double.isNaN(x) || Double.isInfinite(x)) {
            throw new IllegalArgumentException("x is invalid: " + x);
        }
        if (Double.isNaN(y) || Double.isInfinite(y)) {
            throw new IllegalArgumentException("y is invalid: " + y);
        }
        if (Double.isNaN(z) || Double.isInfinite(z)) {
            throw new IllegalArgumentException("z is invalid: " + z);
        }
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vertex(DecimalPosition xy, double z) {
        if (Double.isNaN(z) || Double.isInfinite(z)) {
            throw new IllegalArgumentException("z is invalid: " + z);
        }
        this.x = xy.getX();
        this.y = xy.getY();
        this.z = z;
    }

    public Vertex(Index xy, double z) {
        if (Double.isNaN(z) || Double.isInfinite(z)) {
            throw new IllegalArgumentException("z is invalid: " + z);
        }
        this.x = xy.getX();
        this.y = xy.getY();
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

    public Vertex sub(double x, double y, double z) {
        return new Vertex(this.x - x, this.y - y, this.z - z);
    }

    public Vertex sub(Vertex other) {
        return new Vertex(x - other.x, y - other.y, z - other.z);
    }

    public Vertex multiply(double w) {
        return new Vertex(x * w, y * w, z * w);
    }

    public Vertex multiply(double x, double y, double z) {
        return new Vertex(this.x * x, this.y * y, this.z * z);
    }

    public Vertex divide(double w) {
        return new Vertex(x / w, y / w, z / w);
    }

    public Vertex negate() {
        return new Vertex(-x, -y, -z);
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

    /**
     * Project the source to this vector. This vertex is the origin.
     *
     * @param source the source of the projection
     * @return distance positive if in the same direction as canvas
     */
    public double projection(Vertex source) {
        double dotProduct = dot(source);

        return dotProduct / magnitude();
    }

    public double unsignedAngle(Vertex other) {
        return Math.acos(dot(other) / (magnitude() * other.magnitude()));
    }

    public double unsignedAngle(Vertex start, Vertex end) {
        if (start.equals(this)) {
            throw new IllegalArgumentException("Start is equals to this vertex: " + start);
        }
        if (end.equals(this)) {
            throw new IllegalArgumentException("End is equals to this vertex: " + end);
        }

        Vertex normStart = start.sub(this);
        Vertex normEnd = end.sub(this);
        double cos = normEnd.dot(normStart) / (normEnd.magnitude() * normStart.magnitude());
        if (cos > 1.0) {
            return 0;
        } else if (cos < -1.0) {
            return MathHelper.HALF_RADIANT;
        }
        return Math.acos(cos);
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

    public DecimalPosition toXZ() {
        return new DecimalPosition(x, z);
    }

    public DecimalPosition toYZ() {
        return new DecimalPosition(y, z);
    }

    public boolean equalsDelta(Vertex other, double delta) {
        return MathHelper.compareWithPrecision(x, other.x, delta) && MathHelper.compareWithPrecision(y, other.y, delta) && MathHelper.compareWithPrecision(z, other.z, delta);
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

    public static Vertex sum(Collection<Vertex> vertices) {
        Vertex sum = new Vertex(0, 0, 0);
        for (Vertex vertex : vertices) {
            sum = sum.add(vertex);
        }
        return sum;
    }

    public static List<DecimalPosition> toXY(List<? extends Vertex> vertices) {
        List<DecimalPosition> decimalPositions = new ArrayList<>();
        for (Vertex vertex : vertices) {
            decimalPositions.add(vertex.toXY());
        }
        return decimalPositions;
    }

    public static List<Vertex> toVertex(List<DecimalPosition> positions, double z) {
        return positions.stream().map(decimalPosition -> new Vertex(decimalPosition, z)).collect(Collectors.toList());
    }

    public static double[] toArray(List<Vertex> vertices) {
        if (vertices == null) {
            return null;
        }
        double[] array = new double[vertices.size() * getComponentsPerVertex()];
        for (int i = 0; i < vertices.size(); i++) {
            int arrayIndex = i * getComponentsPerVertex();
            Vertex vertex = vertices.get(i);
            array[arrayIndex] = vertex.getX();
            array[arrayIndex + 1] = vertex.getY();
            array[arrayIndex + 2] = vertex.getZ();
        }
        return array;
    }

    public static Comparator<Vertex> createVertexComparator1(double delta) {
        return (v1, v2) -> {
            if (v1.equalsDelta(v2, delta)) {
                return 0;
            }
            if (v1.getX() + v1.getY() + v1.getZ() >= v2.getX() + v2.getY() + v2.getZ()) {
                return 1;
            } else {
                return -1;
            }
        };
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
}