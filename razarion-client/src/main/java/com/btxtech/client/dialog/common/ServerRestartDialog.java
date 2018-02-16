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
        lifecycleService.setServerRestartCallback(serverState -> {
            switch (serverState) {
                case RESTARTING:
                    stateSpan.setTextContent(I18nHelper.getConstants().restarting());
                    break;
                case STARTING_PLANET:
                    stateSpan.setTextContent(I18nHelper.getConstants().startingPlanet());
                    break;
                case RUNNING:
                    stateSpan.setTextContent(I18nHelper.getConstants().running());
                    break;
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
