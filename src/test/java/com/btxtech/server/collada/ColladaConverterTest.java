package com.btxtech.server.collada;

import org.junit.Test;

/**
 * Created by Beat
 * 14.08.2015.
 */
public class ColladaConverterTest {

    @Test
    public void testRead() throws Exception {
        ColladaConverter colladaConverter = new ColladaConverter();
        colladaConverter.read();
    }
}