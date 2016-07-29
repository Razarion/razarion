package com.btxtech.servercommon.collada;

import org.w3c.dom.Node;

/**
 * Created by Beat
 * 27.07.2016.
 */
public class Sampler extends NameIdColladaXml {
    private Input input;
    private Input output;
    private Input interpolation;

    public Sampler(Node node) {
        super(node);

        for (Node inputNode : getChildren(node, ELEMENT_INPUT)) {
            Input inputElement = new Input(inputNode);
            switch (inputElement.getSemantic()) {
                case SEMANTIC_INPUT:
                    input = inputElement;
                    break;
                case SEMANTIC_OUTPUT:
                    output = inputElement;
                    break;
                case SEMANTIC_INTERPOLATION:
                    interpolation = inputElement;
                    break;
                default:
                    System.out.println("Unknown inout in Sampler: " + inputElement);
            }
        }
    }

    public Input getInput() {
        return input;
    }

    public Input getOutput() {
        return output;
    }

    public Input getInterpolation() {
        return interpolation;
    }

    @Override
    public String toString() {
        return "Sampler{" +
                super.toString() +
                " input=" + input +
                ", output=" + output +
                ", interpolation=" + interpolation +
                '}';
    }
}
