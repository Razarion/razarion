package com.btxtech.scenariongui.scenario;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.SlopeNode;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.model.SyncPhysicalArea;
import com.btxtech.shared.gameengine.planet.model.SyncPhysicalMovable;
import com.btxtech.shared.gameengine.planet.terrain.TerrainService;
import com.btxtech.shared.gameengine.planet.terrain.TerrainSlopeTileContext;
import com.btxtech.shared.gameengine.planet.terrain.TerrainTileContext;
import com.btxtech.shared.system.JsInteropObjectFactory;
import com.btxtech.webglemulator.razarion.DevToolTerrainSlopeTile;
import com.btxtech.webglemulator.razarion.DevToolTerrainTile;

import javax.enterprise.inject.Instance;
import javax.enterprise.util.TypeLiteral;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.function.Supplier;

import static org.easymock.EasyMock.*;

/**
 * Created by Beat
 * 05.04.2017.
 */
public class FrameworkHelper {

    public static void mockJsInteropObjectFactory(Object object) {
        JsInteropObjectFactory mockJsInteropObjectFactory = createNiceMock(JsInteropObjectFactory.class);
        expect(mockJsInteropObjectFactory.generateTerrainTile()).andReturn(new DevToolTerrainTile());
        expect(mockJsInteropObjectFactory.generateTerrainSlopeTile()).andReturn(new DevToolTerrainSlopeTile());
        injectJsInteropObjectFactory("jsInteropObjectFactory", object, mockJsInteropObjectFactory);
        replay(mockJsInteropObjectFactory);
    }

    public static void injectTerrainTileContextInstance(TerrainService terrainService) {
        injectInstance("terrainTileContextInstance", terrainService, () -> {
            TerrainTileContext terrainTileContext = new TerrainTileContext();
            mockJsInteropObjectFactory(terrainTileContext);
            injectInstance("terrainSlopeTileContextInstance", terrainTileContext, () -> {
                TerrainSlopeTileContext terrainSlopeTileContext = new TerrainSlopeTileContext();
                mockJsInteropObjectFactory(terrainSlopeTileContext);
                return terrainSlopeTileContext;
            });
            return terrainTileContext;
        });
    }

    public static void injectInstance(String fieldName, Object object, Supplier getSupplier) {
        Instance instance = new Instance() {
            @Override
            public Instance select(Annotation... qualifiers) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Instance select(Class subtype, Annotation... qualifiers) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Instance select(TypeLiteral subtype, Annotation... qualifiers) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean isUnsatisfied() {
                return false;
            }

            @Override
            public boolean isAmbiguous() {
                return false;
            }

            @Override
            public void destroy(Object instance) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Iterator iterator() {
                throw new UnsupportedOperationException();
            }

            @Override
            public Object get() {
                return getSupplier.get();
            }
        };

        try {
            Field field = object.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(object, instance);
            field.setAccessible(false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void injectJsInteropObjectFactory(String fieldName, Object service, JsInteropObjectFactory jsInteropObjectFactory) {
        try {
            Field field = service.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(service, jsInteropObjectFactory);
            field.setAccessible(false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void injectService(String fieldName, Object service, Class clazz, Object serviceToInject) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(service, serviceToInject);
            field.setAccessible(false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void injectService(String fieldName, Object service, Object serviceToInject) {
        injectService(fieldName, service, service.getClass(), serviceToInject);
    }

    public static Object readField(String fieldName, Object bean) {
        try {
            Field field = bean.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            Object object = field.get(bean);
            field.setAccessible(false);
            return object;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static double[][] toColumnRow(double[][] rowColumn) {
        int xCount = rowColumn[0].length;
        int yCount = rowColumn.length;
        double[][] columnRow = new double[xCount][yCount];
        for (int x = 0; x < xCount; x++) {
            for (int y = 0; y < yCount; y++) {
                columnRow[x][y] = rowColumn[y][x];
            }
        }
        return columnRow;
    }

    public static SlopeNode[][] toColumnRow(SlopeNode[][] rowColumn) {
        int xCount = rowColumn[0].length;
        int yCount = rowColumn.length;
        SlopeNode[][] columnRow = new SlopeNode[xCount][yCount];
        for (int x = 0; x < xCount; x++) {
            for (int y = 0; y < yCount; y++) {
                columnRow[x][y] = rowColumn[y][x];
            }
        }
        return columnRow;
    }


    public static SlopeNode createSlopeNode(double x, double z, double slopeFactor) {
        return new SlopeNode().setPosition(new Vertex(x, 0, z)).setSlopeFactor(slopeFactor);
    }

    public static SyncPhysicalMovable createSyncPhysicalMovable(DecimalPosition position, double radius) {
        SyncPhysicalMovable syncPhysicalMovable = new SyncPhysicalMovable();
        injectService("position2d", syncPhysicalMovable, SyncPhysicalArea.class, position);
        injectService("radius", syncPhysicalMovable, SyncPhysicalArea.class, radius);
        return syncPhysicalMovable;
    }

}


