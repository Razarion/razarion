package com.btxtech.client.editor.AlarmServiceView;

import com.btxtech.client.editor.editorpanel.AbstractEditor;
import com.btxtech.shared.system.alarm.Alarm;
import elemental2.dom.HTMLTableElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.List;

import static com.btxtech.client.utils.Elemental2Utils.removeAllChildren;

public abstract class AbstractAlarmViewEditor extends AbstractEditor {
    @Inject
    private Instance<AlarmRow> rowInstance;
    @Inject
    @DataField
    private HTMLTableElement alarmTable;

    @PostConstruct
    public void postConstruct() {
       requestAlarms();
    }

    protected abstract void requestAlarms();

    protected void onAlarmReceived(List<Alarm> alarms) {
        removeAllChildren(alarmTable);
        alarms.forEach(this::displayAlarm);
    }

    private void displayAlarm(Alarm alarm) {
        AlarmRow alarmRow = rowInstance.get();
        alarmRow.init(alarm);
        alarmTable.appendChild(alarmRow.getElement());
    }


}
