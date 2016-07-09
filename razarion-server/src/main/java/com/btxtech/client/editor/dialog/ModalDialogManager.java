package com.btxtech.client.editor.dialog;

import com.google.gwt.user.client.ui.RootPanel;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by Beat
 * 20.05.2016.
 */
@Singleton
public class ModalDialogManager {
    @Inject
    private Instance<ModalDialogContent> contentInstance;
    @Inject
    private Instance<BootstrapModalDialog> containerInstance;
    private BootstrapModalDialog container;
    private ModalDialogContent content;
    private ApplyListener applyListener;
    private Object applyValue;

    public <T> void show(String title, Class<? extends ModalDialogContent<T>> contentClass, T t, ApplyListener<T> applyListener) {
        applyValue = null;
        this.applyListener = applyListener;
        if (content != null || container != null) {
            return;
        }
        ModalDialogContent<T> content = contentInstance.select(contentClass).get();
        content.init(t);
        this.content = content;
        container = containerInstance.get();
        container.init(title, content);
        content.customize(this);
        RootPanel.get().add(container);

    }

    public void hide() {
        if (container != null) {
            RootPanel.get().remove(container);
            container = null;
            content = null;
            applyListener = null;
            applyValue = null;
        }
    }

    public void cancel() {
        hide();
    }

    public void setApplyValue(Object applyValue) {
        this.applyValue = applyValue;
    }

    public void apply() {
        if (applyListener != null) {
            applyListener.onApply(applyValue);
        }
        hide();
    }

    public BootstrapModalDialog getContainer() {
        return container;
    }
}
