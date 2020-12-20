package com.btxtech.client.editor.terrain;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.MapCollection;
import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.datatypes.Polygon2DRasterizer;
import com.btxtech.shared.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * on 27.11.2017.
 */
public class EditorSlopeWrapperContainer {
    private static final Logger logger = Logger.getLogger(EditorSlopeWrapperContainer.class.getName());
    private MapCollection<Index, EditorSlopeWrapper> innerTiles = new MapCollection<>();
    private MapCollection<Index, EditorSlopeWrapper> piercedTiles = new MapCollection<>();
    private Collection<EditorSlopeWrapper> polygon2Ds = new ArrayList<>();
    private List<Integer> deletedSlopeIds = new ArrayList<>();
    private int rasterSize;

    public EditorSlopeWrapperContainer(int rasterSize) {
        this.rasterSize = rasterSize;
    }

    public void setPolygons(Collection<EditorSlopeWrapper> polygonProviders) {
        innerTiles.clear();
        piercedTiles.clear();
        polygon2Ds.clear();
        polygonProviders.forEach(this::add);
    }

    public Collection<EditorSlopeWrapper> getPolygons() {
        return polygon2Ds;
    }

    public void add(EditorSlopeWrapper modifiedSlope) {
        try {
            Polygon2DRasterizer polygon2DRasterizer = Polygon2DRasterizer.create(modifiedSlope.getPolygon(), rasterSize);
            polygon2DRasterizer.getInnerTiles().forEach(index -> innerTiles.put(index, modifiedSlope));
            polygon2DRasterizer.getPiercedTiles().forEach(index -> piercedTiles.put(index, modifiedSlope));
            polygon2Ds.add(modifiedSlope);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "ModifiedSlopeContainer.add(). Original id:" + modifiedSlope.getOriginalId(), e);
        }
    }

    public void remove(EditorSlopeWrapper modifiedSlope) {
        removeInternal(modifiedSlope);
        if (!modifiedSlope.isCreated()) {
            deletedSlopeIds.add(modifiedSlope.getOriginalId());
        }
    }

    private void removeInternal(EditorSlopeWrapper modifiedSlope) {
        Collection<Index> indexToRemove = new ArrayList<>();
        addTiles(innerTiles, modifiedSlope, indexToRemove);
        addTiles(piercedTiles, modifiedSlope, indexToRemove);
        indexToRemove.forEach(index -> innerTiles.remove(index, modifiedSlope));
        indexToRemove.forEach(index -> piercedTiles.remove(index, modifiedSlope));
        polygon2Ds.remove(modifiedSlope);
    }

    private void addTiles(MapCollection<Index, EditorSlopeWrapper> indices, EditorSlopeWrapper modifiedSlope, Collection<Index> indexToRemove) {
        indices.iterate((index, storedModifiedSlope) -> {
            if (storedModifiedSlope.equals(modifiedSlope)) {
                indexToRemove.add(index);
            }
            return true;
        });
    }

    public void update(EditorSlopeWrapper modifiedSlope) {
        removeInternal(modifiedSlope);
        add(modifiedSlope);
    }

    public List<Integer> getAndClearDeletedSlopeIds() {
        List<Integer> tmp = deletedSlopeIds;
        deletedSlopeIds = new ArrayList<>();
        return tmp;
    }

    public EditorSlopeWrapper getPolygonAt(DecimalPosition position) {
        Collection<EditorSlopeWrapper> inner = innerTiles.get(position.divide(rasterSize).toIndexFloor());
        if (inner != null && !inner.isEmpty()) {
            if (inner.size() == 1) {
                return CollectionUtils.getFirst(inner);
            } else {
                // 1 find parent
                for (EditorSlopeWrapper modifiedSlope : inner) {
                    if (!modifiedSlope.isParent()) {
                        return modifiedSlope;
                    }
                }
                logger.severe("ModifiedSlopeContainer.getPolygonAt() can not find parent: " + position);
            }
        }
        return null;
    }

    public EditorSlopeWrapper getPolygonAt(Polygon2D area) {
        for (DecimalPosition position : area.getCorners()) {
            Collection<EditorSlopeWrapper> pierced = piercedTiles.get(position.divide(rasterSize).toIndexFloor());
            if (pierced != null && !pierced.isEmpty()) {
                if (pierced.size() == 1) {
//                    if (CollectionUtils.getFirst(pierced).getPolygon().isInside(position)) {
//                        return CollectionUtils.getFirst(pierced);
//                    }
                    return CollectionUtils.getFirst(pierced);
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
