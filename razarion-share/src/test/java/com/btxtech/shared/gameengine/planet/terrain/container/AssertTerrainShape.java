package com.btxtech.shared.gameengine.planet.terrain.container;

import com.btxtech.shared.gameengine.planet.terrain.container.nativejs.NativeTerrainShape;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.unitils.reflectionassert.ReflectionAssert;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Beat
 * on 02.10.2017.
 */
public interface AssertTerrainShape {
    String SAVE_DIRECTORY = "C:\\dev\\projects\\razarion\\code\\razarion\\razarion-share\\src\\test\\resources\\com\\btxtech\\shared\\gameengine\\planet\\terrain\\container";


    static void assertTerrainShape(Class theClass, String resourceName, TerrainShape actualTerrainShape) {
        InputStream inputStream = theClass.getResourceAsStream(resourceName);
        if (inputStream == null) {
            throw new RuntimeException("Resource does not exist: " + theClass.getProtectionDomain().getCodeSource().getLocation().getPath() + "/" + resourceName);
        }
        NativeTerrainShape expected;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            expected = objectMapper.readValue(inputStream, NativeTerrainShape.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ReflectionAssert.assertReflectionEquals(expected, actualTerrainShape.toNativeTerrainShape());
    }

    static void saveTerrainShape(TerrainShape terrainShape, String fileName) {
        try {
            new ObjectMapper().writeValue(new File(SAVE_DIRECTORY, fileName), terrainShape.toNativeTerrainShape());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
