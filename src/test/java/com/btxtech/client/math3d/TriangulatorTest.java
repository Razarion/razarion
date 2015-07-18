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





}
