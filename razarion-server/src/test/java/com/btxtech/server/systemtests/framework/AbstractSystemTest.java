package com.btxtech.server.systemtests.framework;

import com.btxtech.server.ServerTestHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;

import java.io.IOException;
import java.io.InputStream;

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

    @Before
    public void setupRestClient() {
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

    protected void assertViaJson(String expectedResource, Class resourceLoader, Object actual) {
        try {
            InputStream inputStream = resourceLoader.getResourceAsStream(expectedResource);
            if(inputStream == null) {
                throw new IOException("No such resource: " + expectedResource);
            }
            // https://www.baeldung.com/jackson-compare-two-json-objects
            ObjectMapper mapper = new ObjectMapper();
//            System.out.println("-----------------------------------");
//            System.out.println(mapper.writeValueAsString(actual));
//            System.out.println("-----------------------------------");
            assertEquals(mapper.readTree(inputStream), mapper.readTree(mapper.writeValueAsString(actual)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}