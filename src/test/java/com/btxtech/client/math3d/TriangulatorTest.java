package com.btxtech.client.math3d;

import com.btxtech.game.jsre.client.common.Index;
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
        List<Index> positions = Arrays.asList(new Index(113, 280), new Index(279, 146), new Index(440, 286), new Index(573, 206), new Index(697, 382), new Index(550, 364), new Index(481, 504), new Index(349, 263), new Index(193, 307), new Index(204, 462));
        Triangulator triangulator = new Triangulator();
        triangulator.calculate(positions);
    }

    @Test
    public void simple2() {
        List<Index> positions = Arrays.asList(new Index(709, 181), new Index(741, 482), new Index(536, 552), new Index(465, 520), new Index(396, 474), new Index(358, 422), new Index(341, 366), new Index(326, 321), new Index(303, 262), new Index(297, 230), new Index(308, 219), new Index(336, 222), new Index(366, 234), new Index(375, 235), new Index(384, 218), new Index(386, 199), new Index(407, 159), new Index(413, 159), new Index(436, 172), new Index(449, 189), new Index(458, 202), new Index(468, 212), new Index(488, 213), new Index(505, 174), new Index(519, 153), new Index(531, 147), new Index(543, 162), new Index(552, 169), new Index(590, 178), new Index(594, 170), new Index(600, 166), new Index(621, 171), new Index(648, 185));
        Triangulator triangulator = new Triangulator();
        triangulator.calculate(positions);
    }

    @Test
    public void simple3() {
        List<Index> positions = Arrays.asList(new Index(200, 290), new Index(211, 290), new Index(223, 290), new Index(235, 290), new Index(247, 290), new Index(258, 290), new Index(270, 290), new Index(282, 290), new Index(294, 290), new Index(305, 290), new Index(317, 290), new Index(329, 290), new Index(341, 290), new Index(352, 290), new Index(364, 290), new Index(376, 290), new Index(388, 290), new Index(400, 290), new Index(411, 290), new Index(423, 290), new Index(435, 290), new Index(447, 290), new Index(458, 290), new Index(470, 290), new Index(482, 290), new Index(494, 290), new Index(505, 290), new Index(517, 290), new Index(529, 290), new Index(541, 290), new Index(552, 290), new Index(564, 290), new Index(576, 290), new Index(588, 290), new Index(600, 290), new Index(589, 295), new Index(579, 301), new Index(569, 307), new Index(559, 313), new Index(549, 319), new Index(539, 325), new Index(529, 331), new Index(519, 337), new Index(509, 343), new Index(498, 348), new Index(488, 354), new Index(478, 360), new Index(468, 366), new Index(458, 372), new Index(448, 378), new Index(438, 384), new Index(428, 390), new Index(418, 396), new Index(408, 402), new Index(397, 407), new Index(387, 413), new Index(377, 419), new Index(367, 425), new Index(357, 431), new Index(347, 437), new Index(337, 443), new Index(327, 449), new Index(317, 455), new Index(307, 461), new Index(300, 450), new Index(294, 440), new Index(288, 430), new Index(281, 420), new Index(275, 410), new Index(269, 400), new Index(262, 390), new Index(256, 380), new Index(250, 370), new Index(244, 360), new Index(237, 350), new Index(231, 340), new Index(225, 330), new Index(218, 320), new Index(212, 310), new Index(206, 300));
        Triangulator triangulator = new Triangulator();
        triangulator.calculate(positions);
    }
}
