package com.btxtech.shared.cdimock;

import com.btxtech.shared.system.SimpleExecutorService;
import com.btxtech.shared.system.SimpleScheduledFuture;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Beat
 * 03.05.2017.
 */
@Singleton
public class TestSimpleExecutorService implements SimpleExecutorService {
    private Collection<TestSimpleScheduledFuture> schedules = new ArrayList<>();
    private Collection<TestSimpleScheduledFuture> scheduleAtFixedRates = new ArrayList<>();

    @Override
    public SimpleScheduledFuture schedule(long delayMilliS, Runnable runnable, Type type) {
        TestSimpleScheduledFuture testSimpleScheduledFuture = new TestSimpleScheduledFuture(false, delayMilliS, runnable, type);
        schedules.add(testSimpleScheduledFuture);
        return testSimpleScheduledFuture;
    }

    @Override
    public SimpleScheduledFuture scheduleAtFixedRate(long delayMilliS, boolean start, Runnable runnable, Type type) {
        TestSimpleScheduledFuture testSimpleScheduledFuture = new TestSimpleScheduledFuture(start, delayMilliS, runnable, type);
        scheduleAtFixedRates.add(testSimpleScheduledFuture);
        return testSimpleScheduledFuture;
    }

    public TestSimpleScheduledFuture getScheduleAtFixedRate(Type type) {
        TestSimpleScheduledFuture testSimpleScheduledFuture = null;
        for (TestSimpleScheduledFuture schedule : scheduleAtFixedRates) {
            if (schedule.getType() == type) {
                if (testSimpleScheduledFuture != null) {
                    throw new IllegalStateException("More then one TestSimpleScheduledFuture found for type: " + type);
                }
                testSimpleScheduledFuture = schedule;
            }
        }
        if (testSimpleScheduledFuture != null) {
            return testSimpleScheduledFuture;
        }
        throw new IllegalStateException("No TestSimpleScheduledFuture found for type: " + type);
    }
}
