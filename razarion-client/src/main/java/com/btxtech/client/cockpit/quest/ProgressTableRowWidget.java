package com.btxtech.client.cockpit.quest;

import com.btxtech.client.StaticResourcePath;
import com.btxtech.shared.system.SimpleExecutorService;
import com.btxtech.shared.system.SimpleScheduledFuture;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.Composite;
import org.jboss.errai.common.client.dom.Div;
import org.jboss.errai.common.client.dom.Image;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;

/**
 * Created by Beat
 * on 09.08.2017.
 */
@Templated("QuestSidebar.html#progressTableRow")
public class ProgressTableRowWidget extends Composite implements TakesValue<ProgressTableRowModel> {
    @Inject
    private SimpleExecutorService simpleExecutorService;
    @Inject
    @DataField
    private Image progressStatusImage;
    @Inject
    @DataField
    private Div progressTextLabel;
    @Inject
    @DataField
    private Image progressBaseItemTypeImage;
    @Inject
    @DataField
    private Div progressActionWordLabel;
    private ProgressTableRowModel progressTableRowModel;
    private SimpleScheduledFuture textCallbackFuture;

    @Override
    public void setValue(ProgressTableRowModel progressTableRowModel) {
        this.progressTableRowModel = progressTableRowModel;
        if (progressTableRowModel.getStatusImage() != null) {
            progressStatusImage.setSrc(StaticResourcePath.getImagePath(progressTableRowModel.getStatusImage()));
            progressStatusImage.getStyle().setProperty("display", "inline");
        } else {
            progressStatusImage.getStyle().setProperty("display", "none");
        }
        if (progressTableRowModel.getText() != null) {
            progressTextLabel.setInnerHTML(progressTableRowModel.getText());
            progressTextLabel.getStyle().setProperty("display", "inline");
        } else {
            progressTextLabel.getStyle().setProperty("display", "none");
        }
        if (progressTableRowModel.getBaseItemImage() != null) {
            progressBaseItemTypeImage.setSrc(progressTableRowModel.getBaseItemImage());
            progressBaseItemTypeImage.getStyle().setProperty("display", "inline");
        } else {
            progressBaseItemTypeImage.getStyle().setProperty("display", "none");
        }
        if (progressTableRowModel.getActionWord() != null) {
            progressActionWordLabel.setInnerHTML(progressTableRowModel.getActionWord());
            progressActionWordLabel.getStyle().setProperty("display", "inline");
        } else {
            progressActionWordLabel.getStyle().setProperty("display", "none");
        }
        if (progressTableRowModel.getTextRefreshInterval() != null && progressTableRowModel.getTextCallback() != null) {
            textCallbackFuture = simpleExecutorService.scheduleAtFixedRate(progressTableRowModel.getTextRefreshInterval(), true, () -> {
                progressTextLabel.setInnerHTML(progressTableRowModel.getTextCallback().get());
            }, SimpleExecutorService.Type.QUEST_PROGRESS_PANEL_TEXT_REFRESHER);
        }
    }

    @Override
    public ProgressTableRowModel getValue() {
        return progressTableRowModel;
    }

    @Override
    protected void onUnload() {
        super.onUnload();
        if (textCallbackFuture != null) {
            textCallbackFuture.cancel();
            textCallbackFuture = null;
        }
    }
}
