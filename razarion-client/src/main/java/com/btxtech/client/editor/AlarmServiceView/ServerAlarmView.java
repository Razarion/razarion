package com.btxtech.client.editor.AlarmServiceView;

import com.btxtech.common.system.ClientExceptionHandlerImpl;
import com.btxtech.shared.rest.AlarmServiceController;
import com.btxtech.shared.system.alarm.Alarm;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;
import java.util.List;

@Templated("AbstractAlarmViewEditor.html#alarmView")
public class ServerAlarmView extends AbstractAlarmViewEditor {
    @Inject
    private Caller<AlarmServiceController> alarmServiceControllerCaller;
    @Inject
    private ClientExceptionHandlerImpl exceptionHandler;

    @Override
    protected void requestAlarms() {
        alarmServiceControllerCaller.call((RemoteCallback<List<Alarm>>) this::onAlarmReceived,
                exceptionHandler.restErrorHandler("AlarmServiceController")).getAlarms();
    }
}
