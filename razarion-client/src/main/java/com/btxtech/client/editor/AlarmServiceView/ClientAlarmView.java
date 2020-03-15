package com.btxtech.client.editor.AlarmServiceView;

import com.btxtech.shared.system.alarm.AlarmService;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;

@Templated("AbstractAlarmViewEditor.html#alarmView")
public class ClientAlarmView extends AbstractAlarmViewEditor {
    @Inject
    private AlarmService alarmService;

    @Override
    protected void requestAlarms() {
        onAlarmReceived(alarmService.getAlarms());
    }
}
