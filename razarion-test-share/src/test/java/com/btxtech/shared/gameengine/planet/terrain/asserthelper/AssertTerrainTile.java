package com.btxtech.shared.gameengine.planet.terrain.asserthelper;

import com.btxtech.shared.JsonAssert;
import com.btxtech.shared.TestHelper;
import com.btxtech.shared.datatypes.Float32ArrayEmu;
import com.btxtech.shared.gameengine.planet.terrain.TerrainTile;
import com.btxtech.shared.mocks.TestFloat32Array;
import com.btxtech.shared.utils.CollectionUtils;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdKeyDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ValueNode;
import com.fasterxml.jackson.databind.ser.std.StdKeySerializer;
import org.junit.Assert;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static com.fasterxml.jackson.databind.deser.std.StdKeyDeserializer.TYPE_INT;

/**
 * Created by Beat
 * 09.04.2017.
 */
public class AssertTerrainTile {
    public static final String SAVE_DIRECTORY = TestHelper.SAVE_DIRECTORY + "terrain";
    private Collection<TerrainTile> expected;


    public static void assertTerrainTile(Class theClass, String resourceName, List<TerrainTile> actualTiles) {
        assertTerrainTile(theClass, resourceName, actualTiles, false);
    }

    public static void assertTerrainTile(Class theClass, String resourceName, List<TerrainTile> actualTiles, boolean save) {
        JsonAssert.TEST_RESOURCE_FOLDER = AssertTerrainTile.SAVE_DIRECTORY;
        JsonAssert.assertViaJson(resourceName, null, null, theClass, actualTiles, createObjectMapper(),save);
    }

    @Deprecated
    public AssertTerrainTile(Class theClass, String resourceName) {
        InputStream inputStream = theClass.getResourceAsStream(resourceName);
        if (inputStream == null) {
            throw new RuntimeException("Resource does not exist: " + theClass.getProtectionDomain().getCodeSource().getLocation().getPath() + "/" + resourceName);
        }
        try {
            expected = createObjectMapper().readValue(inputStream, new TypeReference<List<TerrainTile>>() {
            });
            expected.forEach(this::oldExpectedFilter);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (expected.isEmpty()) {
            throw new RuntimeException("expected.isEmpty()");
        }
    }

    /**
     * Temporary to fix wrong test-expected json files
     *
     * @param expectedTerrainTile
     */
    private void oldExpectedFilter(TerrainTile expectedTerrainTile) {
        // TestFloat32Array groundPositions = (TestFloat32Array) expectedTerrainTile.getGroundPositions();
        // groundPositions.setVertices(groundPositions.getVertices().subList(0, 2400));
    }

    @Deprecated
    public void assertEquals(Collection<TerrainTile> actual) {
        Assert.assertEquals("TerrainTile count does not match", expected.size(), actual.size());

        for (TerrainTile expectedTile : expected) {
            boolean found = false;
            for (TerrainTile actualTile : actual) {
                if (expectedTile.getIndex().equals(actualTile.getIndex())) {
                    compare(expectedTile, actualTile);
                    found = true;
                    break;
                }
            }
            if (!found) {
                Assert.fail("Unexpected TerrainTile: " + expectedTile.getIndex());
            }
        }
    }

    @Deprecated
    public void assertEquals(TerrainTile actual) {
        if (expected.size() != 1) {
            Assert.fail("Expected size does not match one single TerrainTile. Expected size: " + expected.size());
        }
        compare(CollectionUtils.getFirst(expected), actual);
    }

    private void compare(TerrainTile expected, TerrainTile actual) {
        try {
            // https://www.baeldung.com/jackson-compare-two-json-objects
            ObjectMapper mapper = createObjectMapper();
            JsonNode expectedNode = mapper.readTree(mapper.writeValueAsString(expected));
            JsonNode actualNode = mapper.readTree(mapper.writeValueAsString(actual));

            boolean equals = expectedNode.equals(actualNode);
            if (!equals) {
                displayDifferences(expectedNode, actualNode, JsonPointer.compile(null));
                displayDifferences(actualNode, expectedNode, JsonPointer.compile(null));
                throw new AssertionError("\nexpected: " + expectedNode + "\nactual  : " + actualNode);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Deprecated
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

    private static ObjectMapper createObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.getSerializerProvider().setNullKeySerializer(new StdKeySerializer() {

            @Override
            public void serialize(Object value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
                jgen.writeFieldName("null");
            }
        });
        SimpleModule module = new SimpleModule();
        module.addSerializer(Float32ArrayEmu.class, new JsonSerializer<Float32ArrayEmu>() {
            @Override
            public void serialize(Float32ArrayEmu value, JsonGenerator jgen, SerializerProvider serializers) throws IOException {
                ObjectMapper mapper = (ObjectMapper) jgen.getCodec();
                jgen.writeRawValue(mapper.writeValueAsString(((TestFloat32Array) value).getDoubles()));
            }
        });
        module.addDeserializer(Float32ArrayEmu.class, new StdDeserializer<Float32ArrayEmu>(Float32ArrayEmu.class) {
            @Override
            public Float32ArrayEmu deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                ObjectMapper mapper = (ObjectMapper) p.getCodec();
                double[] value = mapper.readValue(p, double[].class);
                return new TestFloat32Array().doubles(value);
            }
        });
        module.addKeyDeserializer(Integer.class, new StdKeyDeserializer(TYPE_INT, Integer.class) {

            @Override
            public Object deserializeKey(String key, DeserializationContext ctxt) throws IOException, JsonProcessingException {
                if ("null".equals(key)) {
                    return null;
                }
                return super.deserializeKey(key, ctxt);
            }
        });
        objectMapper.registerModule(module);
        return objectMapper;
    }
}
