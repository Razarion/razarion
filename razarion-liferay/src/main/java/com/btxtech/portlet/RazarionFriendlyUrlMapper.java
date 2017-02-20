package com.btxtech.portlet;

import com.liferay.portal.kernel.portlet.DefaultFriendlyURLMapper;
import com.liferay.portal.kernel.portlet.FriendlyURLMapper;
import org.osgi.service.component.annotations.Component;

/**
 * Created by Beat
 * 12.06.2016.
 */
@Component(
        property = {
                "com.liferay.portlet.friendly-url-routes=META-INF/friendly-url-routes/routesXXXX.xml",
                "javax.portlet.name=Razarion friendly URL Mapper"
        },
        service = FriendlyURLMapper.class
)
public class RazarionFriendlyUrlMapper extends DefaultFriendlyURLMapper {

    @Override
    public String getMapping() {
        return "xxxRAZARion";
    }
}
