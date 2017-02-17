package com.btxtech.server.liferay;

import com.liferay.faces.util.context.FacesContextHelperUtil;
import com.liferay.faces.util.logging.Logger;
import com.liferay.faces.util.logging.LoggerFactory;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.event.ActionEvent;

/**
 * Created by Beat
 * 17.02.2017.
 */
@RequestScoped
@ManagedBean
public class ExampleBacking {
    private static final Logger logger = LoggerFactory.getLogger(ExampleBacking.class);
    private String name;

    public ExampleBacking() {
        logger.error("ExampleBacking(): " + this);
    }

    public String getName() {
        logger.error("ExampleBacking.getName(): " + name + " this: " + this);
        return name;
    }

    public void setName(String name) {
        logger.error("ExampleBacking.setName(): " + name + " this: " + this);
        this.name = name;
    }

    public void submit(ActionEvent actionEvent) {
        logger.error("ExampleBacking.submit(): this: " + this);
        FacesContextHelperUtil.addGlobalSuccessInfoMessage();
    }
}
