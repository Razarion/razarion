package com.btxtech.servercommon.collada;

import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Beat
 * 06.06.2016.
 */
public class Effect extends NameIdColladaXml {
    private Map<String, List<Technique>> profiles = new HashMap<>();

    public Effect(Node node) {
        super(node);
        for (Node profile : getChildrenWithPrefix(node, ELEMENT_PROFILE_)) {
            List<Technique> techniques = new ArrayList<>();
            for (Node technique : getChildren(profile, ELEMENT_TECHNIQUE)) {
                techniques.add(new Technique(technique));
            }
            profiles.put(profile.getNodeName(), techniques);
        }
    }

    public Technique getTechnique() {
        for (List<Technique> techniques : profiles.values()) {
            if (!techniques.isEmpty()) {
                return techniques.get(0);
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "Effect{" +
                "super" + super.toString() +
                " profiles=" + profiles +
                '}';
    }
}
