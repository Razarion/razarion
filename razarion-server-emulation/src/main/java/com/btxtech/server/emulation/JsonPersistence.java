package com.btxtech.server.emulation;

import com.btxtech.shared.system.ExceptionHandler;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 07.07.2016.
 */
@Singleton
public class JsonPersistence {
    private static final String DEV_TOOL_RESOURCE_DIR = "C:\\dev\\projects\\razarion\\code\\razarion\\razarion-server-emulation\\src\\main\\resources";
    @Inject
    private Logger logger;
    @Inject
    private ExceptionHandler exceptionHandler;

    public <T> T readJson(String fileName, Class<T> clazz) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            // objectMapper.configure(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS, false);
            objectMapper.setVisibility(JsonMethod.ALL, JsonAutoDetect.Visibility.NONE);
            objectMapper.setVisibility(JsonMethod.FIELD, JsonAutoDetect.Visibility.ANY);
            objectMapper.enableDefaultTyping();
            return objectMapper.readValue(getFileReader(fileName), clazz);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void writeJson(String fileName, Object object) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS, false);
            objectMapper.setVisibility(JsonMethod.ALL, JsonAutoDetect.Visibility.NONE);
            objectMapper.setVisibility(JsonMethod.FIELD, JsonAutoDetect.Visibility.ANY);
            objectMapper.enableDefaultTyping();
            objectMapper.writeValue(getFileWriter(fileName), object);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private File getFile(String fileName) {
        return new File(DEV_TOOL_RESOURCE_DIR, fileName);
    }

    private FileWriter getFileWriter(String fileName) throws IOException {
        return new FileWriter(getFile(fileName));
    }

    private FileReader getFileReader(String fileName) throws IOException {
        return new FileReader(getFile(fileName));
    }
}
