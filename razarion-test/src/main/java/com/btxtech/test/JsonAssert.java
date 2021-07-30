package com.btxtech.test;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ValueNode;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Iterator;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class JsonAssert {
    // public static String TEST_RESOURCE_FOLDER = "C:\\dev\\projects\\razarion\\code\\razarion\\razarion-server\\src\\test\\resources";
    // public static String TEST_RESOURCE_FOLDER = "C:\\dev\\projects\\razarion\\code\\razarion\\razarion-server\\src\\test\\resources\\com\\btxtech\\server\\collada";
    // public static String TEST_RESOURCE_FOLDER = "C:\\dev\\projects\\razarion\\code\\razarion\\razarion-server\\src\\test\\resources\\com\\btxtech\\server\\systemtests\\editors";
    // public static String TEST_RESOURCE_FOLDER = "C:\\dev\\projects\\razarion\\code\\razarion\\razarion-server\\src\\test\\resources\\com\\btxtech\\server\\systemtests\\testnormal";
    public static String TEST_RESOURCE_FOLDER = "C:\\dev\\projects\\razarion\\code\\razarion\\razarion-share\\src\\test\\resources\\com\\btxtech\\shared\\gameengine\\planet\\terrain";

    private JsonAssert() {

    }

    public static void assertViaJson(Object expected, Object actual, IdSuppressor[] idSuppressors) {
        try {
            // https://www.baeldung.com/jackson-compare-two-json-objects
            ObjectMapper mapper = new ObjectMapper();
            JsonNode expectedNode = mapper.readTree(mapper.writeValueAsString(expected));
            JsonNode actualNode = mapper.readTree(mapper.writeValueAsString(actual));

            assertViaJson(expectedNode, actualNode, idSuppressors);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void assertViaJson(String expectedResource, Function<String, String> replacer, IdSuppressor[] idSuppressors, Class resourceLoader, Object actual) {
        assertViaJson(expectedResource, replacer, idSuppressors, resourceLoader, actual, false);
    }

    public static void assertViaJson(String expectedResource, Function<String, String> replacer, IdSuppressor[] idSuppressors, Class resourceLoader, Object actual, boolean createExpectedFile) {
        assertViaJson(expectedResource, replacer, idSuppressors, resourceLoader, actual, null, createExpectedFile);
    }

    public static void assertViaJson(String expectedResource, Function<String, String> replacer, IdSuppressor[] idSuppressors, Class resourceLoader, Object actual, ObjectMapper mapper, boolean createExpectedFile) {
        try {
            InputStream inputStream = resourceLoader.getResourceAsStream(expectedResource);
            if (inputStream == null) {
                if (createExpectedFile) {
                    new File(TEST_RESOURCE_FOLDER, expectedResource).createNewFile();
                }
                throw new IOException("No such resource: " + expectedResource);
            }
            String jsonString = new BufferedReader(new InputStreamReader(inputStream)).lines().collect(Collectors.joining("\n"));
            if (replacer != null) {
                jsonString = replacer.apply(jsonString);
            }
            // https://www.baeldung.com/jackson-compare-two-json-objects
            if (mapper == null) {
                mapper = new ObjectMapper();
            }
            if (createExpectedFile) {
                mapper.writerWithDefaultPrettyPrinter().writeValue(new File(TEST_RESOURCE_FOLDER, expectedResource), actual);
            }
//            System.out.println("-----------------------------------");
//            System.out.println(mapper.writeValueAsString(actual));
//            System.out.println("-----------------------------------");
            assertViaJson(mapper.readTree(jsonString), mapper.readTree(mapper.writeValueAsString(actual)), idSuppressors);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void assertViaJson(JsonNode expectedNode, JsonNode actualNode, IdSuppressor[] idSuppressors) {
        suppress(idSuppressors, expectedNode);
        suppress(idSuppressors, actualNode);
        boolean equals = expectedNode.equals(actualNode);
        if (!equals) {
            displayDifferences(expectedNode, actualNode, JsonPointer.compile(null));
            displayDifferences(actualNode, expectedNode, JsonPointer.compile(null));
            throw new AssertionError("\nexpected: " + expectedNode + "\nactual  : " + actualNode);
        }
    }

    public static void displayDifferences(JsonNode expectedNode, JsonNode actualNode, JsonPointer jsonPointer) {
        if (expectedNode instanceof ObjectNode) {
            ObjectNode objectNode = (ObjectNode) expectedNode;
            for (Iterator<String> stringIterator = objectNode.fieldNames(); stringIterator.hasNext(); ) {
                String fieldName = stringIterator.next();
                JsonNode child = expectedNode.get(fieldName);
                displayDifferences(child, actualNode, jsonPointer.append(JsonPointer.compile("/" + fieldName)));
            }
        } else if (expectedNode instanceof ArrayNode) {
            ArrayNode arrayNode = (ArrayNode) expectedNode;
            for (int i = 0; i < arrayNode.size(); i++) {
                JsonNode child = arrayNode.get(i);
                displayDifferences(child, actualNode, jsonPointer.append(JsonPointer.compile("/" + i)));
            }
        } else if (expectedNode instanceof ValueNode) {
            ValueNode valueNodeExpected = (ValueNode) expectedNode;
            JsonNode actualNodeExpected = actualNode.at(jsonPointer);
            if (!valueNodeExpected.equals(actualNodeExpected)) {
                System.out.println(jsonPointer + " expected: " + valueNodeExpected + " actual: " + actualNodeExpected);
            }
        } else {
            throw new IllegalArgumentException(expectedNode.toString());
        }
    }

    private static void suppress(IdSuppressor[] idSuppressors, JsonNode jsonNode) {
        if (idSuppressors != null) {
            Arrays.stream(idSuppressors).forEach(idSuppressor -> {
                if (idSuppressor.array) {
                    int index = 0;
                    while (true) {
                        if (!suppress(idSuppressor.jsonPtrExpr + "/" + index, idSuppressor.propertyName, idSuppressor.children, jsonNode)) {
                            return;
                        }
                        index++;
                        if (index > 1000000) {
                            throw new IllegalStateException();
                        }
                    }
                } else {
                    suppress(idSuppressor.jsonPtrExpr, idSuppressor.propertyName, idSuppressor.children, jsonNode);
                }
            });
        }
    }

    private static boolean suppress(String jsonPtrExpr, String propertyName, IdSuppressor[] childrenIdSuppressors, JsonNode rootNode) {
        JsonNode objectNode = rootNode.at(jsonPtrExpr);
        if (objectNode.isObject()) {
            ((ObjectNode) objectNode).put(propertyName, "SUPPRESSED");
            suppress(childrenIdSuppressors, objectNode);
            return true;
        }
        return false;
    }

    public static class IdSuppressor {
        private String jsonPtrExpr;
        private String propertyName;
        private boolean array;
        private IdSuppressor[] children;

        public IdSuppressor(String jsonPtrExpr, String propertyName, IdSuppressor... children) {
            this(jsonPtrExpr, propertyName, false, children);
        }

        public IdSuppressor(String jsonPtrExpr, String propertyName, boolean array, IdSuppressor... children) {
            this.jsonPtrExpr = jsonPtrExpr;
            this.propertyName = propertyName;
            this.array = array;
            this.children = children;
        }
    }
}
