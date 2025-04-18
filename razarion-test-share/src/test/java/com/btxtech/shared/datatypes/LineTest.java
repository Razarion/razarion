package com.btxtech.shared.datatypes;

import com.btxtech.shared.TestHelper;
import com.btxtech.shared.gui.AbstractTestGuiRenderer;
import com.btxtech.shared.gui.ShareTestGuiDisplay;
import com.btxtech.shared.utils.CollectionUtils;
import javafx.scene.paint.Color;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collection;

public class LineTest {

    @Test
    public void circleLineIntersection1() {
        assertIntersections(new DecimalPosition(19.6971, 3.473),
                new DecimalPosition(-19.697, -3.473),
                new Line(new DecimalPosition(0.0, 0.0), Math.toRadians(10), 1000));
    }

    @Test
    public void circleLineIntersection2() {
        assertIntersections(new DecimalPosition(4.352, 19.521),
                new DecimalPosition(-10.766, 16.855),
                new Line(new DecimalPosition(-36.6, 12.3), Math.toRadians(10), 1000));
    }

    @Test
    public void circleLineIntersection3() {
        assertIntersections(new DecimalPosition(0, 20),
                new DecimalPosition(0, -20),
                new Line(new DecimalPosition(0, -25), Math.toRadians(90), 1000));
    }

    @Test
    public void circleLineIntersection4() {
        assertIntersections(new DecimalPosition(0, 20),
                new DecimalPosition(0, -20),
                new Line(new DecimalPosition(0, -25), Math.toRadians(270), 1000));
    }

    @Test
    public void circleLineIntersection5() {
        assertIntersections(new DecimalPosition(8.718, -18.0),
                new DecimalPosition(-8.718, -18.0),
                new Line(new DecimalPosition(0, -18), Math.toRadians(0), 1000));
    }

    @Test
    public void circleLineIntersection6() {
        assertIntersections(new DecimalPosition(-8.718, -18.0),
                new DecimalPosition(8.718, -18.0),
                new Line(new DecimalPosition(0, -18), Math.toRadians(180), 1000));
    }

    @Test
    public void circleLineIntersectionToFar() {
        Assert.assertNull(new Line(new DecimalPosition(21, -25), Math.toRadians(90), 1000).circleLineIntersection(20));
    }

    private void assertIntersections(DecimalPosition expected1, DecimalPosition expected2, Line line) {
        Collection<DecimalPosition> intersctions = line.circleLineIntersection(20);
        TestHelper.assertDecimalPosition("expected1 not matching", expected1, CollectionUtils.getFirst(intersctions));
        TestHelper.assertDecimalPosition("expected2 not matching", expected2, CollectionUtils.getLast(intersctions));
    }

    // @Test
    public void gui() {
        ShareTestGuiDisplay.show(new AbstractTestGuiRenderer() {
            private DecimalPosition position = DecimalPosition.NULL;

            @Override
            protected void doRender() {
                System.out.println("----------------------------------------");
                System.out.println(position);
                Line line = new Line(position, Math.toRadians(0), 1000);
                strokeLine(line, 0.2, Color.BLUE);
                try {
                    Collection<DecimalPosition> intersctions = line.circleLineIntersection(20);
                    if (intersctions == null) {
                        return;
                    }
                    strokeDecimalPosition(CollectionUtils.getFirst(intersctions), 0.5, Color.RED);
                    System.out.println(CollectionUtils.getFirst(intersctions));
                    strokeDecimalPosition(CollectionUtils.getLast(intersctions), 0.5, Color.RED);
                    System.out.println(CollectionUtils.getLast(intersctions));
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }

            @Override
            protected boolean onMouseMoved(DecimalPosition position) {
                this.position = position;
                return true;
            }
        });
    }
}