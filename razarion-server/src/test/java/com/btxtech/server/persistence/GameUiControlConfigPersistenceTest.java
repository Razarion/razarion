package com.btxtech.server.persistence;

import com.btxtech.server.ArquillianBaseTest;
import com.btxtech.shared.datatypes.UserContext;
import org.junit.Test;

import javax.inject.Inject;

import static org.junit.Assert.*;

/**
 * Created by Beat
 * 10.05.2017.
 */
public class GameUiControlConfigPersistenceTest extends ArquillianBaseTest {
    @Inject
    private GameUiControlConfigPersistence gameUiControlConfigPersistence;

    @Test
    public void testTutorial() {
        UserContext userContext = new UserContext();

        // gameUiControlConfigPersistence.load()

    }
}