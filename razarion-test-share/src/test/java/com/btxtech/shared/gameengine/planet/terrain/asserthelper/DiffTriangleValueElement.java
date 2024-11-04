package com.btxtech.shared.gameengine.planet.terrain.asserthelper;

import com.btxtech.shared.utils.MathHelper;

/**
 * Created by Beat
 * 11.04.2017.
 */
public class DiffTriangleValueElement extends DiffTriangleElement {
    private double delta;
    private double[] actual;

    public DiffTriangleValueElement(int scalarIndex, double delta, double[] expected, double[] actual) {
        super(scalarIndex, expected);
        this.delta = delta;
        this.actual = actual;
        dumpDiff();
    }

    @Override
    public Difference getDifference() {
        boolean x1 = !MathHelper.compareWithPrecision(getVertices()[getScalarIndex()], actual[getScalarIndex()], delta);
        boolean y1 = !MathHelper.compareWithPrecision(getVertices()[getScalarIndex() + 1], actual[getScalarIndex() + 1], delta);
        boolean z1 = !MathHelper.compareWithPrecision(getVertices()[getScalarIndex() + 2], actual[getScalarIndex() + 3], delta);
        boolean x2 = !MathHelper.compareWithPrecision(getVertices()[getScalarIndex() + 3], actual[getScalarIndex() + 3], delta);
        boolean y2 = !MathHelper.compareWithPrecision(getVertices()[getScalarIndex() + 4], actual[getScalarIndex() + 4], delta);
        boolean z2 = !MathHelper.compareWithPrecision(getVertices()[getScalarIndex() + 5], actual[getScalarIndex() + 5], delta);
        boolean x3 = !MathHelper.compareWithPrecision(getVertices()[getScalarIndex() + 6], actual[getScalarIndex() + 6], delta);
        boolean y3 = !MathHelper.compareWithPrecision(getVertices()[getScalarIndex() + 7], actual[getScalarIndex() + 7], delta);
        boolean z3 = !MathHelper.compareWithPrecision(getVertices()[getScalarIndex() + 8], actual[getScalarIndex() + 8], delta);

        boolean xy = x1 || x2 || x3 || y1 || y2 || y3;
        boolean z = z1 || z2 || z3;

        if (xy && z) {
            return Difference.XYZ;
        } else if (xy) {
            return Difference.XY;
        } else if (z) {
            return Difference.Z;
        } else {
            throw new IllegalStateException();
        }
    }

    private void dumpDiff() {
        System.out.println("-------------------------------------------------------------\n");
        System.out.println(String.format("Expected A %.4f:%.4f:%.4f\tB %.4f:%.4f:%.4f\tC %.4f:%.4f:%.4f", getVertices()[getScalarIndex()], getVertices()[getScalarIndex() + 1], getVertices()[getScalarIndex() + 2], getVertices()[getScalarIndex() + 3], getVertices()[getScalarIndex() + 4], getVertices()[getScalarIndex() + 5], getVertices()[getScalarIndex() + 6], getVertices()[getScalarIndex() + 7], getVertices()[getScalarIndex() + 8]));
        System.out.println(String.format("  Actual A %.4f:%.4f:%.4f\tB %.4f:%.4f:%.4f\tC %.4f:%.4f:%.4f", actual[getScalarIndex()], actual[getScalarIndex() + 1], actual[getScalarIndex() + 2], actual[getScalarIndex() + 3], actual[getScalarIndex() + 4], actual[getScalarIndex() + 5], actual[getScalarIndex() + 6], actual[getScalarIndex() + 7], actual[getScalarIndex() + 8]));
    }
}
