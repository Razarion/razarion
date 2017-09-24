package com.btxtech.shared.system;

/**
 * Created by Beat
 * 25.04.2017.
 */
public interface ConnectionMarshaller {
    // Do not use Collections with generic types as top level parameter e.g. Map<Integer, Integer> List<Double>

    interface Packet {
        Class getTheClass();

        String name();
    }

    String PACKAGE_DELIMITER = "#";

    static String marshall(Packet packet, String jsonParam) {
        return packet.name() + PACKAGE_DELIMITER + jsonParam;
    }

    static <E extends Enum<E>> E deMarshallPackage(String text, Class<E> theClass) {
        int delimiterOffset = text.indexOf(PACKAGE_DELIMITER);
        if (delimiterOffset < 0) {
            throw new IllegalArgumentException("Can not parse msg. Delimiter missing: " + text);
        }
        return Enum.valueOf(theClass, text.substring(0, delimiterOffset));
    }

    static String deMarshallPayload(String text) {
        int delimiterOffset = text.indexOf(PACKAGE_DELIMITER);
        if (delimiterOffset < 0) {
            throw new IllegalArgumentException("Can not parse msg. Delimiter missing: " + text);
        }
        return text.substring(delimiterOffset + 1);
    }
}
