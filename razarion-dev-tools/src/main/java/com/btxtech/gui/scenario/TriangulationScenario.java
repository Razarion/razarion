package com.btxtech.gui.scenario;

/**
 * Created by Beat
 * 17.07.2015.
 */
public class TriangulationScenario extends Scenario {
    @Override
    public void setup() {
        // List<DecimalPosition> positions = Arrays.asList(new DecimalPosition(113, 280), new DecimalPosition(279, 146), new DecimalPosition(440, 286), new DecimalPosition(573, 206), new DecimalPosition(697, 382), new DecimalPosition(550, 364), new DecimalPosition(481, 504), new DecimalPosition(349, 263), new DecimalPosition(193, 307), new DecimalPosition(204, 462));
        // List<DecimalPosition> positions = Arrays.asList(new DecimalPosition(200, 290), new DecimalPosition(211, 290), new DecimalPosition(223, 290), new DecimalPosition(235, 290), new DecimalPosition(247, 290), new DecimalPosition(258, 290), new DecimalPosition(270, 290), new DecimalPosition(282, 290), new DecimalPosition(294, 290), new DecimalPosition(305, 290), new DecimalPosition(317, 290), new DecimalPosition(329, 290), new DecimalPosition(341, 290), new DecimalPosition(352, 290), new DecimalPosition(364, 290), new DecimalPosition(376, 290), new DecimalPosition(388, 290), new DecimalPosition(400, 290), new DecimalPosition(411, 290), new DecimalPosition(423, 290), new DecimalPosition(435, 290), new DecimalPosition(447, 290), new DecimalPosition(458, 290), new DecimalPosition(470, 290), new DecimalPosition(482, 290), new DecimalPosition(494, 290), new DecimalPosition(505, 290), new DecimalPosition(517, 290), new DecimalPosition(529, 290), new DecimalPosition(541, 290), new DecimalPosition(552, 290), new DecimalPosition(564, 290), new DecimalPosition(576, 290), new DecimalPosition(588, 290), new DecimalPosition(600, 290), new DecimalPosition(589, 295), new DecimalPosition(579, 301), new DecimalPosition(569, 307), new DecimalPosition(559, 313), new DecimalPosition(549, 319), new DecimalPosition(539, 325), new DecimalPosition(529, 331), new DecimalPosition(519, 337), new DecimalPosition(509, 343), new DecimalPosition(498, 348), new DecimalPosition(488, 354), new DecimalPosition(478, 360), new DecimalPosition(468, 366), new DecimalPosition(458, 372), new DecimalPosition(448, 378), new DecimalPosition(438, 384), new DecimalPosition(428, 390), new DecimalPosition(418, 396), new DecimalPosition(408, 402), new DecimalPosition(397, 407), new DecimalPosition(387, 413), new DecimalPosition(377, 419), new DecimalPosition(367, 425), new DecimalPosition(357, 431), new DecimalPosition(347, 437), new DecimalPosition(337, 443), new DecimalPosition(327, 449), new DecimalPosition(317, 455), new DecimalPosition(307, 461), new DecimalPosition(300, 450), new DecimalPosition(294, 440), new DecimalPosition(288, 430), new DecimalPosition(281, 420), new DecimalPosition(275, 410), new DecimalPosition(269, 400), new DecimalPosition(262, 390), new DecimalPosition(256, 380), new DecimalPosition(250, 370), new DecimalPosition(244, 360), new DecimalPosition(237, 350), new DecimalPosition(231, 340), new DecimalPosition(225, 330), new DecimalPosition(218, 320), new DecimalPosition(212, 310), new DecimalPosition(206, 300));

//        List<DecimalPosition> positions =   Arrays.asList(new DecimalPosition(199.99999999999986, 199.99999999999997), new DecimalPosition(800.0, 200.0), new DecimalPosition(800.0, 600.0), new DecimalPosition(199.99999999999986, 199.99999999999997), new DecimalPosition(384.0, 256.0), new DecimalPosition(384.0, 320.0), new DecimalPosition(448.0, 320.0), new DecimalPosition(512.0, 320.0), new DecimalPosition(512.0, 384.0), new DecimalPosition(576.0, 384.0), new DecimalPosition(576.0, 448.0), new DecimalPosition(640.0, 448.0), new DecimalPosition(704.0, 448.0), new DecimalPosition(704.0, 512.0), new DecimalPosition(768.0, 512.0), new DecimalPosition(768.0, 448.0), new DecimalPosition(768.0, 384.0), new DecimalPosition(768.0, 320.0), new DecimalPosition(768.0, 256.0), new DecimalPosition(704.0, 256.0), new DecimalPosition(640.0, 256.0), new DecimalPosition(576.0, 256.0), new DecimalPosition(512.0, 256.0), new DecimalPosition(448.0, 256.0), new DecimalPosition(384.0, 256.0));
//
//        addPolygon(positions);
//        Triangulator triangulator = new Triangulator();
//        try {
//            List<Triangle2d> triangle2ds = triangulator.calculate(new Polygon2d(positions));
//            for (Triangle2d triangle2d : triangle2ds) {
//                addTriangle(triangle2d, Color.TRANSPARENT, createRandomColor(0.3));
//            }
//        } catch (Exception e) {
//            for (Triangle2d triangle2d : triangulator.getTriangles()) {
//                addTriangle(triangle2d, Color.TRANSPARENT, createRandomColor(0.3));
//            }
//
//            // TerrainPolygon<TerrainPolygonCorner, TerrainPolygonLine> polygon = triangulator.getLastKnownGoodPolygon();
//            // System.out.println(DecimalPosition.testString(polygon.toPoints()));
//            e.printStackTrace();
//        }
    }
}
