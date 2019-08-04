package com.btxtech.shared.gameengine.planet.gui.userobject;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Line;
import com.btxtech.shared.datatypes.Line2I;
import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.TextureCoordinate;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.TerrainSlopeCorner;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainType;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * Created by Beat
 * 17.05.2016.
 */
public class InstanceStringGenerator {
    private static final String NULL_STRING = "null";

    public static String generate(Index index) {
        if (index != null) {
            return "new Index(" + index.getX() + ", " + index.getY() + ")";
        } else {
            return NULL_STRING;
        }
    }

    public static String generateIndexList(List<Index> indexList) {
        StringBuilder builder = new StringBuilder();
        builder.append("List<Index> positions = Arrays.asList(");
        for (int i = 0; i < indexList.size(); i++) {
            Index index = indexList.get(i);
            builder.append(generate(index));
            if (i < indexList.size() - 1) {
                builder.append(", ");
            }
        }
        builder.append(");");
        return builder.toString();
    }

    public static String generateSimpleDecimalPositionList(List<DecimalPosition> positions) {
        StringBuilder builder = new StringBuilder();
        builder.append("Arrays.asList(");
        for (int i = 0; i < positions.size(); i++) {
            DecimalPosition decimalPosition = positions.get(i);
            builder.append(generate(decimalPosition));
            if (i < positions.size() - 1) {
                builder.append(", ");
            }
        }
        builder.append(")");
        return builder.toString();
    }

    public static String generateSlope(List<DecimalPosition> positions) {
        StringBuilder builder = new StringBuilder();
        builder.append("Arrays.asList(");
        for (int i = 0; i < positions.size(); i++) {
            builder.append(generate(new TerrainSlopeCorner().setPosition(positions.get(i))));
            if (i < positions.size() - 1) {
                builder.append(", ");
            }
        }
        builder.append(")");
        return builder.toString();
    }

    public static String generateDecimalPositionList(List<DecimalPosition> positions) {
        return ("List<DecimalPosition> positions = " + generateSimpleDecimalPositionList(positions)) + ");";
    }

    public static String generate(DecimalPosition decimalPosition) {
        if (decimalPosition != null) {
            return "new DecimalPosition(" + String.format(Locale.US, "%.3f, %.3f", decimalPosition.getX(), decimalPosition.getY()) + ")";
        } else {
            return NULL_STRING;
        }
    }

    public static String generate(Line line) {
        if (line != null) {
            return "new Line(" + generate(line.getPoint1()) + ", " + generate(line.getPoint2()) + ")";
        } else {
            return NULL_STRING;
        }
    }

    public static String generate(Line2I line) {
        if (line != null) {
            return "new Line2I(" + generate(line.getPoint1()) + ", " + generate(line.getPoint2()) + ")";
        } else {
            return NULL_STRING;
        }
    }

    public static String generate(Vertex vertex) {
        if (vertex != null) {
            return "new Vertex(" + String.format(Locale.US, "%.4f, %.4f, %.4f", vertex.getX(), vertex.getY(), vertex.getZ()) + ")";
        } else {
            return NULL_STRING;
        }
    }

    public static String generate(TerrainType startTerrainType) {
        if (startTerrainType != null) {
            return "TerrainType." + startTerrainType.name();
        } else {
            return NULL_STRING;
        }
    }

    public static String generate(TerrainSlopeCorner terrainSlopeCorner) {
        if (terrainSlopeCorner != null) {
            return String.format(Locale.US,"GameTestHelper.createTerrainSlopeCorner(%.4f, %.4f, null)", terrainSlopeCorner.getPosition().getX(), terrainSlopeCorner.getPosition().getY());
        } else {
            return NULL_STRING;
        }
    }

    public static String toVertexDoubleString(List<Vertex> vertices) {
        String s = "new double[]{";
        for (Iterator<Vertex> iterator = vertices.iterator(); iterator.hasNext(); ) {
            s += toSimpleString(iterator.next());
            if (iterator.hasNext()) {
                s += ", ";
            }
        }
        s += "}";
        return s;
    }

    public static String toSimpleString(Vertex vertex) {
        return String.format(Locale.US, "%.4f, %.4f, %.4f", vertex.getX(), vertex.getY(), vertex.getZ());
    }

    public static String toSimpleString(TextureCoordinate textureCoordinate) {
        return String.format("%.4f, %.4f", textureCoordinate.getS(), textureCoordinate.getT());
    }

    public static String toTextureCoordinateDoubleString(List<TextureCoordinate> textureCoordinates) {
        String s = "new double[]{";
        for (Iterator<TextureCoordinate> iterator = textureCoordinates.iterator(); iterator.hasNext(); ) {
            s += toSimpleString(iterator.next());
            if (iterator.hasNext()) {
                s += ", ";
            }
        }
        s += "}";
        return s;
    }

    public static String generate(Matrix4 matrix4) {
        if (matrix4 != null) {
            String s = "new double[][]{";
            for (int y = 0; y < Matrix4.ROWS; y++) {
                s += "{";
                for (int x = 0; x < Matrix4.COLUMNS; x++) {
                    s += String.format("%.4f", matrix4.numberAt(x, y));
                    if (x + 1 < Matrix4.COLUMNS) {
                        s += ", ";
                    }
                }
                s += "}";
                if (y + 1 < Matrix4.ROWS) {
                    s += ", ";
                }

            }
            s += "}";
            return "new Matrix4(" + s + ")";
        } else {
            return NULL_STRING;
        }
    }
}
