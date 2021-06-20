package com.btxtech.client.renderer.shaders;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class GlslLibrarian {
    private static final String INCLUDE_CHUNK = "//-$$$-INCLUDE-CHUNK ";
    private static final String INCLUDE_EXTENSIONS = "//-$$$-INCLUDE-EXTENSIONS";
    private static final String INCLUDE_DEFINES = "//-$$$-INCLUDE-DEFINES";
    private static final String CHUNK_PREFIX = "//-$$$-CHUNK ";
    private static final String BEGIN_CHUNK_POSTFIX = " BEGIN";
    private static final String END_CHUNK_POSTFIX = " END";
    // private Logger logger = Logger.getLogger(GlslLibrarian.class.getName());
    private String customLib;
    private String lineSeparator;

    public GlslLibrarian(String customLib, String lineSeparator) {
        this.customLib = customLib;
        this.lineSeparator = lineSeparator;
    }

    public String link(String skeletonCode, List<String> glslDefines, boolean oESStandardDerivatives) {
        String[] lines = skeletonCode.split(lineSeparator);
        StringBuilder result = new StringBuilder();
        Arrays.stream(lines).forEach(line -> {
            if (line.trim().startsWith(INCLUDE_CHUNK)) {
                if (customLib != null) {
                    String chunkName = readParameter(0, line);
                    result.append(generateChunk(chunkName));
                    result.append(lineSeparator);
                }
            } else if (line.trim().startsWith(INCLUDE_EXTENSIONS)) {
                if (oESStandardDerivatives) {
                    result.append("#extension GL_OES_standard_derivatives : enable");
                    result.append(lineSeparator);
                }
            } else if (line.trim().startsWith(INCLUDE_DEFINES)) {
                if (glslDefines != null && !glslDefines.isEmpty()) {
                    result.append(generateDefines(glslDefines));
                    result.append(lineSeparator);
                }
            } else {
                result.append(line);
                result.append(lineSeparator);
            }
        });
        return result.toString();
    }

    private String readParameter(int pos, String line) {
        return splitIntoControlNames(line)[pos];
    }

    private String[] splitIntoControlNames(String line) {
        String remaining = line.trim().substring(INCLUDE_CHUNK.length());
        return remaining.split("\\s+");
    }

    private String generateChunk(String chunkName) {
        String beginMarker = CHUNK_PREFIX + chunkName + BEGIN_CHUNK_POSTFIX;
        int beginIndex = customLib.indexOf(beginMarker);
        if (beginIndex < 0) {
            return "";
        }
        int endIndex = customLib.indexOf(CHUNK_PREFIX + chunkName + END_CHUNK_POSTFIX);
        if (endIndex < 0) {
            throw new IllegalArgumentException("EndMarker '" + CHUNK_PREFIX + chunkName + END_CHUNK_POSTFIX + "' not found.");
        }
        return customLib.substring(beginIndex + beginMarker.length(), endIndex).trim();
    }

    private String generateDefines(List<String> glslDefines) {
        return glslDefines.stream().map(d -> "#define " + d + lineSeparator).collect(Collectors.joining()).trim();
    }
}
