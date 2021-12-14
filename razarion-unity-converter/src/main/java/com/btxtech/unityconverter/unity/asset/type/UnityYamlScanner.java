package com.btxtech.unityconverter.unity.asset.type;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UnityYamlScanner {
    private static final Logger LOGGER = Logger.getLogger(UnityYamlScanner.class.getName());
    private static final String SEPARATOR_DELIMITER = "---";

    static public List<YamlDocument> readAllYamlDocuments(File unityYamlFile) {
        List<YamlDocument> yamlDocuments = new ArrayList<>();
        yamlDocuments.add(new YamlDocument("+++ROOT+++", "+++ROOT+++", null));
        try (Stream<String> stream = Files.lines(Paths.get(unityYamlFile.toURI()))) {
            stream.forEach(line -> {
                if (line.startsWith(SEPARATOR_DELIMITER)) {
                    List<String> values = parseSeparator(line);
                    if (values.size() == 2) {
                        yamlDocuments.add(new YamlDocument(values.get(0), values.get(1), null));
                    } else if (values.size() == 3) {
                        yamlDocuments.add(new YamlDocument(values.get(0), values.get(1), values.get(2)));
                    } else {
                        LOGGER.warning("Unknown yaml separator value count: '" + line + "'");
                    }
                } else {
                    yamlDocuments.get(yamlDocuments.size() - 1).appendLine(line);
                }
            });
            return yamlDocuments;
        } catch (IOException e) {
            throw new RuntimeException("Error UnityYamlScanner.readAllYamlDocuments: " + yamlDocuments, e);
        }
    }

    private static List<String> parseSeparator(String separatorLine) {
        String valueLine = separatorLine.trim().substring(SEPARATOR_DELIMITER.length()).trim();
        return Arrays.stream(valueLine.split("\\s+")).collect(Collectors.toList());
    }

    public static class YamlDocument {
        private final String tag;
        private final String objectId;
        private final String mayBeStripped;
        private String content = "";

        public YamlDocument(String tag, String objectId, String mayBeStripped) {
            this.tag = tag;
            this.objectId = objectId.substring(1);
            this.mayBeStripped = mayBeStripped;
        }

        public String getTag() {
            return tag;
        }

        public String getObjectId() {
            return objectId;
        }

        public String getMayBeStripped() {
            return mayBeStripped;
        }

        public void appendLine(String line) {
            content = content + line + "\n";
        }

        @Override
        public String toString() {
            return "YamlDocument{" +
                    "tag='" + tag + '\'' +
                    ", objectId='" + objectId + '\'' +
                    ", mayBeStripped='" + mayBeStripped + '\'' +
                    ", content='" + content + '\'' +
                    '}';
        }
    }
}
