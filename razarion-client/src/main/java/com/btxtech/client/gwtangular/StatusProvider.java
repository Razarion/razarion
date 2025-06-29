package com.btxtech.client.gwtangular;

import com.btxtech.common.system.ClientExceptionHandlerImpl;
import com.btxtech.shared.rest.AlarmServiceController;
import com.btxtech.shared.system.alarm.Alarm;
import com.btxtech.shared.system.alarm.AlarmService;
import elemental2.promise.Promise;
import jsinterop.annotations.JsType;
import com.btxtech.shared.deprecated.Caller;
import com.btxtech.shared.deprecated.RemoteCallback;

import javax.inject.Singleton;
import javax.inject.Inject;
import java.util.List;

@JsType
@Singleton
public class StatusProvider {

    private AlarmService alarmService;

    private Caller<AlarmServiceController> alarmServiceControllerCaller;

    private ClientExceptionHandlerImpl exceptionHandler;

    @Inject
    public StatusProvider(ClientExceptionHandlerImpl exceptionHandler, Caller<com.btxtech.shared.rest.AlarmServiceController> alarmServiceControllerCaller, AlarmService alarmService) {
        this.exceptionHandler = exceptionHandler;
        this.alarmServiceControllerCaller = alarmServiceControllerCaller;
        this.alarmService = alarmService;
    }
    // TODO private Stats stats;

    @SuppressWarnings("unused") // Called by Angular
    public Alarm[] getClientAlarms() {
        return alarmService.getAlarms().toArray(new Alarm[0]);
    }

    @SuppressWarnings("unused") // Called by Angular
    public Promise<Alarm[]> requestServerAlarms() {
        // TODO return new Promise<>((resolve, reject) ->
        // TODO  alarmServiceControllerCaller.call((RemoteCallback<List<Alarm>>) response -> resolve.onInvoke(response.toArray(new Alarm[0])),
        // TODO exceptionHandler.restErrorHandler("AlarmServiceController")).getAlarms());
        return null;
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
