package com.btxtech.shared.gameengine.planet.terrain.asserthelper;

import com.btxtech.shared.utils.MathHelper;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Beat
 * on 16.01.2018.
 */
public class DifferenceCollector {
    private Collection<DiffTriangleElement> differenceTriangleElements = new ArrayList<>();

    public void compareArray(String message, double[] expected, double[] actual, double delta) {
        if (actual == null && expected == null) {
            return;
        }
        if (expected == null) {
            throw new IllegalArgumentException("Unexpected");
//             differenceCollectorElement.setActual(actual);
//             differenceTriangleElements.add(differenceCollectorElement);
//             return;
        }
        if (actual == null) {
            throw new IllegalArgumentException("Unexpected");
//             differenceCollectorElement.setExpected(expected);
//             differenceTriangleElements.add(differenceCollectorElement);
//             return;
        }
        if (expected.length != actual.length) {
            Collection<TriangleElement> expectedTriangles = createTriangles(expected, delta);
            Collection<TriangleElement> actualTriangles = createTriangles(actual, delta);
            expectedTriangles.stream().filter(expectedTriangle -> !actualTriangles.remove(expectedTriangle)).forEach(expectedTriangle -> differenceTriangleElements.add(new DiffTriangleMissingElement(DiffTriangleElement.Difference.MISSING, expectedTriangle)));
            actualTriangles.forEach(triangleElement -> differenceTriangleElements.add(new DiffTriangleMissingElement(DiffTriangleElement.Difference.UNEXPECTED, triangleElement)));
            return;
        }
        for (int i = 0; i < expected.length; i += 9) {
            DiffTriangleElement diff = compareTriangle(i, delta, expected, actual);
            if (diff != null) {
                differenceTriangleElements.add(diff);
            }
        }
    }

    private DiffTriangleElement compareTriangle(int index, double delta, double[] expected, double[] actual) {
        return compareTriangle(index, index, delta, expected, actual);
    }

    private DiffTriangleElement compareTriangle(int expectedIndex, int actualIndex, double delta, double[] expected, double[] actual) {
        if (!compareVertex(expectedIndex, actualIndex, delta, expected, actual)) {
            return new DiffTriangleValueElement(expectedIndex, delta, expected, actual);
        }
        if (!compareVertex(expectedIndex + 3, actualIndex + 3, delta, expected, actual)) {
            return new DiffTriangleValueElement(expectedIndex + 3, delta, expected, actual);
        }
        if (!compareVertex(expectedIndex + 6, actualIndex + 6, delta, expected, actual)) {
            return new DiffTriangleValueElement(expectedIndex + 6, delta, expected, actual);
        }
        return null;
    }

    private boolean compareVertex(int expectedIndex, int actualIndex, double delta, double[] expected, double[] actual) {
        return MathHelper.compareWithPrecision(expected[expectedIndex], actual[actualIndex], delta) && MathHelper.compareWithPrecision(expected[expectedIndex + 1], actual[actualIndex + 1], delta) && MathHelper.compareWithPrecision(expected[expectedIndex + 2], actual[actualIndex + 2], delta);
    }

    public Collection<DiffTriangleElement> getDifferenceTriangleElements() {
        return differenceTriangleElements;
    }

    private Collection<TriangleElement> createTriangles(double[] vertices, double delta) {
        Collection<TriangleElement> triangles = new ArrayList<>();
        for (int scalarIndex = 0; scalarIndex < vertices.length; scalarIndex += 9) {
            triangles.add(new TriangleElement(vertices, scalarIndex, delta));
        }
        return triangles;
    }

}
