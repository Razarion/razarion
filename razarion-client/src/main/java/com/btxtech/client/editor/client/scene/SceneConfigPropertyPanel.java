package com.btxtech.client.editor.client.scene;

import com.btxtech.client.editor.framework.ObjectNamePropertyPanel;
import com.btxtech.client.editor.widgets.marker.DecimalPositionWidget;
import com.btxtech.client.editor.widgets.marker.PolygonField;
import com.btxtech.client.guielements.CommaDoubleBox;
import com.btxtech.client.guielements.DecimalPositionBox;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.dto.ResourceItemPosition;
import com.btxtech.shared.dto.SceneConfig;
import com.btxtech.shared.rest.SceneEditorProvider;
import com.btxtech.uiservice.control.GameUiControl;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.common.client.dom.DOMUtil;
import org.jboss.errai.common.client.dom.Input;
import org.jboss.errai.common.client.dom.Label;
import org.jboss.errai.common.client.dom.NumberInput;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.databinding.client.components.ListComponent;
import org.jboss.errai.databinding.client.components.ListContainer;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * on 12.08.2017.
 */
@Templated("SceneConfigPropertyPanel.html#propertyPanel")
public class SceneConfigPropertyPanel extends ObjectNamePropertyPanel {
    private Logger logger = Logger.getLogger(SceneConfigPropertyPanel.class.getName());
    @Inject
    private Caller<SceneEditorProvider> provider;
    @Inject
    private GameUiControl gameUiControl;
    @Inject
    @AutoBound
    private DataBinder<SceneConfig> dataBinder;
    @Inject
    @Bound
    @DataField
    private Label id;
    @Inject
    @Bound
    @DataField
    private Input internalName;
    @Inject
    @Bound
    @DataField
    private Input introText;
    @Inject
    @Bound
    @DataField
    private CheckBox removeLoadingCover;
    @Inject
    @Bound
    @DataField
    private CheckBox wait4LevelUpDialog;
    @Inject
    @Bound
    @DataField
    private CheckBox wait4QuestPassedDialog;
    @Inject
    @Bound
    @DataField
    private CheckBox waitForBaseLostDialog;
    @Inject
    @Bound
    @DataField
    private NumberInput duration;
    @Inject
    @Bound(property = "viewFieldConfig.fromPosition")
    @DataField
    private DecimalPositionBox vfcFromPosition;
    @Inject
    @Bound(property = "viewFieldConfig.toPosition")
    @DataField
    private DecimalPositionBox vfcToPosition;
    @Inject
    @Bound(property = "viewFieldConfig.speed")
    @DataField
    private CommaDoubleBox vfcSpeed;
    @Inject
    @Bound(property = "viewFieldConfig.cameraLocked")
    @DataField
    private CheckBox vfcCameraLocked;
    @Inject
    @Bound(property = "viewFieldConfig.bottomWidth")
    @DataField
    private CommaDoubleBox vfcBottomWidth;
    @Inject
    @Bound
    @DataField
    @ListContainer("tbody")
    private ListComponent<ResourceItemPosition, ResourceItemPositionRow> resourceItemTypePositions;
    @Inject
    @DataField
    private Button resourcePositionCreateButton;
    @Inject
    @DataField
    private StartPointPlacerWidget startPointPlacerConfig;

    @Override
    public void setObjectNameId(ObjectNameId objectNameId) {
        DOMUtil.removeAllElementChildren(resourceItemTypePositions.getElement()); // Remove placeholder table row from template.
        resourceItemTypePositions.addComponentCreationHandler(resourceItemPositionRow -> resourceItemPositionRow.setSceneConfigPropertyPanel(SceneConfigPropertyPanel.this));
        int gameUiControlConfigId = gameUiControl.getColdGameUiControlConfig().getWarmGameUiControlConfig().getGameUiControlConfigId();
        provider.call(new RemoteCallback<SceneConfig>() {
            @Override
            public void callback(SceneConfig sceneConfig) {
                dataBinder.setModel(sceneConfig);
                startPointPlacerConfig.init(sceneConfig.getStartPointPlacerConfig(), sceneConfig::setStartPointPlacerConfig);
            }
        }, (message, throwable) -> {
            logger.log(Level.SEVERE, "SceneEditorProvider.readSceneConfig failed: " + message, throwable);
            return false;
        }).readSceneConfig(gameUiControlConfigId, objectNameId.getId());
        registerSaveButton(this::save);
        enableSaveButton(true);
    }

    private void save() {
        int gameUiControlConfigId = gameUiControl.getColdGameUiControlConfig().getWarmGameUiControlConfig().getGameUiControlConfigId();
        provider.call(response -> {
        }, (message, throwable) -> {
            logger.log(Level.SEVERE, "SceneEditorProvider.updateSceneConfig failed: " + message, throwable);
            return false;
        }).updateSceneConfig(gameUiControlConfigId, dataBinder.getModel());
    }

    @Override
    public Object getConfigObject() {
        return dataBinder.getModel();
    }

    @EventHandler("resourcePositionCreateButton")
    private void resourcePositionCreateButtonClicked(ClickEvent event) {
        List<ResourceItemPosition> resourceItemPositions = resourceItemTypePositions.getValue();
        if (resourceItemPositions == null) {
            resourceItemPositions = new ArrayList<>();
        }
        resourceItemPositions.add(new ResourceItemPosition());
        resourceItemPositions = new ArrayList<>(resourceItemPositions);
        resourceItemTypePositions.setValue(resourceItemPositions);
        dataBinder.getModel().setResourceItemTypePositions(resourceItemPositions);
    }

    public void removeResourceItemPosition(ResourceItemPosition resourceItemPosition) {
        List<ResourceItemPosition> resourceItemPositions = resourceItemTypePositions.getValue();
        resourceItemPositions.remove(resourceItemPosition);
        resourceItemPositions = new ArrayList<>(resourceItemPositions);
        resourceItemTypePositions.setValue(resourceItemPositions);
        dataBinder.getModel().setResourceItemTypePositions(resourceItemPositions);
    }
}
