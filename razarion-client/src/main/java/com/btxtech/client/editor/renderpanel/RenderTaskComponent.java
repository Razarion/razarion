package com.btxtech.client.editor.renderpanel;

import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.Label;
import org.jboss.errai.common.client.api.IsElement;
import org.jboss.errai.common.client.dom.CheckboxInput;
import org.jboss.errai.common.client.dom.HTMLElement;
import org.jboss.errai.common.client.dom.TableRow;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by Beat
 * 26.03.2017.
 */
@Templated("RenderEngineEditorPanel.html#renderTaskTr")
public class RenderTaskComponent  implements TakesValue<RenderTaskModel>, IsElement {
    @Inject
    @DataField
    @Named("tr")
    private TableRow renderTaskTr;
    @Inject
    @AutoBound
    private DataBinder<RenderTaskModel> dataBinder;
    @Bound
    @Inject
    @DataField
    private Label name;
    @Bound
    @Inject
    @DataField
    private CheckboxInput enabled;

    @Override
    public void setValue(RenderTaskModel renderTaskModel) {
        dataBinder.setModel(renderTaskModel);
    }

    @Override
    public RenderTaskModel getValue() {
        return dataBinder.getModel();
    }

    @Override
    public HTMLElement getElement() {
        return renderTaskTr;
    }
}
