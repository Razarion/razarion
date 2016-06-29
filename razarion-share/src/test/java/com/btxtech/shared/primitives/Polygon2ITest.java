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

    @Test
    public void removeCompletelyInside() {
        Polygon2I outer = new Polygon2I(Arrays.asList(new Index(-9, 171), new Index(107, 155), new Index(166, 116), new Index(168, 17), new Index(165, -74), new Index(113, -141), new Index(3, -146), new Index(-103, -140), new Index(-172, -75), new Index(-176, 28), new Index(-147, 131)));
        Polygon2I inner = new Polygon2I(Arrays.asList(new Index(-25, 75), new Index(19, 71), new Index(70, 27), new Index(76, -43), new Index(13, -70), new Index(-47, -59), new Index(-80, 49)));
        Assert.assertNull(inner.remove(outer));
    }

    @Test(expected = IllegalArgumentException.class)
    public void removeOtherCompletelyInside() {
        Polygon2I outer = new Polygon2I(Arrays.asList(new Index(-9, 171), new Index(107, 155), new Index(166, 116), new Index(168, 17), new Index(165, -74), new Index(113, -141), new Index(3, -146), new Index(-103, -140), new Index(-172, -75), new Index(-176, 28), new Index(-147, 131)));
        Polygon2I inner = new Polygon2I(Arrays.asList(new Index(-25, 75), new Index(19, 71), new Index(70, 27), new Index(76, -43), new Index(13, -70), new Index(-47, -59), new Index(-80, 49)));
        outer.remove(inner);
    }

    @Test(expected = IllegalArgumentException.class)
    public void removeDoNotOverlap() {
        Polygon2I polygon1 = new Polygon2I(Arrays.asList(new Index(20, 80), new Index(-67, 80), new Index(-80, -7), new Index(-69, -69), new Index(31, -83), new Index(53, 37)));
        Polygon2I polygon2 = new Polygon2I(Arrays.asList(new Index(275, 74), new Index(181, 66), new Index(139, -60), new Index(282, -94), new Index(332, -5)));
        polygon1.remove(polygon2);
    }

    @Test
    public void removeThisNoCorner() {
        Polygon2I polygon1 = new Polygon2I(Arrays.asList(new Index(-79, 7), new Index(-25, -74), new Index(50, -72), new Index(82, -42), new Index(46, 83), new Index(-73, 83)));
        Polygon2I polygon2 = new Polygon2I(Arrays.asList(new Index(116, 127), new Index(31, 40), new Index(103, -11), new Index(202, -14), new Index(231, 53), new Index(200, 129)));

        Polygon2I expected = new Polygon2I(Arrays.asList(new Index(46, 83), new Index(-73, 83), new Index(-79, 7), new Index(-25, -74), new Index(50, -72), new Index(82, -42), new Index(65, 16), new Index(31, 40), new Index(52, 62)));
        Assert.assertEquals(expected, polygon1.remove(polygon2));

        expected = new Polygon2I(Arrays.asList(new Index(103, -11), new Index(202, -14), new Index(231, 53), new Index(200, 129), new Index(116, 127), new Index(52, 62), new Index(65, 16)));

        Assert.assertEquals(expected, polygon2.remove(polygon1));
    }
}