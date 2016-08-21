package com.btxtech.client.dialog;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * Created by Beat
 * 05.05.2016.
 */
public interface ModalDialogContent<T> extends IsWidget {
    void onClose();

    void init(T t);

    void customize(ModalDialogPanel<T> modalDialogPanel);
}
