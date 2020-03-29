package com.btxtech.client.editor.AlarmServiceView;

import com.btxtech.shared.system.alarm.Alarm;
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
    private HTMLTableCellElement alarmDate;
    @Inject
    @DataField
    @Named("td")
    private HTMLTableCellElement alarmType;
    @Inject
    @DataField
    @Named("td")
    private HTMLTableCellElement alarmText;
    @Inject
    @DataField
    @Named("td")
    private HTMLTableCellElement alarmId;

    public void init(Alarm alarm) {
        alarmDate.textContent = alarm.getDate().toString();
        alarmType.textContent = alarm.getType().toString();
        alarmText.textContent = alarm.getText();
        if (alarm.getId() != null) {
            alarmId.textContent = Integer.toString(alarm.getId());
        }
    }

    @Override
    public HTMLElement getElement() {
        return alarmRow;
    }
}
