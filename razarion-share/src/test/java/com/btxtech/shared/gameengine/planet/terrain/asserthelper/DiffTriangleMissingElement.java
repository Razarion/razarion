package com.btxtech.shared.gameengine.planet.terrain.asserthelper;

/**
 * Created by Beat
 * on 17.01.2018.
 */
public class DiffTriangleMissingElement extends DiffTriangleElement {
    private Difference difference;

    public DiffTriangleMissingElement(Difference difference, TriangleElement triangleElement) {
        super(triangleElement.getScalarIndex(), triangleElement.getVertices());
        this.difference = difference;
        dump();
    }

    @Override
    public Difference getDifference() {
        return difference;
    }

    private void dump() {
        System.out.println("-------------------------------------------------------------\n");
        System.out.println(String.format(difference + " A %.4f:%.4f:%.4f\tB %.4f:%.4f:%.4f\tC %.4f:%.4f:%.4f", getVertices()[getScalarIndex()], getVertices()[getScalarIndex() + 1], getVertices()[getScalarIndex() + 2], getVertices()[getScalarIndex() + 3], getVertices()[getScalarIndex() + 4], getVertices()[getScalarIndex() + 5], getVertices()[getScalarIndex() + 6], getVertices()[getScalarIndex() + 7], getVertices()[getScalarIndex() + 8]));
    }

}
