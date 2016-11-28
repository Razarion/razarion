package com.btxtech.scenariongui.scenario;

import com.btxtech.ExtendedGraphicsContext;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Rectangle2D;
import javafx.scene.paint.Color;

/**
 * Created by Beat
 * 26.11.2016.
 */
public class FillCircleScenario extends Scenario {
    private DecimalPosition position = new DecimalPosition(0, 0);


    @Override
    public void render(ExtendedGraphicsContext extendedGraphicsContext) {
        // fillCircle(new DecimalPosition(position), 50, 8, extendedGraphicsContext);

        // Interesting cases
        // fillCircle(new DecimalPosition(0, 0), 13, 10, extendedGraphicsContext);
        // fillCircle(new DecimalPosition(5, 5), 7, 10, extendedGraphicsContext);

        // ---
        // fillCircle(new DecimalPosition(60.166666666666664, 43.666666666666664), 5, 8, extendedGraphicsContext);
        fillCircle(new DecimalPosition(131.7785979445439, 318.1544206736698), 5, 8, extendedGraphicsContext);
    }

    @Override
    public boolean onMouseMove(DecimalPosition position) {
        this.position = position;
        return true;
    }

    private void fillCircle(DecimalPosition center, double radius, double blockLength, ExtendedGraphicsContext extendedGraphicsContext) {
        System.out.println("-------------------------------------------------------------");


        extendedGraphicsContext.getGc().setLineWidth(0.1);
        extendedGraphicsContext.getGc().setStroke(new Color(0, 1, 0, 1));
        extendedGraphicsContext.getGc().strokeOval(center.getX() - radius, center.getY() - radius, 2 * radius, 2 * radius);


        double startX = Math.floor((center.getX() - radius) / blockLength) * blockLength;
        double startY = Math.floor((center.getY() - radius) / blockLength) * blockLength;
        double endX = Math.ceil((center.getX() + radius) / blockLength) * blockLength;
        double endY = Math.ceil((center.getY() + radius) / blockLength) * blockLength;

        int countX = (int) ((endX - startX) / blockLength);
        int countY = (int) ((endY - startY) / blockLength);

        System.out.println("countX: " + countX);
        System.out.println("countY: " + countY);

        for (double x = startX; x < endX; x += blockLength) {
            for (double y = startY; y < endY; y += blockLength) {
                Rectangle2D rect = new Rectangle2D(x, y, blockLength, blockLength);
                if(rect.contains(center)) {
                    putPixel(x, y, extendedGraphicsContext, blockLength);
                } else {
                    DecimalPosition projection = rect.getNearestPoint(center);
                    if (projection.getDistance(center) <= radius) {
                        putPixel(x, y, extendedGraphicsContext, blockLength);
                    }
                }
            }
        }


//        // putPixel(center.getX() - offsetX, center.getY() - offsetY, extendedGraphicsContext, blockLength);
//        double halfEdge = Math.ceil(radius / blockLength) * blockLength;
//        for (double relativeX = blockLength - halfEdge - offsetX; relativeX < halfEdge; relativeX += blockLength) {
//            for (double relativeY = blockLength - halfEdge - offsetY; relativeY < halfEdge; relativeY += blockLength) {
////                double correctedRelativeX = relativeX;
////                if (relativeX < 0) {
////                    correctedRelativeX += blockLength;
////                }
////                double correctedRelativeY = relativeY;
////                if (relativeY < 0) {
////                    correctedRelativeY += blockLength;
////                }
////                double distance = center.getDistance(new DecimalPosition(correctedRelativeX + center.getX(), correctedRelativeY + center.getY()));
////                if (distance <= radius) {
//                putPixel(relativeX + center.getX(), relativeY + center.getY(), extendedGraphicsContext, blockLength);
////                }
//            }
//        }


        // Works-----------------------------------------------------------------------------------
//        double halfEdge = Math.ceil(radius / blockLength) * blockLength;
//        for (double relativeX = -halfEdge; relativeX < halfEdge; relativeX += blockLength) {
//            for (double relativeY = -halfEdge; relativeY < halfEdge; relativeY += blockLength) {
//                double correctedRelativeX = relativeX;
//                if (relativeX < 0) {
//                    correctedRelativeX += blockLength;
//                }
//                double correctedRelativeY = relativeY;
//                if (relativeY < 0) {
//                    correctedRelativeY += blockLength;
//                }
//                double distance = center.getDistance(new DecimalPosition(correctedRelativeX + center.getX(), correctedRelativeY + center.getY()));
//                if (distance <= radius) {
//                    putPixel(relativeX + center.getX(), relativeY + center.getY(), extendedGraphicsContext, blockLength);
//                }
//            }
//        }


        // old from web-----------------------------------------------------------------------------------
        // Fill square inside the circle.
//        double halfEdge = radius * Math.sin(MathHelper.EIGHTH_RADIANT);
//        halfEdge = Math.ceil(halfEdge / blockLength) * blockLength;
//        for (double x = center.getX() - halfEdge + blockLength; x < center.getX() + halfEdge; x += blockLength) {
//            for (double y = center.getY() - halfEdge + blockLength; y < center.getY() + halfEdge; y += blockLength) {
//                putPixel(x, y, extendedGraphicsContext, blockLength);
//            }
//        }
//
//        // This here is sin(45) but i just hard-coded it.
//        double sinus = 0.70710678118;
//
//        // Fill remaining of the circle
//        // This is the distance on the axis from sin(90) to sin(45).
//        double range = radius / (2.0 * sinus);
//        for (double i = radius; i >= range; i -= blockLength) {
//            double j = Math.sqrt(radius * radius - i * i);
//            for (double k = -j; k <= j; k += blockLength) {
//                //We draw all the 4 sides at the same time.
//                putPixel(center.getX() - k, center.getY() + i, extendedGraphicsContext, blockLength);
////                putPixel(center.getX() - k, center.getY() - i, extendedGraphicsContext, blockLength);
////                putPixel(center.getX() + i, center.getY() + k, extendedGraphicsContext, blockLength);
////                putPixel(center.getX() - i, center.getY() - k, extendedGraphicsContext, blockLength);
//            }
//        }

    }

    private void putPixel(double x, double y, ExtendedGraphicsContext extendedGraphicsContext, double blockLength) {
        // System.out.println("drawPoint x:" + y + " y:" + y + " width:" + width + " height:" + height);
        extendedGraphicsContext.getGc().setFill(new Color(0, 0, 0, 0.3));
        extendedGraphicsContext.getGc().fillRect(x, y, blockLength, blockLength);
        extendedGraphicsContext.getGc().setStroke(new Color(1, 0, 0, 1));
        extendedGraphicsContext.getGc().setLineWidth(0.1);
        extendedGraphicsContext.getGc().strokeRect(x, y, blockLength, blockLength);
    }

}
