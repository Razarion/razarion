package com.btxtech.server;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.runner.RunWith;

/**
 * Created by Beat
 * 05.05.2017.
 */
@Deprecated // Not using Arquillian anymore
@RunWith(Arquillian.class) // Ignores tests
public abstract class IgnoreOldArquillianTest extends ServerTestHelper {
    @Deployment
    public static Archive<?> createDeployment() {
        return null;
    }
}