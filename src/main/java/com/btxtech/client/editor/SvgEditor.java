package com.btxtech.client.editor;

import com.btxtech.game.jsre.client.common.Index;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import elemental.client.Browser;
import elemental.events.MouseEvent;
import elemental.svg.SVGGElement;
import elemental.svg.SVGPoint;
import elemental.svg.SVGSVGElement;
import elemental.svg.SVGTransform;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 12.05.2015.
 */
public abstract class SvgEditor {
    private SVGSVGElement svg;
    private List<EditorCorner> editorCorners = new ArrayList<>();
    private SVGGElement group;
    private boolean deleteMode;
    private Logger logger = Logger.getLogger(SvgEditor.class.getName());

    protected void init(Element svgElement, float width, float height, boolean centerXNegateY) {
        this.svg = (SVGSVGElement) svgElement;
        // TODO setupGrid();

        group = Browser.getDocument().createSVGGElement();
        SVGTransform transform = svg.createSVGTransform();
        if (centerXNegateY) {
            transform.setTranslate(width / 2, height);
            group.getAnimatedTransform().getBaseVal().appendItem(transform);
        } else {
            transform.setTranslate(0, height);
            group.getAnimatedTransform().getBaseVal().appendItem(transform);
        }
        transform = svg.createSVGTransform();
        transform.setScale(1, -1);
        group.getAnimatedTransform().getBaseVal().appendItem(transform);
        svg.appendChild(group);

        for (Index index : getIndexes()) {
            addEditorCorner(index);
        }

//        svg.addEventListener("click", new EventListener() {
//            @Override
//            public void handleEvent(Event event) {
//                logger.severe("event: " + event);
//                addEditorCorner(convertMouseToSvg((MouseEvent) event));
//                onChanged();
//            }
//        }, true);

        // svg.getAnimatedWidth().getBaseVal().setValue(width);
        // svg.getAnimatedHeight().getBaseVal().setValue(height);

//        svg.addEventListener(Document.Events.KEYBOARD, new EventListener() {
//            @Override
//            public void handleEvent(Event event) {
//                KeyboardEvent keyboardEvent = (KeyboardEvent) event;
//                keyboardEvent.getKeyCode() ==
//
//
//                if (event.getNativeKeyCode() == KeyCodes.KEY_DELETE) {
//                    onDeleteModeChanged(true);
//                }
//            }
//        });
//
//        svgWidget.addKeyUpHandler(new KeyUpHandler() {
//            @Override
//            public void onKeyUp(KeyUpEvent event) {
//                if (event.getNativeKeyCode() == KeyCodes.KEY_DELETE) {
//                    onDeleteModeChanged(false);
//                }
//            }
//        });
    }


//public static class SvgWidget extends FocusWidget {
//    public SvgWidget(OMSVGSVGElement e) {
//        setElement(e.getElement());
//    }
//
//}

    protected void onDump(List<Index> indexes, Logger logger) {
        logger.severe("Dump not overridden");
    }

    abstract protected List<Index> getIndexes();

    abstract protected void setIndexes(List<Index> indexes);

    protected void setupGrid() {

    }

    private void addEditorCorner(Index position) {
        try {
            EditorCorner predecessor = null;
            if (!editorCorners.isEmpty()) {
                predecessor = editorCorners.get(editorCorners.size() - 1);
            }
            editorCorners.add(new EditorCorner(position, predecessor, this));
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "Editor.addEditorCorner()", t);
        }
    }

    public void removeCorner(EditorCorner editorCorner) {
        editorCorners.remove(editorCorner);
        onChanged();
    }

    public SVGGElement getGroup() {
        return group;
    }

    public Index convertMouseToSvg(MouseEvent event) {
        SVGPoint point = svg.createSVGPoint();
        point.setX(event.getOffsetX());
        point.setY(event.getOffsetY());
        SVGPoint convertedPoint = point.matrixTransform(group.getCTM().inverse());
        return new Index((int) convertedPoint.getX(), (int) convertedPoint.getY());
    }

    public void onChanged() {
        setIndexes(setupIndexes());
    }

    private List<Index> setupIndexes() {
        List<Index> indexes = new ArrayList<>();
        for (EditorCorner editorCorner : editorCorners) {
            indexes.add(editorCorner.getPosition());
        }
        return indexes;
    }

    private void onDeleteModeChanged(boolean mode) {
        if (deleteMode == mode) {
            return;
        }
        deleteMode = mode;
        if (mode) {
            svg.getStyle().setCursor(Style.Cursor.CROSSHAIR.getCssName());
        } else {
            svg.getStyle().setCursor(Style.Cursor.MOVE.getCssName());
        }
        for (EditorCorner editorCorner : editorCorners) {
            editorCorner.setDeleteMode(mode);
        }
    }

    public boolean isDeleteMode() {
        return deleteMode;
    }
}
