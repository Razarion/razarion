package com.btxtech.shared.gameengine.pathing;

import com.btxtech.game.jsre.client.common.DecimalPosition;
import com.btxtech.game.jsre.client.common.Index;
import com.btxtech.game.jsre.client.common.Line2I;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Beat
 * 17.05.2016.
 */
public class PathingTest {

    @Test
    public void testCase() throws Exception {
        Pathing pathing = new Pathing();
        pathing.createUnit(1, true, 10.0, new DecimalPosition(33.079091144070816, -0.768289290001168), new DecimalPosition(40.0, 0.0), new DecimalPosition(100.0, 0.0), new DecimalPosition(100.0, 0.0));
        pathing.createObstacle(new Line2I(new Index(40, 5), new Index(60, 5)), new DecimalPosition(0.0, -1.0));
        pathing.createObstacle(new Line2I(new Index(60, 5), new Index(60, 105)), new DecimalPosition(1.0, 0.0));
        pathing.createObstacle(new Line2I(new Index(60, 105), new Index(40, 105)), new DecimalPosition(0.0, 1.0));
        pathing.createObstacle(new Line2I(new Index(40, 105), new Index(40, 5)), new DecimalPosition(-1.0, 0.0));
        pathing.tick(Pathing.FACTOR);
    }

    @Test
    public void testCase_DELETE_ME() throws Exception {
        Pathing pathing = new Pathing();
        pathing.createUnit(1, true, 10.0, new DecimalPosition(30.0009, 0.0), new DecimalPosition(40.0, 0.0), new DecimalPosition(100.0, 0.0), new DecimalPosition(100.0, 0.0));
        pathing.createObstacle(new Line2I(new Index(40, 5), new Index(60, 5)), new DecimalPosition(0.0, -1.0));
        pathing.createObstacle(new Line2I(new Index(60, 5), new Index(60, 105)), new DecimalPosition(1.0, 0.0));
        pathing.createObstacle(new Line2I(new Index(60, 105), new Index(40, 105)), new DecimalPosition(0.0, 1.0));
        pathing.createObstacle(new Line2I(new Index(40, 105), new Index(40, 5)), new DecimalPosition(-1.0, 0.0));
        pathing.tick(Pathing.FACTOR);
    }
}