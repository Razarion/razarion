package com.btxtech.shared.datatypes;

import com.btxtech.shared.utils.CollectionUtils;
import com.btxtech.shared.utils.GeometricUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Beat
 * on 11.11.2017.
 */
public class Polygon2DRasterizer {
    private final Polygon2D polygon;
    private final int rasterSize;
    private Map<Index, Integer> piercedTiles = new HashMap<>();
    private List<Index> piercedTileOrder = new ArrayList<>();
    private Set<Index> openInside = new HashSet<>();
    private Set<Index> closedInside = new HashSet<>();

    public static Polygon2DRasterizer create(Polygon2D polygon, int rasterSize) {
        polygon.checkForLineCrossing();
        Polygon2DRasterizer polygon2DRasterizer = new Polygon2DRasterizer(polygon, rasterSize);
        polygon2DRasterizer.rasterize();
        return polygon2DRasterizer;
    }

    private Polygon2DRasterizer(Polygon2D polygon, int rasterSize) {
        this.polygon = polygon;
        this.rasterSize = rasterSize;
    }

    private void rasterize() {
        findPiercedTiles();
        openInside = findFirstLayerInsideTiles();
        findAllInside();
    }

    private void findPiercedTiles() {
        polygon.getLines().stream().map(line -> GeometricUtil.rasterizeLine(line, rasterSize)).forEach(indices -> indices.forEach(index -> {

            if (piercedTileOrder.isEmpty()) {
                piercedTiles.put(index, 1);
                piercedTileOrder.add(index);
            } else if (!piercedTileOrder.get(piercedTileOrder.size() - 1).equals(index)) {
                piercedTileOrder.add(index);
                Integer count = piercedTiles.get(index);
                if (count == null) {
                    count = 0;
                }
                count++;
                piercedTiles.put(index, count);
            }
        }));
        if (piercedTileOrder.size() > 1) {
            if (piercedTileOrder.get(piercedTileOrder.size() - 1).equals(piercedTileOrder.get(0))) {
                piercedTileOrder.remove(0);
            }
        }
    }

    private Set<Index> findFirstLayerInsideTiles() {
        Set<Index> innerTiles = new HashSet<>();
        for (int i = 0; i < piercedTileOrder.size(); i++) {
            Index current = piercedTileOrder.get(i);
            if (piercedTiles.get(current) > 1) {
                continue;
            }

            Index pref = CollectionUtils.getCorrectedElement(i - 1, piercedTileOrder);
            Index nex = CollectionUtils.getCorrectedElement(i + 1, piercedTileOrder);
            for (Index innerTile : findPossibleInside(current, pref, nex)) {
                if (!piercedTiles.containsKey(innerTile)) {
                    innerTiles.add(innerTile);
                }
            }
        }
        return innerTiles;
    }

    private Collection<Index> findPossibleInside(Index current, Index pref, Index nex) {
        Index.Direction directionPref = current.getDirection(pref);
        Index.Direction directionNext = current.getDirection(nex);
        if (directionPref == directionNext) {
            return Collections.emptyList();
        }
        switch (directionPref) {
            case N:
                switch (directionNext) {
                    case S:
                        return Collections.singletonList(current.add(1, 0));
                    case W:
                        return Arrays.asList(current.add(1, 0), current.add(0, -1));
                }
                break;
            case E:
                switch (directionNext) {
                    case W:
                        return Collections.singletonList(current.add(0, -1));
                    case N:
                        return Arrays.asList(current.add(0, -1), current.add(-1, 0));
                }
                break;
            case S:
                switch (directionNext) {
                    case N:
                        return Collections.singletonList(current.add(-1, 0));
                    case E:
                        return Arrays.asList(current.add(-1, 0), current.add(0, 1));
                }
                break;
            case W:
                switch (directionNext) {
                    case E:
                        return Collections.singletonList(current.add(0, 1));
                    case S:
                        return Arrays.asList(current.add(0, 1), current.add(1, 0));
                }
                break;
        }
        return Collections.emptyList();
    }

    public Set<Index> getPiercedTiles() {
        return piercedTiles.keySet();
    }

    private void findAllInside() {
        while (!openInside.isEmpty()) {
            Index current = CollectionUtils.removeFirst(openInside);
            findAllNeighbours(current);
            closedInside.add(current);
        }
    }

    private void findAllNeighbours(Index current) {
        checkAndAdd(current, 1, 0);
        checkAndAdd(current, -1, 0);
        checkAndAdd(current, 0, 1);
        checkAndAdd(current, 0, -1);
    }

    private void checkAndAdd(Index current, int x, int y) {
        Index neighbour = current.add(x, y);
        if (!piercedTiles.containsKey(neighbour) && !openInside.contains(neighbour) && !closedInside.contains(neighbour)) {
            openInside.add(neighbour);
        }
    }


    public Set<Index> getInnerTiles() {
        return closedInside;
    }
}
