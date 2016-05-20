package com.btxtech.client.dialog;

import com.google.gwt.user.client.ui.RootPanel;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by Beat
 * 20.05.2016.
 */
@Singleton
public class ModalDialog {
    @Inject
    private Instance<ModalDialogContent> contentInstance;
    @Inject
    private Instance<BootstrapModalDialog> containerInstance;
    private BootstrapModalDialog container;
    private ModalDialogContent content;
    private Runnable applyListener;

    public <T> void show(String title, Class<? extends ModalDialogContent<T>> contentClass, T t, Runnable applyListener) {
        this.applyListener = applyListener;
        if (content != null || container != null) {
            return;
        }
        ModalDialogContent<T> content = contentInstance.select(contentClass).get();
        content.init(t);
        this.content = content;
        container = containerInstance.get();
        container.init(title, content);
        RootPanel.get().add(container);

    }

    public void hide() {
        if (container != null) {
            RootPanel.get().remove(container);
            container = null;
            content = null;
            applyListener = null;
        }
    }

    public void cancel() {
        hide();
    }

    public void apply() {
        if (applyListener != null) {
            applyListener.run();
        }
        hide();
    }
}
