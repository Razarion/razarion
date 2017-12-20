package com.btxtech.client.editor.widgets;

import com.google.gwt.event.dom.client.ChangeEvent;
import elemental.html.FileList;
import elemental.html.InputElement;
import org.jboss.errai.common.client.api.IsElement;
import org.jboss.errai.common.client.dom.Button;
import org.jboss.errai.common.client.dom.HTMLElement;
import org.jboss.errai.common.client.dom.Input;
import org.jboss.errai.common.client.dom.Span;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;
import java.util.function.Consumer;

/**
 * Created by Beat
 * on 20.12.2017.
 */
@Templated("FileButton.html#fileButton")
public class FileButton implements IsElement {
    // private Logger logger = Logger.getLogger(FileButton.class.getName());
    @Inject
    @DataField
    private Button fileButton;
    @Inject
    @DataField
    private Input fileButtonInput;
    @Inject
    @DataField
    private Span fileButtonSpan;
    private Consumer<FileList> fileCallback;

    public void init(String text, Consumer<FileList> fileCallback) {
        this.fileCallback = fileCallback;
        fileButtonSpan.setInnerHTML(text);
    }

    @Override
    public HTMLElement getElement() {
        return fileButton;
    }

    @EventHandler("fileButtonInput")
    public void fileButtonInputChanged(ChangeEvent e) {
        InputElement fileSelector = (InputElement) e.getNativeEvent().getEventTarget();
        if (fileSelector.getSize() > 0) {
            fileCallback.accept(fileSelector.getFiles());
        }
        fileButtonInput.setValue(null); // Prevent caching
    }

}
