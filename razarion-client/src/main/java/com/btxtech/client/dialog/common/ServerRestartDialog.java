package com.btxtech.client.dialog.common;

import com.btxtech.client.dialog.framework.ModalDialogContent;
import com.btxtech.client.dialog.framework.ModalDialogPanel;
import com.btxtech.client.system.LifecycleService;
import com.btxtech.uiservice.i18n.I18nHelper;
import com.google.gwt.user.client.ui.Composite;
import org.jboss.errai.common.client.dom.Span;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;

/**
 * Created by Beat
 * 30.10.2016.
 */
@Templated("ServerRestartDialog.html#serverRestartDialog")
public class ServerRestartDialog extends Composite implements ModalDialogContent<Void> {
    @Inject
    private LifecycleService lifecycleService;
    @Inject
    @DataField
    private Span stateSpan;

    @Override
    public void init(Void ignore) {
        stateSpan.setTextContent(I18nHelper.getConstants().serverShuttingDown());
        lifecycleService.setServerRestartCallback(serverState -> {
            switch (serverState) {
                case SHUTTING_DOWN:
                    stateSpan.setTextContent(I18nHelper.getConstants().serverShuttingDown());
                    break;
                case STARTING:
                    stateSpan.setTextContent(I18nHelper.getConstants().serverStarting());
                    break;
                case RUNNING:
                    stateSpan.setTextContent(I18nHelper.getConstants().serverRunning());
                    break;
                    default:
                        stateSpan.setTextContent(I18nHelper.getConstants().unknown());
            }
        });
    }

    @Override
    public void onClose() {

    }

    @Override
    public void customize(ModalDialogPanel<Void> modalDialogPanel) {

    }
}
