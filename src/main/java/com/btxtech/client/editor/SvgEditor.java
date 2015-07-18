package com.btxtech.client.editor;

import com.btxtech.game.jsre.client.common.Index;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import org.vectomatic.dom.svg.OMSVGDocument;
import org.vectomatic.dom.svg.OMSVGGElement;
import org.vectomatic.dom.svg.OMSVGLength;
import org.vectomatic.dom.svg.OMSVGPoint;
import org.vectomatic.dom.svg.OMSVGSVGElement;
import org.vectomatic.dom.svg.OMSVGTransform;
import org.vectomatic.dom.svg.utils.OMSVGParser;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 12.05.2015.
 */
public abstract class SvgEditor extends DialogBox {
    private List<EditorCorner> editorCorners = new ArrayList<>();
    private OMSVGDocument doc;
    private OMSVGSVGElement svg;
    private OMSVGGElement group;
    private boolean deleteMode;
    private SvgWidget svgWidget;
    private Logger logger = Logger.getLogger(SvgEditor.class.getName());

    public static class SvgWidget extends FocusWidget {
        public SvgWidget(OMSVGSVGElement e) {
            setElement(e.getElement());
        }
    }

    protected SvgEditor(float width, float height, boolean centerXNegateY) {
        setAutoHideEnabled(true);
        setText("Editor");

        VerticalPanel verticalPanel = new VerticalPanel();
        verticalPanel.add(new Button("Dump", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                logger.severe("------------- DUMP ------------");
                onDump(setupIndexes(), logger);
                logger.severe("---------- DUMP ENDS ----------");
            }
        }));
        verticalPanel.add(setupSvg(width, height, centerXNegateY));
        setWidget(verticalPanel);
    }

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
            logger.log(Level.SEVERE, "Editor.onMouseDown()", t);
        }
    }

    public void removeCorner(EditorCorner editorCorner) {
        editorCorners.remove(editorCorner);
        onChanged();
    }

    public OMSVGDocument getDoc() {
        return doc;
    }

    public OMSVGGElement getGroup() {
        return group;
    }

    public OMSVGSVGElement getSvg() {
        return svg;
    }

    public Index convertMouseToSvg(MouseEvent event) {
        OMSVGPoint point = svg.createSVGPoint(event.getRelativeX(svg.getElement()), event.getRelativeY(svg.getElement()));
        OMSVGPoint convertedPoint = point.matrixTransform(group.getCTM().inverse());
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

    private Widget setupSvg(float width, float height, boolean centerXNegateY) {
        doc = OMSVGParser.currentDocument();
        svg = doc.createSVGSVGElement();
        setupGrid();

        group = doc.createSVGGElement();
        OMSVGTransform omsvgTransform = svg.createSVGTransform();
        if (centerXNegateY) {
            omsvgTransform.setTranslate(width / 2, height);
            group.getTransform().getBaseVal().appendItem(omsvgTransform);
        } else {
            omsvgTransform.setTranslate(0, height);
            group.getTransform().getBaseVal().appendItem(omsvgTransform);
        }
        omsvgTransform = svg.createSVGTransform();
        omsvgTransform.setScale(1, -1);
        group.getTransform().getBaseVal().appendItem(omsvgTransform);
        svg.appendChild(group);

        for (Index index : getIndexes()) {
            addEditorCorner(index);
        }

        svg.addMouseDownHandler(new MouseDownHandler() {
            @Override
            public void onMouseDown(MouseDownEvent event) {
                addEditorCorner(convertMouseToSvg(event));
                onChanged();
            }
        });

        svg.setWidth(OMSVGLength.SVG_LENGTHTYPE_PX, width);
        svg.setHeight(OMSVGLength.SVG_LENGTHTYPE_PX, height);


        svgWidget = new SvgWidget(svg);
        svgWidget.addKeyDownHandler(new KeyDownHandler() {
            @Override
            public void onKeyDown(KeyDownEvent event) {
                if (event.getNativeKeyCode() == KeyCodes.KEY_DELETE) {
                    onDeleteModeChanged(true);
                }
            }
        });
        svgWidget.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                if (event.getNativeKeyCode() == KeyCodes.KEY_DELETE) {
                    onDeleteModeChanged(false);
                }
            }
        });

        return svgWidget;
    }

    private void onDeleteModeChanged(boolean mode) {
        if (deleteMode == mode) {
            return;
        }
        deleteMode = mode;
        if (mode) {
            svg.getStyle().setCursor(Style.Cursor.CROSSHAIR);
        } else {
            svg.getStyle().setCursor(Style.Cursor.MOVE);
        }
        for (EditorCorner editorCorner : editorCorners) {
            editorCorner.setDeleteMode(mode);
        }
    }

    public void setFocus() {
        svgWidget.setFocus(true);
    }

    public boolean isDeleteMode() {
        return deleteMode;
    }
}
