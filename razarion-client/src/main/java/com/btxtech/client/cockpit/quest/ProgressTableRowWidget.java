package com.btxtech.client.cockpit.quest;

import com.btxtech.client.StaticResourcePath;
import com.google.gwt.user.client.TakesValue;
import org.jboss.errai.common.client.api.IsElement;
import org.jboss.errai.common.client.dom.HTMLElement;
import org.jboss.errai.common.client.dom.Image;
import org.jboss.errai.common.client.dom.Label;
import org.jboss.errai.common.client.dom.TableRow;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;

/**
 * Created by Beat
 * on 09.08.2017.
 */
@Templated("QuestSidebar.html#progressTableRow")
public class ProgressTableRowWidget implements TakesValue<ProgressTableRowModel>, IsElement {
    @Inject
    @DataField
    private TableRow progressTableRow;
    @Inject
    @DataField
    private Image progressStatusImage;
    @Inject
    @DataField
    private Label progressTextLabel;
    @Inject
    @DataField
    private Image progressBaseItemTypeImage;
    @Inject
    @DataField
    private Label progressActionWordLabel;


    private ProgressTableRowModel progressTableRowModel;

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
    }

    @Override
    public ProgressTableRowModel getValue() {
        return progressTableRowModel;
    }

    @Override
    public HTMLElement getElement() {
        return progressTableRow;
    }
}
