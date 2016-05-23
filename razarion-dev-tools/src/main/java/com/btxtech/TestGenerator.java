package com.btxtech;

import com.btxtech.game.jsre.client.common.Index;

import java.util.List;

/**
 * Created by Beat
 * 16.07.2015.
 */
public class TestGenerator {

    /**
     * Deprecated: use from Index.testString
     */
    @Deprecated
    public static String generateIndexList(List<Index> indexList) {
        StringBuilder builder = new StringBuilder();
        builder.append("List<Index> positions = Arrays.asList(");
        for (int i = 0; i < indexList.size(); i++) {
            Index index = indexList.get(i);
            builder.append(index.testString());
            if(i <indexList.size()-1) {
                builder.append(", ");
            }
        }
        builder.append(");");
        return builder.toString();
    }
}
