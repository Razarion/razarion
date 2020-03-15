package com.btxtech.client.editor.AlarmServiceView;

import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLTableCellElement;
import elemental2.dom.HTMLTableRowElement;
import org.jboss.errai.common.client.api.elemental2.IsElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;
import javax.inject.Named;

@Templated("AlarmRow.html#alarmRow")
public class AlarmRow implements IsElement {
    @Inject
    @DataField
    private HTMLTableRowElement alarmRow;
    @Inject
    @DataField
    @Named("td")
    private HTMLTableCellElement alarmText;

    public void init(String alarm) {
        alarmText.textContent = alarm;
    }

    @Override
    public HTMLElement getElement() {
        return alarmRow;
    }
}
