package com.btxtech.shared.primitives;

import com.btxtech.game.jsre.client.common.Index;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

/**
 * Created by Beat
 * 09.05.2016.
 */
public class Polygon2ITest {

    @Test(expected = IllegalArgumentException.class)
    public void combineDoNotAdjoin() {
        Polygon2I poly1 = new Polygon2I(Arrays.asList(new Index(50, 0), new Index(25, 44), new Index(-25, 44), new Index(-50, 0), new Index(-25, -43), new Index(25, -43)));
        Polygon2I poly2 = new Polygon2I(Arrays.asList(new Index(580, 500), new Index(1000, 500), new Index(1000, 1120)));
        poly1.combine(poly2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void combineCompletelyInside() {
        Polygon2I poly1 = new Polygon2I(Arrays.asList(new Index(883, 637), new Index(858, 681), new Index(808, 681), new Index(783, 637), new Index(808, 594), new Index(858, 594)));
        Polygon2I poly2 = new Polygon2I(Arrays.asList(new Index(580, 500), new Index(1000, 500), new Index(1000, 1120)));
        poly1.combine(poly2);
    }

    @Test
    public void combineOneCorner() {
        Polygon2I poly1 = new Polygon2I(Arrays.asList(new Index(1081, 598), new Index(1056, 642), new Index(1006, 642), new Index(981, 598), new Index(1006, 555), new Index(1056, 555)));
        Polygon2I poly2 = new Polygon2I(Arrays.asList(new Index(580, 500), new Index(1000, 500), new Index(1000, 1120)));
        Polygon2I polyResult = poly1.combine(poly2);
        Assert.assertEquals(new Polygon2I(Arrays.asList(new Index(1006, 555), new Index(1056, 555), new Index(1081, 598), new Index(1056, 642), new Index(1006, 642), new Index(1000, 631), new Index(1000, 1120), new Index(580, 500), new Index(1000, 500), new Index(1000, 565))), polyResult);
    }

    @Test
    public void combineTwoCorner() {
        Polygon2I poly1 = new Polygon2I(Arrays.asList(new Index(700, 652), new Index(675, 696), new Index(625, 696), new Index(600, 652), new Index(625, 609), new Index(675, 609)));
        Polygon2I poly2 = new Polygon2I(Arrays.asList(new Index(580, 500), new Index(1000, 500), new Index(1000, 1120)));
        Polygon2I polyResult = poly1.combine(poly2);
        Assert.assertEquals(new Polygon2I(Arrays.asList(new Index(675, 696), new Index(625, 696), new Index(600, 652), new Index(625, 609), new Index(654, 609), new Index(580, 500), new Index(1000, 500), new Index(1000, 1120), new Index(692, 666))), polyResult);
    }

    @Test
    public void combineTwoCornerOtherSide() {
        Polygon2I poly1 = new Polygon2I(Arrays.asList(new Index(684, 622), new Index(659, 666), new Index(609, 666), new Index(584, 622), new Index(609, 579), new Index(659, 579)));
        Polygon2I poly2 = new Polygon2I(Arrays.asList(new Index(580, 500), new Index(1000, 500), new Index(1000, 1120)));
        Polygon2I polyResult = poly1.combine(poly2);
        Assert.assertEquals(new Polygon2I(Arrays.asList(new Index(659, 666), new Index(609, 666), new Index(584, 622), new Index(609, 579), new Index(634, 579), new Index(580, 500), new Index(1000, 500), new Index(1000, 1120), new Index(674, 639))), polyResult);
    }

    @Test
    public void combine2Corners1Corner() {
        Polygon2I poly1 = new Polygon2I(Arrays.asList(new Index(1017, 484), new Index(992, 528), new Index(942, 528), new Index(917, 484), new Index(942, 441), new Index(992, 441)));
        Polygon2I poly2 = new Polygon2I(Arrays.asList(new Index(580, 500), new Index(1000, 500), new Index(1000, 1120)));
        Polygon2I polyResult = poly1.combine(poly2);
        Assert.assertEquals(new Polygon2I(Arrays.asList(new Index(917, 484), new Index(942, 441), new Index(992, 441), new Index(1017, 484), new Index(1000, 514), new Index(1000, 1120), new Index(580, 500), new Index(926, 500))), polyResult);
    }

    @Test
    public void combineOnlyOneCornerOutside() {
        Polygon2I poly1 = new Polygon2I(Arrays.asList(new Index(1020, 846), new Index(995, 890), new Index(945, 890), new Index(920, 846), new Index(945, 803), new Index(995, 803)));
        Polygon2I poly2 = new Polygon2I(Arrays.asList(new Index(580, 500), new Index(1000, 500), new Index(1000, 1120)));
        Polygon2I polyResult = poly1.combine(poly2);
        Assert.assertEquals(new Polygon2I(Arrays.asList(new Index(1020, 846), new Index(1000, 881), new Index(1000, 1120), new Index(580, 500), new Index(1000, 500), new Index(1000, 812))), polyResult);
    }

    @Test
    public void combineOtherPart() {
        Polygon2I poly1 = new Polygon2I(Arrays.asList(new Index(1080, 487), new Index(1055, 531), new Index(1005, 531), new Index(980, 487), new Index(1005, 444), new Index(1055, 444)));
        Polygon2I poly2 = new Polygon2I(Arrays.asList(new Index(580, 500), new Index(1000, 500), new Index(1000, 1120)));
        Polygon2I polyResult = poly1.combine(poly2);
        Assert.assertEquals(new Polygon2I(Arrays.asList(new Index(1000, 1120), new Index(580, 500), new Index(987, 500), new Index(980, 487), new Index(1005, 444), new Index(1055, 444), new Index(1080, 487), new Index(1055, 531), new Index(1005, 531), new Index(1000, 522))), polyResult);
    }

// TODO
//    @Test
//    public void combinePierce() {
//        Polygon2I poly1 = new Polygon2I(Arrays.asList(new Index(1031, 1040), new Index(1006, 1084), new Index(956, 1084), new Index(931, 1040), new Index(956, 997), new Index(1006, 997)));
//        Polygon2I poly2 = new Polygon2I(Arrays.asList(new Index(580, 500), new Index(1000, 500), new Index(1000, 1120)));
//        Polygon2I polyResult = poly1.combine(poly2);
//    }

// TODO
//    @Test
//    public void combine() {
//        Polygon2I poly1 = new Polygon2I(Arrays.asList(new Index(1021, 1040), new Index(996, 1084), new Index(946, 1084), new Index(921, 1040), new Index(946, 997), new Index(996, 997)));
//        Polygon2I poly2 = new Polygon2I(Arrays.asList(new Index(580, 500), new Index(1000, 500), new Index(1000, 1120)));
//        Polygon2I polyResult = poly1.combine(poly2);
//        Assert.fail();
//        // Assert.assertEquals( new Polygon2I(Arrays.asList(new Index(1021, 1040), new Index(996, 1084), new Index(946, 1084), new Index(1000, 1077), new Index(1000, 1120), new Index(580, 500), new Index(1000, 500), new Index(1000, 1004))), polyResult);
//    }

}