package com.btxtech.client.editor.widgets;

import com.google.gwt.event.dom.client.ChangeEvent;
import elemental2.dom.FileList;
import elemental2.dom.HTMLButtonElement;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLInputElement;
import org.jboss.errai.common.client.api.elemental2.IsElement;
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
    private HTMLButtonElement fileButton;
    @Inject
    @DataField
    private HTMLInputElement fileButtonInput;
    @Inject
    @DataField
    private HTMLDivElement fileButtonDiv;
    private Consumer<FileList> fileCallback;

    public void init(String text, Consumer<FileList> fileCallback) {
        this.fileCallback = fileCallback;
        fileButtonDiv.textContent = text;
    }

    @Override
    public HTMLElement getElement() {
        return fileButton;
    }

    @EventHandler("fileButtonInput")
    public void fileButtonInputChanged(ChangeEvent e) {
        if (fileButtonInput.files.length > 0) {
            fileCallback.accept(fileButtonInput.files);
        }
        fileButtonInput.value = null; // Prevent caching
    }

}
