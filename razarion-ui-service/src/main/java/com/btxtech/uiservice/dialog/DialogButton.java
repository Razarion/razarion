package com.btxtech.uiservice.dialog;

import com.btxtech.uiservice.i18n.I18nHelper;

/**
 * Created by Beat
 * 12.12.2016.
 */
public interface DialogButton {
    enum Button {
        OK {
            @Override
            public String getText() {
                return I18nHelper.getConstants().ok();
            }
        },
        CANCEL {
            @Override
            public String getText() {
                return I18nHelper.getConstants().cancel();
            }
        },
        APPLY {
            @Override
            public String getText() {
                return I18nHelper.getConstants().apply();
            }
        },
        CLOSE {
            @Override
            public String getText() {
                return I18nHelper.getConstants().close();
            }
        };

        public abstract String getText();
    }

    interface Listener<T> {
        void onPressed(Button button, T t);
    }
}
