package com.btxtech.server.systemtests.framework;

import com.btxtech.server.ServerTestHelper;
import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ValueNode;
import org.junit.Assert;

import javax.ws.rs.NotAuthorizedException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by Beat
 * 05.05.2017.
 */
public abstract class AbstractSystemTest extends ServerTestHelper {
    private RestConnection defaultRestConnection = new RestConnection(null);

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

    public <T> void runUnauthorizedTest(Class<T> testClass, Consumer<T> underTestConsumer, RestConnection.TestUser ... unauthorizedUsers) {
        T underTest = defaultRestConnection.proxy(testClass);
        Arrays.stream(unauthorizedUsers).forEach(unauthorizedUser -> {
            defaultRestConnection.login(unauthorizedUser);
            try {
                underTestConsumer.accept(underTest);
                Assert.fail("NotAuthorizedException expected for " + unauthorizedUser);
            } catch (NotAuthorizedException e) {
                // Ignore
            }
        });
    }

    protected <T> T setupRestAccess(Class<T> clazz) {
        return defaultRestConnection.proxy(clazz);
    }

    public RestConnection getDefaultRestConnection() {
        return defaultRestConnection;
    }

    protected void assertViaJson(Object expected, Object actual, IdSuppressor[] idSuppressors) {
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

    protected void assertViaJson(String expectedResource, Function<String, String> replacer, IdSuppressor[] idSuppressors, Class resourceLoader, Object actual) {
        try {
            InputStream inputStream = resourceLoader.getResourceAsStream(expectedResource);
            if (inputStream == null) {
                throw new IOException("No such resource: " + expectedResource);
            }
            String jsonString = new BufferedReader(new InputStreamReader(inputStream)).lines().collect(Collectors.joining("\n"));
            if (replacer != null) {
                jsonString = replacer.apply(jsonString);
            }
            // https://www.baeldung.com/jackson-compare-two-json-objects
            ObjectMapper mapper = new ObjectMapper();
//            System.out.println("-----------------------------------");
//            System.out.println(mapper.writeValueAsString(actual));
//            System.out.println("-----------------------------------");
            assertViaJson(mapper.readTree(jsonString), mapper.readTree(mapper.writeValueAsString(actual)), idSuppressors);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void assertViaJson(JsonNode expectedNode, JsonNode actualNode, IdSuppressor[] idSuppressors) {
        suppress(idSuppressors, expectedNode);
        suppress(idSuppressors, actualNode);
        boolean equals = expectedNode.equals(actualNode);
        if (!equals) {
            displayDifferences(expectedNode, actualNode, JsonPointer.compile(null));
            displayDifferences(actualNode, expectedNode, JsonPointer.compile(null));
            throw new AssertionError("\nexpected: " + expectedNode + "\nactual  : " + actualNode);
        }
    }

    public void displayDifferences(JsonNode expectedNode, JsonNode actualNode, JsonPointer jsonPointer) {
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
            ValueNode actualNodeExpected = (ValueNode) actualNode.at(jsonPointer);
            if (!valueNodeExpected.equals(actualNodeExpected)) {
                System.out.println(jsonPointer + " expected: " + valueNodeExpected + " actual: " + actualNodeExpected);
            }
        } else {
            throw new IllegalArgumentException(expectedNode.toString());
        }
    }

    private void suppress(IdSuppressor[] idSuppressors, JsonNode jsonNode) {
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

    private boolean suppress(String jsonPtrExpr, String propertyName, IdSuppressor[] childrenIdSuppressors, JsonNode rootNode) {
        JsonNode objectNode = rootNode.at(jsonPtrExpr);
        if (objectNode.isObject()) {
            ((ObjectNode) objectNode).put(propertyName, "SUPPRESSED");
            suppress(childrenIdSuppressors, objectNode);
            return true;
        }
        return false;
    }

}