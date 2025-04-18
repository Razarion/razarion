package com.btxtech.shared.gameengine.planet.terrain.container;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.stream.Collectors;

/**
 * Created by Beat
 * on 13.07.2017.
 */
public class NodeHandlerHelper {
    private int actualCount;
    private Collection<Index> expectedNode = new ArrayList<>();
    private Collection<DecimalPosition> expectedSubNodeDepth0 = new ArrayList<>();
    private Collection<DecimalPosition> expectedSubNodeDepth1 = new ArrayList<>();
    private Collection<DecimalPosition> expectedSubNodeDepth2 = new ArrayList<>();
    private Collection<PathingNodeWrapper> actual = new ArrayList<>();

    public void addExpectedNode(Index nodeIndex) {
        expectedNode.add(nodeIndex);
    }

    public void addExpectedSubNode0(DecimalPosition subNodePosition) {
        expectedSubNodeDepth0.add(subNodePosition);
    }

    public void addExpectedSubNode1(DecimalPosition subNodePosition) {
        expectedSubNodeDepth1.add(subNodePosition);
    }

    public void addExpectedSubNode2(DecimalPosition subNodePosition) {
        expectedSubNodeDepth2.add(subNodePosition);
    }

    public void addActual(PathingNodeWrapper pathingNodeWrapper) {
        System.out.println("Actual PathingNodeWrapper: " + pathingNodeWrapper);
        actual.add(pathingNodeWrapper);
    }

    public void doAssert() {
        Assert.assertEquals(actual.size(), expectedNode.size() + expectedSubNodeDepth0.size() + expectedSubNodeDepth1.size() + expectedSubNodeDepth2.size());
        // Assert nodes
        if (!expectedNode.isEmpty()) {
            for (Iterator<PathingNodeWrapper> iterator = actual.iterator(); iterator.hasNext(); ) {
                PathingNodeWrapper pathingNodeWrapper = iterator.next();
                if (pathingNodeWrapper.getNodeIndex() != null) {
                    if (!expectedNode.remove(pathingNodeWrapper.getNodeIndex())) {
                        Assert.fail("Node not expected: " + pathingNodeWrapper.getNodeIndex());
                    }
                    iterator.remove();
                }
            }
            if (!expectedNode.isEmpty()) {
                Assert.fail("No all expected node are in the actual collection: " + expectedNode.stream().map(index -> index.toString() + ", ").collect(Collectors.joining()));
            }
        }
        assertSubNodes(expectedSubNodeDepth0, 0);
        assertSubNodes(expectedSubNodeDepth1, 1);
        assertSubNodes(expectedSubNodeDepth2, 2);

        if(!actual.isEmpty()) {
            Assert.fail("Unexpected PathingNodeWrapper: " + actual.stream().map(index -> index.toString() + ", ").collect(Collectors.joining()));
        }
    }

    private void assertSubNodes(Collection<DecimalPosition> expectedSubNodeDepthX, int depth) {
        if (!expectedSubNodeDepthX.isEmpty()) {
            for (Iterator<PathingNodeWrapper> iterator = actual.iterator(); iterator.hasNext(); ) {
                PathingNodeWrapper pathingNodeWrapper = iterator.next();
//                if (pathingNodeWrapper.getTerrainShapeSubNode() != null && pathingNodeWrapper.getTerrainShapeSubNode().getDepth() == depth) {
//                    if (!expectedSubNodeDepthX.remove(pathingNodeWrapper.getSubNodePosition())) {
//                        Assert.fail("Sub node with depth '" + depth + "' not expected: " + pathingNodeWrapper.getSubNodePosition());
//                    }
//                    iterator.remove();
//                }
            }
            if (!expectedSubNodeDepthX.isEmpty()) {
                Assert.fail("No all expected sub node with depth '" + depth + "' are in the actual collection: " + expectedSubNodeDepthX.stream().map(index -> index.toString() + ", ").collect(Collectors.joining()));
            }
        }
    }

}
