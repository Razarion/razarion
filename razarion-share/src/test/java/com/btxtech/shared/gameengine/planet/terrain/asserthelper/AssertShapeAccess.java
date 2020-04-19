package com.btxtech.shared.gameengine.planet.terrain.asserthelper;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.planet.terrain.TerrainService;
import com.btxtech.shared.gameengine.planet.terrain.container.PathingAccess;
import com.btxtech.shared.gameengine.planet.terrain.container.SurfaceAccess;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Beat
 * on 10.11.2017.
 */
public class AssertShapeAccess {
    public static void assertShape(TerrainService terrainService, DecimalPosition from, DecimalPosition to, Class theClass, String resourceName) {
        HashMap<DecimalPosition, ShapeAccessTypeContainer> expectedMap = read(theClass, resourceName).stream().collect(Collectors.toMap(ShapeAccessTypeContainer::getSamplePosition, shapeAccessTypeContainer -> shapeAccessTypeContainer, (a, b) -> b, HashMap::new));

        SurfaceAccess surfaceAccess = terrainService.getSurfaceAccess();
        PathingAccess pathingAccess = terrainService.getPathingAccess();
        for (double x = from.getX(); x < to.getX(); x++) {
            for (double y = from.getY(); y < to.getY(); y++) {
                DecimalPosition samplePosition = new DecimalPosition(x + 0.5, y + 0.5);
                ShapeAccessTypeContainer expected = expectedMap.remove(new DecimalPosition(samplePosition));
                if (expected == null) {
                    Assert.fail("No ShapeAccessTypeContainer for samplePosition: " + samplePosition);
                }
                expected.assertHeight(surfaceAccess.getInterpolatedZ(samplePosition));
                expected.assertNorm(surfaceAccess.getInterpolatedNorm(samplePosition));
                expected.assertTerrainType(pathingAccess.getTerrainType(samplePosition));
            }
        }
    }

    public static void saveShape(TerrainService terrainService, DecimalPosition from, DecimalPosition to, String fileName) {
        SurfaceAccess surfaceAccess = terrainService.getSurfaceAccess();
        PathingAccess pathingAccess = terrainService.getPathingAccess();
        Collection<ShapeAccessTypeContainer> shapeAccessTypeContainers = new ArrayList<>();
        for (double x = from.getX(); x < to.getX(); x++) {
            for (double y = from.getY(); y < to.getY(); y++) {
                ShapeAccessTypeContainer shapeAccessTypeContainer = new ShapeAccessTypeContainer();
                DecimalPosition samplePosition = new DecimalPosition(x + 0.5, y + 0.5);
                shapeAccessTypeContainer.setSamplePosition(samplePosition);
                shapeAccessTypeContainer.setHeight(surfaceAccess.getInterpolatedZ(samplePosition));
                shapeAccessTypeContainer.setNorm(surfaceAccess.getInterpolatedNorm(samplePosition));
                shapeAccessTypeContainer.setTerrainType(pathingAccess.getTerrainType(samplePosition));
                shapeAccessTypeContainers.add(shapeAccessTypeContainer);
            }
        }
        try {
            new ObjectMapper().writeValue(new File(AssertTerrainTile.SAVE_DIRECTORY, fileName), shapeAccessTypeContainers);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Collection<ShapeAccessTypeContainer> read(Class theClass, String resourceName) {
        InputStream inputStream = theClass.getResourceAsStream(resourceName);
        if (inputStream == null) {
            throw new RuntimeException("Resource does not exist: " + theClass.getProtectionDomain().getCodeSource().getLocation().getPath() + "/" + resourceName);
        }
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            return objectMapper.readValue(inputStream, new TypeReference<List<ShapeAccessTypeContainer>>() {
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
