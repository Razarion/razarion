package com.btxtech.shared.gameengine.planet.connection;

import com.btxtech.shared.datatypes.DecimalPosition;

/**
 * Created by Beat
 * 20.04.2017.
 */
public abstract class AbstractServerConnection {
    public static final String PACKAGE_DELIMITER = "#";

    public enum Package {
        CREATE_BASE(DecimalPosition.class);

        private Class theClass;

        Package(Class theClass) {
            this.theClass = theClass;
        }

        public Class getTheClass() {
            return theClass;
        }
    }

    protected abstract void sendToServer(Package aPackage, Object param);

    public abstract void init();

    public void createHumanBaseWithBaseItem(DecimalPosition position) {
        sendToServer(Package.CREATE_BASE, position);
    }
}
