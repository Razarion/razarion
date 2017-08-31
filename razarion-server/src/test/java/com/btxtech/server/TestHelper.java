package com.btxtech.server;

import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.dto.ObjectNameIdProvider;
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

        StringBuilder actualNamesString = new StringBuilder();
        actualName.forEach(s -> actualNamesString.append(s).append(" "));

        for (String name : expectedNames) {
            if (!actualName.remove(name)) {
                Assert.fail("Name not found: " + name + " Available: '" + actualNamesString + "'");
            }
        }
        if (!actualName.isEmpty()) {
            Assert.fail("Not all names where used: " + actualName + " Available: '" + actualNamesString + "'");
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

    static <T extends ObjectNameIdProvider> T findObjectForId(Collection<T> objects, int id) {
        for (T object : objects) {
            if (object.createObjectNameId().getId() == id) {
                return object;
            }
        }
        throw new IllegalArgumentException("No Object for id found: " + id);
    }

    static void assertIds(Collection<Integer> actualIds, Integer... expectedIds) {
        Assert.assertEquals(expectedIds.length, actualIds.size());
        Collection<Integer> expectedCollection = new ArrayList<>(Arrays.asList(expectedIds));
        expectedCollection.removeAll(actualIds);
        Assert.assertTrue(expectedCollection.isEmpty());
    }

    static void assertObjectNameIdProviders(Collection<? extends ObjectNameIdProvider> actualIds, Integer... expectedIds) {
        Assert.assertEquals(expectedIds.length, actualIds.size());
        Collection<Integer> expectedCollection = actualIds.stream().map(o -> o.createObjectNameId().getId()).collect(Collectors.toList());
        expectedCollection.removeAll(Arrays.asList(expectedIds));
        Assert.assertTrue(expectedCollection.isEmpty());
    }

    static PlaceConfig placeConfigPolygonFromRect(double x, double y, double width, double height) {
        PlaceConfig placeConfig = new PlaceConfig();
        placeConfig.setPolygon2D(Polygon2D.fromRectangle(x, y, width, height));
        return placeConfig;
    }

    static void assertCollection(Collection<Integer> actual, Integer... expected) {
        if (actual == null) {
            if(expected.length == 0) {
                return;
            } else {
                Assert.fail("actual == null && expected.length == 0)");
            }
        }
        Assert.assertEquals(expected.length, actual.size());
        Collection<Integer> actualClone = new ArrayList<>(actual);
        actualClone.removeAll(Arrays.asList(expected));
        Assert.assertTrue(actualClone.isEmpty());
    }
}
