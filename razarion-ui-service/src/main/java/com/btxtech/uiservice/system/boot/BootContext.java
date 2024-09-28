package com.btxtech.uiservice.system.boot;

import com.btxtech.client.Caller;
import com.btxtech.shared.datatypes.asset.MeshContainer;
import com.btxtech.shared.datatypes.shape.ParticleSystemConfig;
import com.btxtech.shared.datatypes.shape.ThreeJsModelConfig;
import com.btxtech.shared.dto.ColdGameUiContext;
import com.btxtech.shared.rest.ServerMgmtController;
import com.btxtech.shared.system.SimpleExecutorService;
import com.btxtech.uiservice.AssetService;
import com.btxtech.uiservice.control.GameEngineControl;
import com.btxtech.uiservice.control.GameUiControl;
import com.btxtech.uiservice.user.UserUiService;
import elemental2.promise.Promise;

public class BootContext {
    private SimpleExecutorService simpleExecutorService;
    private Caller<ServerMgmtController> serverMgmt;
    private GameEngineControl gameEngineControl;
    private GameUiControl gameUiControl;
    private AssetService assetService;
    private UserUiService userUiService;

    public SimpleExecutorService getSimpleExecutorService() {
        return simpleExecutorService;
    }

    public Caller<ServerMgmtController> getServerMgmt() {
        return serverMgmt;
    }

    public GameUiControl getGameUiControl() {
        return gameUiControl;
    }

    public GameEngineControl getGameEngineControl() {
        return gameEngineControl;
    }

    public AssetService getAssetService() {
        return assetService;
    }

    public UserUiService getUserUiService() {
        return userUiService;
    }

    public void loadWorker(DeferredStartup deferredStartup) {

    }

    public void activateFacebookAppStartLogin() {

    }

    public Promise<Void> loadThreeJsModels(ThreeJsModelConfig[] threeJsModelConfigs, ParticleSystemConfig[] particleSystemConfigs) {
        return null;
    }

    public void initGameEngineControl(ColdGameUiContext coldGameUiContext, DeferredStartup deferredStartup) {

    }

    public void runRenderer(MeshContainer[] meshContainers) {

    }
}
