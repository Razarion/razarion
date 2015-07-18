package com.btxtech.client.math3d;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 18.04.2015.
 */
public class TestColor {

    @Test
    public void constructor1() {
        Color color = new Color(0.1, 0.3, 0.5, 0.7);
        Assert.assertEquals(0.1, color.getR(), 0.0001);
        Assert.assertEquals(0.3, color.getG(), 0.0001);
        Assert.assertEquals(0.5, color.getB(), 0.0001);
        Assert.assertEquals(0.7, color.getA(), 0.0001);
    }

    @Test
    public void constructor2() {
        Color color = new Color(0.1, 0.3, 0.5);
        Assert.assertEquals(0.1, color.getR(), 0.0001);
        Assert.assertEquals(0.3, color.getG(), 0.0001);
        Assert.assertEquals(0.5, color.getB(), 0.0001);
        Assert.assertEquals(1.0, color.getA(), 0.0001);
    }

    @Test
    public void equals() {
        Color color1 = new Color(0.1, 0.3, 0.5, 0.7);
        Color color2 = color1;
        Assert.assertTrue(color1.equals(color2));
        Assert.assertTrue(new Color(0.1, 0.3, 0.5, 0.7).equals(new Color(0.1, 0.3, 0.5, 0.7)));
        Assert.assertFalse(new Color(0.2, 0.3, 0.5, 0.7).equals(new Color(0.1, 0.3, 0.5, 0.7)));
        Assert.assertFalse(new Color(0.1, 0.4, 0.5, 0.7).equals(new Color(0.1, 0.3, 0.5, 0.7)));
        Assert.assertFalse(new Color(0.2, 0.3, 0.9, 0.7).equals(new Color(0.1, 0.3, 0.5, 0.7)));
        Assert.assertFalse(new Color(0.2, 0.3, 0.5, 0.1).equals(new Color(0.1, 0.3, 0.5, 0.7)));
    }

    @Test
    public void hash() {
        Assert.assertEquals(2080374784, new Color(1, 1, 1, 1).hashCode());
        Assert.assertEquals(2080374784, new Color(1, 1, 1, 1).hashCode());
        Assert.assertEquals(0, new Color(0, 0, 0, 0).hashCode());
    }

    @Test
    public void appendToColor() {
        Color color = new Color(0.1, 0.3, 0.5, 0.7);
        List<Double> doubleList = new ArrayList<Double>();
        color.appendToColorRGBA(doubleList);
        Assert.assertEquals(4, doubleList.size());
        Assert.assertEquals(0.1, doubleList.get(0), 0.001);
        Assert.assertEquals(0.3, doubleList.get(1), 0.001);
        Assert.assertEquals(0.5, doubleList.get(2), 0.001);
        Assert.assertEquals(0.7, doubleList.get(3), 0.001);
    }

    @Test
    public void toHtmlString() {
        Assert.assertEquals("#000000", new Color(0, 0, 0, 1).toHtmlColor());
        Assert.assertEquals("#000000", new Color(0, 0, 0, 0).toHtmlColor());
        Assert.assertEquals("#FF0000", new Color(1, 0, 0, 0).toHtmlColor());
        Assert.assertEquals("#FF0000", new Color(1, 0, 0, 1).toHtmlColor());
        Assert.assertEquals("#00FF00", new Color(0, 1, 0, 0).toHtmlColor());
        Assert.assertEquals("#0000FF", new Color(0, 0, 1, 0).toHtmlColor());
        Assert.assertEquals("#00007F", new Color(0, 0, 0.5, 0).toHtmlColor());
        Assert.assertEquals("#FFFFFF", new Color(1, 1, 1, 0).toHtmlColor());
        Assert.assertEquals("#020507", new Color(0.01, 0.02, 0.03, 0).toHtmlColor());
    }

    @Test
    public void fromHtmlColor() {
        Assert.assertEquals(new Color(0, 0, 0, 1), Color.fromHtmlColor("#000000"));
        Assert.assertEquals(new Color(1, 0, 0, 1), Color.fromHtmlColor("#FF0000"));
        Assert.assertEquals(new Color(0, 1, 0, 1), Color.fromHtmlColor("#00FF00"));
        Assert.assertEquals(new Color(0, 0, 1, 1), Color.fromHtmlColor("#0000FF"));
        Assert.assertEquals(new Color(0, 0, 0.4980392156862745, 1), Color.fromHtmlColor("#00007F"));
        Assert.assertEquals(new Color(1, 1, 1, 1), Color.fromHtmlColor("#FFFFFF"));
        Assert.assertEquals(new Color(0.00784313725490196, 0.0196078431372549, 0.027450980392156862, 1), Color.fromHtmlColor("#020507"));
    }

}
