package com.btxtech.client.renderer.shaders.library;

import com.google.gwt.resources.client.ClientBundleWithLookup;
import com.google.gwt.resources.client.ResourcePrototype;
import com.google.gwt.resources.client.TextResource;
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
    public void linkChunk() {
        GlslLibrarian glslLibrarian = new GlslLibrarian(setupShaderLibrary(), System.lineSeparator());
        String actual = glslLibrarian.link(createTextResource("ShaderChunk.frag", GlslLibrarianTest.class), null);
        Assert.assertEquals(resource2Text("ResultChunk.frag", GlslLibrarianTest.class), actual);
    }

    @Test
    public void linkDefines() {
        GlslLibrarian glslLibrarian = new GlslLibrarian(setupShaderLibrary(), System.lineSeparator());
        String actual = glslLibrarian.link(createTextResource("ShaderDefines.frag", GlslLibrarianTest.class), Arrays.asList("RENDER_BOTTOM"));
        Assert.assertEquals(resource2Text("ResultDefines.frag", GlslLibrarianTest.class), actual);
    }

    @Test
    public void linkNullDefines() {
        GlslLibrarian glslLibrarian = new GlslLibrarian(setupShaderLibrary(), System.lineSeparator());
        String actual = glslLibrarian.link(createTextResource("ShaderNullDefines.frag", GlslLibrarianTest.class), null);
        Assert.assertEquals(resource2Text("ResultNullDefines.frag", GlslLibrarianTest.class), actual);
    }

    private ClientBundleWithLookup setupShaderLibrary() {
        return new ClientBundleWithLookup() {
            @Override
            public ResourcePrototype getResource(String name) {
                if (name.equals("phong")) {
                    return new TextResource() {
                        @Override
                        public String getText() {
                            return resource2Text("Library.frag", GlslLibrarianTest.class);
                        }

                        @Override
                        public String getName() {
                            throw new UnsupportedOperationException();
                        }
                    };
                } else {
                    throw new IllegalArgumentException("Unknown: " + name);
                }
            }

            @Override
            public ResourcePrototype[] getResources() {
                throw new UnsupportedOperationException();
            }
        };
    }

    private TextResource createTextResource(String location, Class clazz) {
        return new TextResource() {
            @Override
            public String getText() {
                return resource2Text(location, clazz);
            }

            @Override
            public String getName() {
                throw new UnsupportedOperationException();
            }
        };
    }

    private static String resource2Text(String location, Class clazz) {
        InputStream inputStream = clazz.getResourceAsStream(location);
        if (inputStream == null) {
            throw new IllegalArgumentException("Location can not be found: " + location);
        }
        return new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)).lines().collect(Collectors.joining(System.lineSeparator()));
    }

}