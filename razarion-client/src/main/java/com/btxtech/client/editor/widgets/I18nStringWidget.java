package com.btxtech.client.editor.widgets;

import com.btxtech.shared.datatypes.I18nString;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.user.client.ui.TextBox;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;
import java.util.function.Consumer;

/**
 * Created by Beat
 * on 23.08.2017.
 */
@Templated("I18nStringWidget.html#i18nStringWidget")
public class I18nStringWidget {
    @Inject
    @DataField
    private TextBox enI18nStringField;
    @Inject
    @DataField
    private TextBox deI18nStringField;
    private I18nString i18nString;
    private Consumer<I18nString> creationCallback;

    public void init(I18nString i18nString, Consumer<I18nString> creationCallback) {
        this.i18nString = i18nString;
        this.creationCallback = creationCallback;
        if (i18nString != null) {
            enI18nStringField.setText(i18nString.getString(I18nString.EN));
            deI18nStringField.setText(i18nString.getString(I18nString.DE));
        }
    }

    @EventHandler("enI18nStringField")
    public void enI18nStringFieldChanged(ChangeEvent e) {
        checkCreation();
        i18nString.setString(I18nString.EN, enI18nStringField.getText());
    }

    @EventHandler("deI18nStringField")
    public void deI18nStringFieldChanged(ChangeEvent e) {
        checkCreation();
        i18nString.setString(I18nString.DE, enI18nStringField.getText());
    }

    private void checkCreation() {
        if (i18nString == null) {
            i18nString = new I18nString();
            creationCallback.accept(i18nString);
        }
    }

}
