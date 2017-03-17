package com.btxtech.server.collada;

import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 27.07.2016.
 */
public class NameArray extends NameIdColladaXml {
    private int count;
    private List<String> nameArray = new ArrayList<>();

    public NameArray(Node node) {
        super(node);
        count = getAttributeAsInt(node, ATTRIBUTE_COUNT);

        nameArray = getElementAsStringList(node);

        if (count != nameArray.size()) {
            throw new ColladaRuntimeException("Count and parsed float array count are not the same");
        }
    }

    public List<String> getNameArray() {
        return nameArray;
    }

    public int getCount() {
        return count;
    }

    @Override
    public String toString() {
        return "NameArray{" +
                "count=" + count +
                ", nameArray=" + nameArray +
                "} " + super.toString();
    }
}
