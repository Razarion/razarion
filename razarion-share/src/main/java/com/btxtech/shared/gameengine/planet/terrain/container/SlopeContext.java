package com.btxtech.shared.gameengine.planet.terrain.container;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.gameengine.planet.terrain.slope.Slope;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Beat
 * on 23.06.2017.
 */
public class SlopeContext {
    private Slope slope;
    private Map<Index, List<List<DecimalPosition>>> innerPiercings = new HashMap<>();
    private Map<Index, List<List<DecimalPosition>>> outerPiercings = new HashMap<>();

    public SlopeContext(Slope slope) {
        this.slope = slope;
    }

    public Slope getSlope() {
        return slope;
    }

    public boolean exitsInSlopeGroundPiercing(Index nodeIndex, boolean isOuter, DecimalPosition position) {
        if (isOuter) {
            List<List<DecimalPosition>> nodePiercings = outerPiercings.get(nodeIndex);
            if (nodePiercings != null) {
                return nodePiercings.stream().anyMatch(nodePiercing -> nodePiercing.contains(position));
            }
        } else {
            List<List<DecimalPosition>> nodePiercings = innerPiercings.get(nodeIndex);
            if (nodePiercings != null) {
                return nodePiercings.stream().anyMatch(nodePiercing -> nodePiercing.contains(position));
            }
        }
        return false;
    }

    public void addSlopeGroundPiercing(Index nodeIndex, boolean isOuter, List<DecimalPosition> piercingLine) {
        if (isOuter) {
            outerPiercings.computeIfAbsent(nodeIndex, index -> new ArrayList<>()).add(piercingLine);
        } else {
            Collections.reverse(piercingLine);
            innerPiercings.computeIfAbsent(nodeIndex, index -> new ArrayList<>()).add(piercingLine);
        }
    }

    public List<List<DecimalPosition>> getInnerPiercings(Index nodeIndex) {
        return innerPiercings.get(nodeIndex);
    }

    public List<List<DecimalPosition>> getOuterPiercings(Index nodeIndex) {
        return outerPiercings.get(nodeIndex);
    }
}
