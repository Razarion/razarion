package com.btxtech.client.editor.clip;

import com.btxtech.client.editor.framework.AbstractPropertyPanel;
import com.btxtech.client.editor.widgets.shape3dwidget.Shape3DReferenceFiled;
import com.btxtech.client.utils.DisplayUtils;
import com.btxtech.shared.dto.ClipConfig;
import com.btxtech.uiservice.Shape3DUiService;
import com.google.gwt.user.client.ui.Label;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;

/**
 * Created by Beat
 * 22.08.2016.
 */
@Templated("ClipPropertyPanel.html#clip-property-panel")
public class ClipPropertyPanel extends AbstractPropertyPanel<ClipConfig> {
    // private Logger logger = Logger.getLogger(Shape3DPropertyPanel.class.getName());
    @Inject
    @AutoBound
    private DataBinder<ClipConfig> dataBinder;
    @Inject
    private ClipCrud clipCrud;
    @Inject
    private Shape3DUiService clipService;
    @SuppressWarnings("CdiInjectionPointsIn@Bindablespection")
    @Inject
    @Bound
    @DataField
    private Label id;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @Bound
    @DataField
    private Label internalName;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Shape3DReferenceFiled shape3DId;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Label duration;

    @Override
    public void init(ClipConfig clipConfig) {
        dataBinder.setModel(clipConfig);
        onChange(clipConfig);
        shape3DId.init(clipConfig.getShape3DId(), shape3DId -> {
            clipConfig.setShape3DId(shape3DId);
            clipConfig.setDurationMillis(clipService.getShape3D(shape3DId).calculateAnimationDuration());
            duration.setText(DisplayUtils.handleInteger(clipConfig.getDurationMillis()));
        });
        if (clipConfig.getShape3DId() != null) {
            duration.setText(DisplayUtils.handleInteger(clipConfig.getDurationMillis()));
        }
    }

    @Override
    public ClipConfig getConfigObject() {
        return dataBinder.getModel();
    }
}
