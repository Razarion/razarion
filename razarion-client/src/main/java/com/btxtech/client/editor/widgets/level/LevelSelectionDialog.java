package com.btxtech.client.editor.widgets.level;

import com.btxtech.client.dialog.framework.ModalDialogContent;
import com.btxtech.client.dialog.framework.ModalDialogPanel;
import com.btxtech.shared.gameengine.LevelService;
import com.btxtech.shared.gameengine.datatypes.config.LevelConfig;
import com.google.gwt.user.client.ui.Composite;
import org.jboss.errai.common.client.dom.DOMUtil;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.databinding.client.components.ListComponent;
import org.jboss.errai.databinding.client.components.ListContainer;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.List;

/**
 * Created by Beat
 * 22.08.2016.
 */
@Templated("LevelSelectionDialog.html#level-selection-dialog")
public class LevelSelectionDialog extends Composite implements ModalDialogContent<Integer> {
    @Inject
    private LevelService levelService;
    @Inject
    @AutoBound
    private DataBinder<List<LevelConfig>> binder;
    @Inject
    @Bound
    @DataField
    @ListContainer("tbody")
    private ListComponent<LevelConfig, LevelSelectionEntry> levels;
    private ModalDialogPanel<Integer> modalDialogPanel;

    @Override
    public void init(Integer selectedId) {
        DOMUtil.removeAllElementChildren(levels.getElement()); // Remove placeholder table row from template.
        binder.setModel(levelService.getOrderedLevels());
        levels.setSelector(shape3DSelectionEntry -> shape3DSelectionEntry.setSelected(true));
        levels.setDeselector(shape3DSelectionEntry -> shape3DSelectionEntry.setSelected(false));
        if (selectedId != null) {
            levels.selectModel(levelService.getLevel(selectedId));
        }
    }

    @Override
    public void customize(ModalDialogPanel<Integer> modalDialogPanel) {
        this.modalDialogPanel = modalDialogPanel;
    }

    public void selectComponent(@Observes LevelSelectionEntry widget) {
        if (isAttached()) {
            levels.deselectAll();
            levels.selectComponent(widget);
            modalDialogPanel.setApplyValue(widget.getValue().getLevelId());
        }
    }

    @Override
    public void onClose() {

    }
}
