package com.btxtech.shared.gameengine.planet.connection;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.datatypes.packets.PlayerBaseInfo;
import com.btxtech.shared.gameengine.datatypes.packets.SyncBaseItemInfo;

/**
 * Created by Beat
 * 21.04.2017.
 */
public interface ConnectionMarshaller {
    String PACKAGE_DELIMITER = "#";

    enum Package {
        CREATE_BASE(DecimalPosition.class),
        BASE_CREATED(PlayerBaseInfo.class),
        SYNC_BASE_ITEM_CHANGED(SyncBaseItemInfo.class);

        private Class theClass;

        Package(Class theClass) {
            this.theClass = theClass;
        }

        public Class getTheClass() {
            return theClass;
        }
    }

    static String marshall(Package aPackage, String jsonParam) {
        return aPackage.name() + PACKAGE_DELIMITER + jsonParam;
    }

    static Package deMarshallPackage(String text) {
        int delimiterOffset = text.indexOf(PACKAGE_DELIMITER);
        if (delimiterOffset < 0) {
            throw new IllegalArgumentException("Can not parse msg. Delimiter missing: " + text);
        }
        return Package.valueOf(text.substring(0, delimiterOffset));
    }

    static String deMarshallPayload(String text) {
        int delimiterOffset = text.indexOf(PACKAGE_DELIMITER);
        if (delimiterOffset < 0) {
            throw new IllegalArgumentException("Can not parse msg. Delimiter missing: " + text);
        }
        return text.substring(delimiterOffset + 1);
    }
}
