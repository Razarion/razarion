package com.btxtech.client.editor.i18n;

import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.Label;
import org.jboss.errai.common.client.api.IsElement;
import org.jboss.errai.common.client.dom.HTMLElement;
import org.jboss.errai.common.client.dom.TableRow;
import org.jboss.errai.common.client.dom.TextArea;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;

/**
 * Created by Beat
 * on 10.09.2017.
 */
@Templated("I18nPanel.html#i18nTableRow")
public class I18nStringEditorWidget implements TakesValue<I18nStringEditorModel>, IsElement {
    @Inject
    @AutoBound
    private DataBinder<I18nStringEditorModel> dataBinder;
    @Inject
    @DataField
    private TableRow i18nTableRow;
    @Inject
    @Bound
    @DataField
    private Label id;
    @Inject
    @Bound
    @DataField
    private TextArea enString;
    @Inject
    @Bound
    @DataField
    private TextArea deString;

    @Override
    public void setValue(I18nStringEditorModel i18nStringEditorModel) {
        dataBinder.setModel(i18nStringEditorModel);
        enString.getStyle().setProperty("background-color", i18nStringEditorModel.hasEnString() ? "width" : "#ffadad");
        deString.getStyle().setProperty("background-color", i18nStringEditorModel.hasDeString() ? "width" : "#ffadad");
    }

    @Override
    public I18nStringEditorModel getValue() {
        return dataBinder.getModel();
    }

    @Override
    public HTMLElement getElement() {
        return i18nTableRow;
    }
}
