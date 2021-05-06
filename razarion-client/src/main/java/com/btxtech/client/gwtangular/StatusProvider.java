package com.btxtech.client.gwtangular;

import com.btxtech.common.system.ClientExceptionHandlerImpl;
import com.btxtech.shared.rest.AlarmServiceController;
import com.btxtech.shared.system.alarm.Alarm;
import com.btxtech.shared.system.alarm.AlarmService;
import elemental2.promise.Promise;
import jsinterop.annotations.JsType;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;

@JsType
@ApplicationScoped
public class StatusProvider {
    @Inject
    private AlarmService alarmService;
    @Inject
    private Caller<AlarmServiceController> alarmServiceControllerCaller;
    @Inject
    private ClientExceptionHandlerImpl exceptionHandler;

    @SuppressWarnings("unused") // Called by Angular
    public Alarm[] getClientAlarms() {
        return alarmService.getAlarms().toArray(new Alarm[0]);
    }

    @SuppressWarnings("unused") // Called by Angular
    public Promise<Alarm[]> requestServerAlarms() {
        return new Promise<>((resolve, reject) ->
                alarmServiceControllerCaller.call((RemoteCallback<List<Alarm>>) response -> resolve.onInvoke(response.toArray(new Alarm[0])),
                exceptionHandler.restErrorHandler("AlarmServiceController")).getAlarms());
    }
}
