package com.btxtech.shared.rest;

import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.system.alarm.Alarm;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path(CommonUrl.ALARM_SERVICE_PATH)
public interface AlarmServiceController {
    @GET
    @Path("alarms")
    @Produces(MediaType.APPLICATION_JSON)
    List<Alarm> getAlarms();

}
