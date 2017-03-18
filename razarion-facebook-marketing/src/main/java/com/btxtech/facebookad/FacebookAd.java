package com.btxtech.facebookad;

import com.btxtech.facebookad.facade.FbFacade;
import org.jboss.weld.environment.se.Weld;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * Created by Beat
 * 17.03.2017.
 */
@ApplicationScoped
public class FacebookAd {
    @Inject
    private FbFacade fbFacade;

    public static void main(String[] args) {
        Weld weld = new Weld();
        weld.initialize().instance().select(FacebookAd.class).get().start();
    }

    public void start() {
        try {
            // fbFacade.createAd();
            fbFacade.printAllAdSets();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
