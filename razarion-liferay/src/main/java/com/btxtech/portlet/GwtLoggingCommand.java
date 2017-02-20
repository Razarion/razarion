package com.btxtech.portlet;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCActionCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;
import org.osgi.service.component.annotations.Component;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;

/**
 * Created by Beat
 * 12.06.2016.
 */
@Component(
        immediate = true,
        property = {
                "javax.portlet.name=" + "GWT Logging Portlet",
                "mvc.command.name=gwt-logging"
        },
        service = MVCActionCommand.class
)
public class GwtLoggingCommand extends BaseMVCActionCommand {
    private Log log = LogFactoryUtil.getLog(GwtLoggingCommand.class);

    @Override
    protected void doProcessAction(ActionRequest actionRequest, ActionResponse actionResponse) throws Exception {
        log.error("*************************");
        log.error("**** doProcessAction ****");
        log.error("*************************");
    }
}
