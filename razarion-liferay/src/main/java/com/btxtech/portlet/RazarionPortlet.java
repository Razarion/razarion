package com.btxtech.portlet;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCPortlet;
import org.osgi.service.component.annotations.Component;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.Portlet;
import javax.portlet.ProcessAction;

/**
 * Created by Beat
 * 11.06.2016.
 */
@Component(
        immediate = true,
        property = {
                "com.liferay.portlet.css-class-wrapper=portlet-jsp",
                "com.liferay.portlet.display-category=category.game",
                "com.liferay.portlet.header-portlet-javascript=/jquery/jquery-1.11.3.min.js",
                "com.liferay.portlet.instanceable=true",
                "javax.portlet.display-name=Razarion Portlet",
                "javax.portlet.init-param.template-path=/",
                "javax.portlet.init-param.view-template=/view.jsp",
                "javax.portlet.resource-bundle=content.Language",
                "javax.portlet.security-role-ref=power-user,user,guest"
        },
        service = Portlet.class
)
public class RazarionPortlet extends MVCPortlet {
    private Log log = LogFactoryUtil.getLog(RazarionPortlet.class);

    public RazarionPortlet() {
        log.error("*************** start RazarionPortlet");
    }

    @ProcessAction(name = "gwtlogging")
    public void gwtlogging(ActionRequest actionRequest, ActionResponse actionResponse){
        log.error("*************** gwtlogging");
    }
}
