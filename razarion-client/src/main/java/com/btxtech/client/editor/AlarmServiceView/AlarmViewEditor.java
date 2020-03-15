package com.btxtech.client.editor.AlarmServiceView;

import com.btxtech.client.editor.editorpanel.AbstractEditor;
import com.btxtech.shared.system.AlarmService;
import elemental2.dom.HTMLTableElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

@Templated("AlarmViewEditor.html#alarmView")
public class AlarmViewEditor extends AbstractEditor {
    @Inject
    private AlarmService alarmService;
    @Inject
    private Instance<AlarmRow> rowInstance;
    @Inject
    @DataField
    private HTMLTableElement alarmTable;

    @PostConstruct
    public void postConstruct() {
        alarmService.getAlarms().forEach(this::displayAlarm);
    }

    private void displayAlarm(String alarm) {
        AlarmRow alarmRow = rowInstance.get();
        alarmRow.init(alarm);
        alarmTable.appendChild(alarmRow.getElement());
    }


}
