package com.btxtech.shared.utils;

import com.btxtech.shared.datatypes.DecimalPosition;

import java.util.List;

/**
 * Created by Beat
 * on 05.06.2017.
 */
public interface DevUtils {
    static void printPolygon(List<DecimalPosition> indexList) {
        System.out.println("-----------------------------------------------------------");
        StringBuilder builder = new StringBuilder();
        builder.append("List<DecimalPosition> positions = Arrays.asList(");
        for (int i = 0; i < indexList.size(); i++) {
            DecimalPosition decimalPosition = indexList.get(i);
            builder.append("new DecimalPosition(").append(decimalPosition.getX()).append(", ").append(decimalPosition.getY()).append(")");
            if (i < indexList.size() - 1) {
                builder.append(", ");
            }
        }
        builder.append(");");
        System.out.println(builder);
        System.out.println("-----------------------------------------------------------");
    }

}
