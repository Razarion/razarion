package com.btxtech.client.renderer.shaders.library;

import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Collectors;

@SuppressWarnings("ALL")
public class GlslLibrarianTest {

    @Test
    public void linkSkeletonNoCustom() {
        GlslLibrarian glslLibrarian = new GlslLibrarian(null, System.lineSeparator());
        String actual = glslLibrarian.link(resource2Text("TestSkeleton.vert", GlslLibrarianTest.class), null, false);
        Assert.assertEquals(resource2Text("ResultNoCustom.vert", GlslLibrarianTest.class).trim(), actual.trim());
    }

    @Test
    public void linkSkeletonCustom() {
        GlslLibrarian glslLibrarian = new GlslLibrarian(resource2Text("Custom.glsl", GlslLibrarianTest.class), System.lineSeparator());
        String actual = glslLibrarian.link(resource2Text("TestSkeleton.vert", GlslLibrarianTest.class), null, false);
        Assert.assertEquals(resource2Text("ResultCustom.vert", GlslLibrarianTest.class).trim(), actual.trim());
    }

    @Test
    public void linkSkeletonCustomDefines() {
        GlslLibrarian glslLibrarian = new GlslLibrarian(resource2Text("Custom.glsl", GlslLibrarianTest.class), System.lineSeparator());
        String actual = glslLibrarian.link(resource2Text("TestSkeleton.vert", GlslLibrarianTest.class), Arrays.asList("RENDER_BOTTOM_1", "RENDER_BOTTOM_2"), true);
        Assert.assertEquals(resource2Text("ResultCustomDefines.vert", GlslLibrarianTest.class).trim(), actual.trim());
    }

    private static String resource2Text(String location, Class clazz) {
        InputStream inputStream = clazz.getResourceAsStream(location);
        if (inputStream == null) {
            throw new IllegalArgumentException("Location can not be found: " + location);
        }
        return new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)).lines().collect(Collectors.joining(System.lineSeparator()));
    }

}