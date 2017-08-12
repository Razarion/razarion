package com.btxtech.server;

import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Beat
 * on 29.07.2017.
 */
public interface TestHelper {

    static void assertObjectNameIds(Collection<ObjectNameId> actual, String... expectedNames) {
        Assert.assertEquals("Size is not the same", expectedNames.length, actual.size());
        Collection<String> actualName = actual.stream().map(ObjectNameId::getInternalName).collect(Collectors.toList());

        for (String name : expectedNames) {
            if (!actualName.remove(name)) {
                Assert.fail("Name not found: " + name);
            }
        }
        if (!actualName.isEmpty()) {
            Assert.fail("Not all names where used: " + actualName);
        }
    }

    static void assertOrderedObjectNameIds(List<ObjectNameId> actual, String... expectedNames) {
        Assert.assertEquals("Size is not the same", expectedNames.length, actual.size());

        for (int i = 0; i < expectedNames.length; i++) {
            Assert.assertEquals(expectedNames[i], actual.get(i).getInternalName());
        }
    }

    static int findIdForName(List<ObjectNameId> objectNameIds, String name) {
        return objectNameIds.stream().filter(objectNameId -> objectNameId.getInternalName().equalsIgnoreCase(name)).findFirst().map(ObjectNameId::getId).orElseThrow(() -> new IllegalArgumentException("No ObjectNameId for name: " + name));
    }

    static void assertIds(Collection<Integer> actualIds, Integer... expectedIds) {
        Assert.assertEquals(expectedIds.length, actualIds.size());
        Collection<Integer> expectedCollection = new ArrayList<>(Arrays.asList(expectedIds));
        expectedCollection.removeAll(actualIds);
        Assert.assertTrue(expectedCollection.isEmpty());
    }

    static PlaceConfig placeConfigPolygonFromRect(double x, double y, double width, double height) {
        PlaceConfig placeConfig = new PlaceConfig();
        placeConfig.setPolygon2D(Polygon2D.fromRectangle(x, y, width, height));
        return placeConfig;
    }
}
