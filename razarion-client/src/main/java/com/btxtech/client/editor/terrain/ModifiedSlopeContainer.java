package com.btxtech.client.editor.terrain;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.MapCollection;
import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.datatypes.Polygon2DRasterizer;
import com.btxtech.shared.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;

/**
 * Created by Beat
 * on 27.11.2017.
 */
public class ModifiedSlopeContainer {
    private static final Logger logger = Logger.getLogger(ModifiedSlopeContainer.class.getName());
    //     private MapCollection<Index, T> innerTiles = new MapCollection<>();
    private MapCollection<Index, ModifiedSlope> piercedTiles = new MapCollection<>();
    private Collection<ModifiedSlope> polygon2Ds = new ArrayList<>();
    private int rasterSize;

    public ModifiedSlopeContainer(int rasterSize) {
        this.rasterSize = rasterSize;
    }

    public void setPolygons(Collection<ModifiedSlope> polygonProviders) {
//         innerTiles.clear();
        piercedTiles.clear();
        polygon2Ds.clear();
        polygonProviders.forEach(this::add);
    }

    public Collection<ModifiedSlope> getPolygons() {
        return polygon2Ds;
    }

    public void add(ModifiedSlope modifiedSlope) {
        polygon2Ds.add(modifiedSlope);
        Polygon2DRasterizer polygon2DRasterizer = Polygon2DRasterizer.create(modifiedSlope.getPolygon(), rasterSize);
//        polygon2DRasterizer.getInnerTiles().forEach(index -> innerTiles.put(index, modifiedSlope));
        polygon2DRasterizer.getPiercedTiles().forEach(index -> piercedTiles.put(index, modifiedSlope));
    }

    public void remove(ModifiedSlope modifiedSlope) {
        Collection<Index> indexToRemove = new ArrayList<>();
        piercedTiles.iterate((index, storedModifiedSlope) -> {
            if (storedModifiedSlope.equals(modifiedSlope)) {
                indexToRemove.add(index);
            }
            return true;
        });
        indexToRemove.forEach(index -> piercedTiles.remove(index, modifiedSlope));
        polygon2Ds.remove(modifiedSlope);
    }

    public void update(ModifiedSlope modifiedSlope) {
        remove(modifiedSlope);
        add(modifiedSlope);
    }

    public ModifiedSlope getPolygonAt(Polygon2D area) {
//        Collection<T> inner = innerTiles.get(position.divide(rasterSize).toIndexFloor());
//        if (inner != null && !inner.isEmpty()) {
//            if (inner.size() == 1) {
//                return CollectionUtils.getFirst(inner);
//            } else {
//                logger.severe("ModifiedSlopeContainer.getPolygonAt() More then one polygon in inner found at: " + position);
//                return CollectionUtils.getFirst(inner);
//            }
//        }
        for (DecimalPosition position : area.getCorners()) {
            Collection<ModifiedSlope> pierced = piercedTiles.get(position.divide(rasterSize).toIndexFloor());
            if (pierced != null && !pierced.isEmpty()) {
                if (pierced.size() == 1) {
                    if (CollectionUtils.getFirst(pierced).getPolygon().isInside(position)) {
                        return CollectionUtils.getFirst(pierced);
                    }
                } else {
                    logger.severe("ModifiedSlopeContainer.getPolygonAt() More then one polygon in inner found at: " + position);
                    if (CollectionUtils.getFirst(pierced).getPolygon().isInside(position)) {
                        return CollectionUtils.getFirst(pierced);
                    }
                }
            }
        }

        return null;
    }
}
