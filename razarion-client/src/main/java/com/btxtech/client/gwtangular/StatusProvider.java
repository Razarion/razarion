package com.btxtech.client.gwtangular;

import com.btxtech.common.system.ClientExceptionHandlerImpl;
import com.btxtech.shared.rest.AlarmServiceController;
import com.btxtech.shared.system.alarm.Alarm;
import com.btxtech.shared.system.alarm.AlarmService;
import elemental2.promise.Promise;
import jsinterop.annotations.JsType;
import com.btxtech.client.Caller;
import com.btxtech.client.RemoteCallback;

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
    // TODO private Stats stats;

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

    // TODO @SuppressWarnings("unused") // Called by Angular
    // TODO public Stats getStats() {
    // TODO     return stats;
    // TODO }

    // TODO @SuppressWarnings("unused") // Called by Angular
    // TODO public void setStats(Stats stats) {
    // TODO     this.stats = stats;
    // TODO }
}
