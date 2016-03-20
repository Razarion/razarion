package com.btxtech.client.math3d;

import com.btxtech.game.jsre.client.common.DecimalPosition;
import com.btxtech.shared.primitives.Polygon2d;
import com.btxtech.shared.primitives.Triangulator;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Beat
 * 16.07.2015.
 */
public class TriangulatorTest {

    @Test
    public void simple1() {
        List<DecimalPosition> positions = Arrays.asList(new DecimalPosition(113, 280), new DecimalPosition(279, 146), new DecimalPosition(440, 286), new DecimalPosition(573, 206), new DecimalPosition(697, 382), new DecimalPosition(550, 364), new DecimalPosition(481, 504), new DecimalPosition(349, 263), new DecimalPosition(193, 307), new DecimalPosition(204, 462));
        Triangulator triangulator = new Triangulator();
        triangulator.calculate(new Polygon2d(positions));
    }

    @Test
    public void simple2() {
        List<DecimalPosition> positions = Arrays.asList(new DecimalPosition(709, 181), new DecimalPosition(741, 482), new DecimalPosition(536, 552), new DecimalPosition(465, 520), new DecimalPosition(396, 474), new DecimalPosition(358, 422), new DecimalPosition(341, 366), new DecimalPosition(326, 321), new DecimalPosition(303, 262), new DecimalPosition(297, 230), new DecimalPosition(308, 219), new DecimalPosition(336, 222), new DecimalPosition(366, 234), new DecimalPosition(375, 235), new DecimalPosition(384, 218), new DecimalPosition(386, 199), new DecimalPosition(407, 159), new DecimalPosition(413, 159), new DecimalPosition(436, 172), new DecimalPosition(449, 189), new DecimalPosition(458, 202), new DecimalPosition(468, 212), new DecimalPosition(488, 213), new DecimalPosition(505, 174), new DecimalPosition(519, 153), new DecimalPosition(531, 147), new DecimalPosition(543, 162), new DecimalPosition(552, 169), new DecimalPosition(590, 178), new DecimalPosition(594, 170), new DecimalPosition(600, 166), new DecimalPosition(621, 171), new DecimalPosition(648, 185));
        Triangulator triangulator = new Triangulator();
        triangulator.calculate(new Polygon2d(positions));
    }

    @Test
    public void simple3() {
        // TODO
        // List<DecimalPosition> positions = Arrays.asList(new DecimalPosition(200, 290), new DecimalPosition(211, 290), new DecimalPosition(223, 290), new DecimalPosition(235, 290), new DecimalPosition(247, 290), new DecimalPosition(258, 290), new DecimalPosition(270, 290), new DecimalPosition(282, 290), new DecimalPosition(294, 290), new DecimalPosition(305, 290), new DecimalPosition(317, 290), new DecimalPosition(329, 290), new DecimalPosition(341, 290), new DecimalPosition(352, 290), new DecimalPosition(364, 290), new DecimalPosition(376, 290), new DecimalPosition(388, 290), new DecimalPosition(400, 290), new DecimalPosition(411, 290), new DecimalPosition(423, 290), new DecimalPosition(435, 290), new DecimalPosition(447, 290), new DecimalPosition(458, 290), new DecimalPosition(470, 290), new DecimalPosition(482, 290), new DecimalPosition(494, 290), new DecimalPosition(505, 290), new DecimalPosition(517, 290), new DecimalPosition(529, 290), new DecimalPosition(541, 290), new DecimalPosition(552, 290), new DecimalPosition(564, 290), new DecimalPosition(576, 290), new DecimalPosition(588, 290), new DecimalPosition(600, 290), new DecimalPosition(589, 295), new DecimalPosition(579, 301), new DecimalPosition(569, 307), new DecimalPosition(559, 313), new DecimalPosition(549, 319), new DecimalPosition(539, 325), new DecimalPosition(529, 331), new DecimalPosition(519, 337), new DecimalPosition(509, 343), new DecimalPosition(498, 348), new DecimalPosition(488, 354), new DecimalPosition(478, 360), new DecimalPosition(468, 366), new DecimalPosition(458, 372), new DecimalPosition(448, 378), new DecimalPosition(438, 384), new DecimalPosition(428, 390), new DecimalPosition(418, 396), new DecimalPosition(408, 402), new DecimalPosition(397, 407), new DecimalPosition(387, 413), new DecimalPosition(377, 419), new DecimalPosition(367, 425), new DecimalPosition(357, 431), new DecimalPosition(347, 437), new DecimalPosition(337, 443), new DecimalPosition(327, 449), new DecimalPosition(317, 455), new DecimalPosition(307, 461), new DecimalPosition(300, 450), new DecimalPosition(294, 440), new DecimalPosition(288, 430), new DecimalPosition(281, 420), new DecimalPosition(275, 410), new DecimalPosition(269, 400), new DecimalPosition(262, 390), new DecimalPosition(256, 380), new DecimalPosition(250, 370), new DecimalPosition(244, 360), new DecimalPosition(237, 350), new DecimalPosition(231, 340), new DecimalPosition(225, 330), new DecimalPosition(218, 320), new DecimalPosition(212, 310), new DecimalPosition(206, 300));
        // Triangulator triangulator = new Triangulator();
        // triangulator.calculate(positions);
    }
}
