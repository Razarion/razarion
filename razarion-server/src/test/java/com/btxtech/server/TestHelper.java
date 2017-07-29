package com.btxtech.server;

import com.btxtech.shared.dto.ObjectNameId;
import org.junit.Assert;

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
            if(!actualName.remove(name)) {
                Assert.fail("Name not found: " + name);
            }
        }
        if(!actualName.isEmpty()) {
            Assert.fail("Not all names where used: " + actualName);
        }
    }

    static int findIdForName(List<ObjectNameId> objectNameIds, String name) {
        return objectNameIds.stream().filter(objectNameId -> objectNameId.getInternalName().equalsIgnoreCase(name)).findFirst().map(ObjectNameId::getId).orElseThrow(() -> new IllegalArgumentException("No ObjectNameId for name: " + name));
    }
}
