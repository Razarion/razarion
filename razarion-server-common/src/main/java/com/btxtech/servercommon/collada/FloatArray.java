package com.btxtech.servercommon.collada;

import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 14.08.2015.
 */
public class FloatArray extends NameIdColladaXml {
    private int count;
    private List<Double> floatArray = new ArrayList<>();

    public FloatArray(Node node) {
        super(node);
        count = getAttributeAsInt(node, ATTRIBUTE_COUNT);

        floatArray = getElementAsDoubleList(node);

        if (count != floatArray.size()) {
            throw new ColladaRuntimeException("Count and parsed float array count are not the same");
        }
    }

    public List<Double> getFloatArray() {
        return floatArray;
    }

    public int getCount() {
        return count;
    }

    @Override
    public String toString() {
        return "FloatArray{" +
                "count=" + count +
                ", floatArray=" + floatArray +
                "} " + super.toString();
    }
}
