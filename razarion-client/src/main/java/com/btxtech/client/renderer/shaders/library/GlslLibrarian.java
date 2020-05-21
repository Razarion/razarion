package com.btxtech.client.renderer.shaders.library;

import com.google.gwt.resources.client.ClientBundleWithLookup;
import com.google.gwt.resources.client.TextResource;

import java.util.Arrays;

public class GlslLibrarian {
    private static final String INCLUDE = "//-$$$-INCLUDE ";
    private static final String CHUNK_PREFIX = "//-$$$-CHUNK ";
    private static final String BEGIN_CHUNK_POSTFIX = " BEGIN";
    private static final String END_CHUNK_POSTFIX = " END";
    // private Logger logger = Logger.getLogger(GlslLibrarian.class.getName());
    private ClientBundleWithLookup shaderLibrary;
    private String lineSeparator;

    public GlslLibrarian(ClientBundleWithLookup shaderLibrary, String lineSeparator) {
        this.shaderLibrary = shaderLibrary;
        this.lineSeparator = lineSeparator;
    }

    public String link(TextResource shaderCode) {
        String[] lines = shaderCode.getText().split(lineSeparator);
        StringBuilder result = new StringBuilder();
        Arrays.stream(lines).forEach(line -> {
            if (line.trim().startsWith(INCLUDE)) {
                String shadeName = findShaderName(line);
                String shadeChunk = findShaderChunk(line);
                result.append(findChunk(shadeName, shadeChunk));
            } else {
                result.append(line);
                result.append(lineSeparator);
            }
        });
        return result.toString();
    }

    private String findShaderName(String line) {
        return splitIntoControlNames(line)[0];
    }

    private String findShaderChunk(String line) {
        return splitIntoControlNames(line)[1];
    }

    private String[] splitIntoControlNames(String line) {
        String remaining = line.trim().substring(INCLUDE.length());
        return remaining.split("\\s+");
    }

    private String findChunk(String shadeName, String shadeChunk) {
        String shaderCode = ((TextResource) shaderLibrary.getResource(shadeName)).getText();
        String beginMarker = CHUNK_PREFIX + shadeChunk + BEGIN_CHUNK_POSTFIX;
        int beginIndex = shaderCode.indexOf(beginMarker);
        if(beginIndex < 0) {
            throw new IllegalArgumentException("BeginMarker '" + beginMarker + "' not found.");
        }
        int endIndex = shaderCode.indexOf(CHUNK_PREFIX + shadeChunk + END_CHUNK_POSTFIX);
        if(endIndex < 0) {
            throw new IllegalArgumentException("EndMarker '" + CHUNK_PREFIX + shadeChunk + END_CHUNK_POSTFIX + "' not found.");
        }
        return shaderCode.substring(beginIndex + beginMarker.length(), endIndex);
    }
}
