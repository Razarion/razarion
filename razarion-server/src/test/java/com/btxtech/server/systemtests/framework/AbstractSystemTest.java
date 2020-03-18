package com.btxtech.server.systemtests.framework;

import com.btxtech.server.ServerTestHelper;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

/**
 * Created by Beat
 * 05.05.2017.
 */
public abstract class AbstractSystemTest extends ServerTestHelper {
    private RestConnection defaultRestConnection = new RestConnection(null);

    protected <T> T setupRestAccess(Class<T> clazz) {
        return defaultRestConnection.proxy(clazz);
    }

    public RestConnection getDefaultRestConnection() {
        return defaultRestConnection;
    }

    protected void assertViaJson(Object expected, Object actual) {
        try {
            // https://www.baeldung.com/jackson-compare-two-json-objects
            ObjectMapper mapper = new ObjectMapper();
            assertEquals(mapper.readTree(mapper.writeValueAsString(expected)), mapper.readTree(mapper.writeValueAsString(actual)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void assertViaJson(String expectedResource, Function<String, String> replacer, Class resourceLoader, Object actual) {
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
            assertEquals(mapper.readTree(jsonString), mapper.readTree(mapper.writeValueAsString(actual)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}