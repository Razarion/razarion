package com.btxtech.servercommon.collada;

import com.btxtech.shared.datatypes.shape.ModelMatrixAnimation;
import com.btxtech.shared.datatypes.shape.Shape3D;
import com.btxtech.shared.datatypes.shape.TimeValueSample;
import com.btxtech.shared.datatypes.shape.TransformationModification;
import com.btxtech.shared.utils.Shape3DUtils;
import com.btxtech.shared.utils.TimeDateUtil;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Beat
 * 27.07.2016.
 */
public class Animation extends NameIdColladaXml {
    private Map<String, Source> sources;
    private Sampler sampler;
    private Channel channel;

    public Animation(Node node) {
        super(node);

        sources = new HashMap<>();
        for (Node sourceNode : getChildren(node, ELEMENT_SOURCE)) {
            Source source = new Source(sourceNode);
            sources.put(source.getId(), source);
        }

        sampler = new Sampler(getChild(node, ELEMENT_SAMPLE)); // More than one
        channel = new Channel(getChild(node, ELEMENT_CHANNEL)); // More than one
    }

    public ModelMatrixAnimation convert(Shape3D shape3D) {
        // Convert to two key frame linear animation
        Source input = sources.get(sampler.getInput().getSourceId());
        Source output = sources.get(sampler.getOutput().getSourceId());
        Source interpolation = sources.get(sampler.getInterpolation().getSourceId());

        // Check source
        int count = input.getFloatArray().getCount();
        verifySource(input, "TIME", "float", count, null);
        verifySource(output, null, "float", count, null);
        verifySource(interpolation, "INTERPOLATION", "name", count, "LINEAR");

        ModelMatrixAnimation modelMatrixAnimation = new ModelMatrixAnimation();
        modelMatrixAnimation.setId(getId());
        modelMatrixAnimation.setElement3D(Shape3DUtils.getElement3D(channel.getTargetId(), shape3D));
        TransformationModification transformationModification = TransformationModification.valueOf(channel.getModification().toUpperCase());
        modelMatrixAnimation.setModification(transformationModification);
        if (transformationModification.axisNeeded()) {
            modelMatrixAnimation.setAxis(ModelMatrixAnimation.Axis.valueOf(channel.getAxis().toUpperCase()));
        }

        List<Double> floatArray = input.getFloatArray().getFloatArray();
        List<TimeValueSample> timeValueSamples = new ArrayList<>();
        for (int i = 0; i < floatArray.size(); i++) {
            TimeValueSample sample = new TimeValueSample();
            sample.setTimeStamp(TimeDateUtil.second2MilliS(floatArray.get(i)));
            sample.setValue(output.getFloatArray().getFloatArray().get(i));
            timeValueSamples.add(sample);
        }
        modelMatrixAnimation.setTimeValueSamples(timeValueSamples);
        return modelMatrixAnimation;
    }

    private void verifySource(Source source, String name, String type, int count, String allValues) {
        List<Param> params = source.getTechniqueCommon().getAccessor().getParams();
        if (params.size() != 1) {
            throw new IllegalArgumentException("Parameter count != 1: " + source);
        }

        if (name != null) {
            if (!params.get(0).getName().equalsIgnoreCase(name)) {
                throw new IllegalArgumentException("Parameter name must be: " + name + ". " + params.get(0));
            }
        }

        if (!params.get(0).getType().equalsIgnoreCase(type)) {
            throw new IllegalArgumentException("Parameter type must be: " + type + ". " + params.get(0));
        }

        if (source.getFloatArray() != null) {
            if (source.getFloatArray().getFloatArray().size() != count) {
                throw new IllegalArgumentException("Illegal size. Expected: " + count + " Actual: " + source.getFloatArray().getFloatArray().size());
            }
        } else if (source.getNameArray() != null) {
            if (source.getNameArray().getNameArray().size() != count) {
                throw new IllegalArgumentException("Illegal size. Expected: " + count + " Actual: " + source.getNameArray().getNameArray().size());
            }
        } else {
            throw new IllegalArgumentException("No values");
        }

        if (allValues != null) {
            if (source.getNameArray() != null) {
                for (String value : source.getNameArray().getNameArray()) {
                    if (!allValues.equalsIgnoreCase(value)) {
                        throw new IllegalArgumentException("Illegal value. Expected: " + allValues + " Actual: " + value);
                    }
                }
            } else {
                throw new IllegalArgumentException("No values");
            }
        }
    }

    @Override
    public String toString() {
        return "Animation{" +
                super.toString() +
                ", sources=" + sources +
                ", sampler=" + sampler +
                ", channel=" + channel +
                '}';
    }
}
