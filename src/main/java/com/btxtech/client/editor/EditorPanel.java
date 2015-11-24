package com.btxtech.client.editor;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import elemental.client.Browser;
import elemental.svg.SVGSVGElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

/**
 * Created by Beat
 * 15.11.2015.
 */
@Templated("EditorPanel.html#editorPanel")
public class EditorPanel extends Composite {
    // private Logger logger = Logger.getLogger(EditorDialogBox.class.getName());
    @Inject
    @DataField
    private Button closeButton;
    @DataField
    private Element svgElement = (Element) Browser.getDocument().createSVGElement();
    @Inject
    private Instance<SlopeEditor> editorInstance;

    public void open() {
        initEditor();
    }

    @EventHandler("closeButton")
    private void closeButtonClick(ClickEvent event) {
        getElement().setPropertyBoolean("open", false);
    }

    private void initEditor() {
        SlopeEditor slopeEditor = editorInstance.get();
        slopeEditor.start(getSVGElement());
    }

    public SVGSVGElement getSVGElement() {
        return (SVGSVGElement) svgElement;
    }
}
