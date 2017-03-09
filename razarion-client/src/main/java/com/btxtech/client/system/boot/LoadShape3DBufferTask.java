package com.btxtech.client.system.boot;

import com.btxtech.client.editor.terrain.TerrainEditorImpl;
import com.btxtech.client.shape3d.ClientShape3DUiService;
import com.btxtech.shared.dto.FacebookUserLoginInfo;
import com.btxtech.shared.dto.GameUiControlConfig;
import com.btxtech.shared.rest.GameUiControlProvider;
import com.btxtech.uiservice.control.GameUiControl;
import com.btxtech.uiservice.system.boot.AbstractStartupTask;
import com.btxtech.uiservice.system.boot.DeferredStartup;
import com.btxtech.uiservice.user.UserUiService;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 07.03.2016.
 */
@Dependent
public class LoadShape3DBufferTask extends AbstractStartupTask {
    @Inject
    private ClientShape3DUiService clientShape3DUiService;

    @Override
    protected void privateStart(final DeferredStartup deferredStartup) {
        deferredStartup.setDeferred();
        deferredStartup.setBackground();
        clientShape3DUiService.loadBuffer(deferredStartup);
    }
}
