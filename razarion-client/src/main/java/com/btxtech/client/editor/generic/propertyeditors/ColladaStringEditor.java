package com.btxtech.client.editor.generic.propertyeditors;

import com.btxtech.client.editor.widgets.FileButton;
import com.btxtech.client.utils.ControlUtils;
import com.btxtech.common.DisplayUtils;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import elemental2.dom.File;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.Date;

@Templated("ColladaStringEditor.html#colladaStringPanel")
public class ColladaStringEditor extends AbstractPropertyEditor<String> {
    // private Logger logger = Logger.getLogger(ImageEditor.class.getName());
    @Inject
    @DataField
    private FileButton selectFileButton;
    @Inject
    @DataField
    private Button reloadFileButton;
    @Inject
    @DataField
    private Label loadedTimestamp;
    @Inject
    @DataField
    private Label fileTimestamp;
    @Inject
    @DataField
    private HTMLDivElement colladaStringPanel;
    private File file;

    @PostConstruct
    public void postConstruct() {
        selectFileButton.init("Select", fileList -> ControlUtils.readFirstAsText(fileList, (colladaText, file) -> {
            setPropertyValue(colladaText);
            this.file = file;
            reloadFileButton.setEnabled(true);
            loadedTimestamp.setText(DisplayUtils.formatDate(new Date()));
            fileTimestamp.setText(file.lastModifiedDate.toDateString());
        }));
    }

    @Override
    public void showValue() {
    }

    @EventHandler("reloadFileButton")
    private void reloadFileButtonClick(ClickEvent event) {
        if (file != null) {
            ControlUtils.readFileText(file, colladaText -> {
                setPropertyValue(colladaText);
                loadedTimestamp.setText(DisplayUtils.formatDate(new Date()));
                fileTimestamp.setText(file.lastModifiedDate.toDateString());
            });
        }
    }

    @Override
    public HTMLElement getElement() {
        return colladaStringPanel;
    }
}
