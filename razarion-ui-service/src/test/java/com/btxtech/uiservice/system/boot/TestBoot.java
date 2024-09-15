package com.btxtech.uiservice.system.boot;

import com.btxtech.shared.system.alarm.Alarm;
import com.btxtech.shared.system.alarm.AlarmRaisedException;
import com.btxtech.shared.system.alarm.AlarmService;
import com.btxtech.shared.utils.ExceptionUtil;
import org.easymock.EasyMock;
import org.easymock.IArgumentMatcher;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

import static org.easymock.EasyMock.contains;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.geq;
import static org.easymock.EasyMock.notNull;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

/**
 * User: beat
 * Date: 18.02.2011
 * Time: 20:20:18
 */
public class TestBoot {
    private static final String ERROR_TEXT = "error_wsxedcrfv";
    private static final AlarmRaisedException ALARM_RAISED_EXCEPTION = new AlarmRaisedException(Alarm.Type.FAIL_START_GAME_ENGINE, "xxaazz", 182);
    private WeldContainer weldContainer;

    @Test
    public void runSimple() {
        StartupProgressListener mockListener = createStrictMock(StartupProgressListener.class);
        mockListener.onStart(StartupTestSeq.TEST_SIMPLE);

        mockListener.onNextTask(SimpleTestTaskEnum.TEST_1);
        mockListener.onTaskFinished(eqAbstractStartupTask(SimpleTestTaskEnum.TEST_1));

        mockListener.onNextTask(SimpleTestTaskEnum.TEST_2);
        mockListener.onTaskFinished(eqAbstractStartupTask(SimpleTestTaskEnum.TEST_2));

        mockListener.onNextTask(SimpleTestTaskEnum.TEST_3);
        mockListener.onTaskFinished(eqAbstractStartupTask(SimpleTestTaskEnum.TEST_3));

        mockListener.onStartupFinished(eqStartupTaskInfo(StartupTestSeq.TEST_SIMPLE), geq(0L));
        replay(mockListener);

        Boot boot = createClientRunner();
        boot.addStartupProgressListener(mockListener);
        boot.start(StartupTestSeq.TEST_SIMPLE);
        verify(mockListener);
        failOnException();
    }

    @Test
    public void runDeferred() {
        StartupProgressListener mockListener = createStrictMock(StartupProgressListener.class);
        mockListener.onStart(StartupTestSeq.TEST_DEFERRED);

        mockListener.onNextTask(DeferredTestTaskEnum.TEST_1);
        mockListener.onTaskFinished(eqAbstractStartupTask(DeferredTestTaskEnum.TEST_1));

        mockListener.onNextTask(DeferredTestTaskEnum.TEST_2);
        mockListener.onTaskFinished(eqAbstractStartupTask(DeferredTestTaskEnum.TEST_2));

        mockListener.onNextTask(DeferredTestTaskEnum.TEST_3);
        mockListener.onTaskFinished(eqAbstractStartupTask(DeferredTestTaskEnum.TEST_3));

        mockListener.onStartupFinished(eqStartupTaskInfo(StartupTestSeq.TEST_DEFERRED), geq(0L));
        replay(mockListener);

        Boot boot = createClientRunner();
        boot.addStartupProgressListener(mockListener);
        boot.start(StartupTestSeq.TEST_DEFERRED);
        getStartupTestTaskMonitor().getDeferredStartupTestTask(0).finished();
        getStartupTestTaskMonitor().getDeferredStartupTestTask(1).finished();
        getStartupTestTaskMonitor().getDeferredStartupTestTask(2).finished();

        verify(mockListener);
        failOnException();
    }

    @Test
    public void runDeferredBackground() {
        StartupProgressListener mockListener = createStrictMock(StartupProgressListener.class);
        mockListener.onStart(StartupTestSeq.TEST_BACKGROUND);

        mockListener.onNextTask(DeferredBackgroundTestTaskEnum.TEST_1);
        mockListener.onTaskFinished(eqAbstractStartupTask(DeferredBackgroundTestTaskEnum.TEST_1));

        mockListener.onNextTask(DeferredBackgroundTestTaskEnum.TEST_2_BACKGROUND);

        mockListener.onNextTask(DeferredBackgroundTestTaskEnum.TEST_3);
        mockListener.onTaskFinished(eqAbstractStartupTask(DeferredBackgroundTestTaskEnum.TEST_3));

        mockListener.onNextTask(DeferredBackgroundTestTaskEnum.TEST_4);
        mockListener.onTaskFinished(eqAbstractStartupTask(DeferredBackgroundTestTaskEnum.TEST_4));

        mockListener.onTaskFinished(eqAbstractStartupTask(DeferredBackgroundTestTaskEnum.TEST_2_BACKGROUND));

        mockListener.onStartupFinished(eqStartupTaskInfo(new StartupTaskEnum[]{DeferredBackgroundTestTaskEnum.TEST_1,
                DeferredBackgroundTestTaskEnum.TEST_3,
                DeferredBackgroundTestTaskEnum.TEST_4,
                DeferredBackgroundTestTaskEnum.TEST_2_BACKGROUND}), geq(0L));

        replay(mockListener);
        Boot boot = createClientRunner();
        boot.addStartupProgressListener(mockListener);
        boot.start(StartupTestSeq.TEST_BACKGROUND);
        getStartupTestTaskMonitor().getDeferredStartupTestTask(0).finished();
        getStartupTestTaskMonitor().getDeferredStartupTestTask(1).finished();
        getStartupTestTaskMonitor().getDeferredStartupTestTask(2).finished();
        getStartupTestTaskMonitor().getDeferredBackgroundStartupTestTask(0).finished();

        verify(mockListener);
        failOnException();
    }

    @Test
    public void runDeferredFinished() {
        StartupProgressListener mockListener = createStrictMock(StartupProgressListener.class);
        mockListener.onStart(StartupTestSeq.TEST_DEFERRED_FINISH);

        mockListener.onNextTask(DeferredFinishTestTaskEnum.TEST_1);
        mockListener.onTaskFinished(eqAbstractStartupTask(DeferredFinishTestTaskEnum.TEST_1));

        mockListener.onNextTask(DeferredFinishTestTaskEnum.TEST_2_FINISH);
        mockListener.onTaskFinished(eqAbstractStartupTask(DeferredFinishTestTaskEnum.TEST_2_FINISH));

        mockListener.onNextTask(DeferredFinishTestTaskEnum.TEST_3);
        mockListener.onTaskFinished(eqAbstractStartupTask(DeferredFinishTestTaskEnum.TEST_3));


        mockListener.onStartupFinished(eqStartupTaskInfo(StartupTestSeq.TEST_DEFERRED_FINISH), geq(0L));

        replay(mockListener);
        Boot boot = createClientRunner();
        boot.addStartupProgressListener(mockListener);
        boot.start(StartupTestSeq.TEST_DEFERRED_FINISH);
        getStartupTestTaskMonitor().getDeferredStartupTestTask(0).finished();
        getStartupTestTaskMonitor().getDeferredStartupTestTask(1).finished();
        verify(mockListener);
        failOnException();
    }

    @Test
    public void runDeferredBackgroundFinished() {
        StartupProgressListener mockListener = createStrictMock(StartupProgressListener.class);
        mockListener.onStart(StartupTestSeq.TEST_DEFERRED_BACKGROUND_FINISH);

        mockListener.onNextTask(DeferredBackgroundFinishTestTaskEnum.TEST_1);
        mockListener.onTaskFinished(eqAbstractStartupTask(DeferredBackgroundFinishTestTaskEnum.TEST_1));

        mockListener.onNextTask(DeferredBackgroundFinishTestTaskEnum.TEST_2_BACKGROUND);

        mockListener.onNextTask(DeferredBackgroundFinishTestTaskEnum.TEST_3_DEFERRED_BACKGROUND_FINISH);
        mockListener.onTaskFinished(eqAbstractStartupTask(DeferredBackgroundFinishTestTaskEnum.TEST_3_DEFERRED_BACKGROUND_FINISH));

        mockListener.onNextTask(DeferredBackgroundFinishTestTaskEnum.TEST_4_DEFERRED_FINISH);
        mockListener.onTaskFinished(eqAbstractStartupTask(DeferredBackgroundFinishTestTaskEnum.TEST_4_DEFERRED_FINISH));

        mockListener.onTaskFinished(eqAbstractStartupTask(DeferredBackgroundFinishTestTaskEnum.TEST_2_BACKGROUND));

        mockListener.onStartupFinished(eqStartupTaskInfo(new StartupTaskEnum[]{DeferredBackgroundFinishTestTaskEnum.TEST_1,
                DeferredBackgroundFinishTestTaskEnum.TEST_3_DEFERRED_BACKGROUND_FINISH,
                DeferredBackgroundFinishTestTaskEnum.TEST_4_DEFERRED_FINISH,
                DeferredBackgroundFinishTestTaskEnum.TEST_2_BACKGROUND
        }), geq(0L));

        replay(mockListener);
        Boot boot = createClientRunner();
        boot.addStartupProgressListener(mockListener);
        boot.start(StartupTestSeq.TEST_DEFERRED_BACKGROUND_FINISH);
        getStartupTestTaskMonitor().getDeferredStartupTestTask(0).finished();
        getStartupTestTaskMonitor().getDeferredBackgroundStartupTestTask(0).finished();
        verify(mockListener);
        failOnException();
    }

    @Test
    public void runMultiple() {
        StartupProgressListener mockListener = createStrictMock(StartupProgressListener.class);
        // 1
        mockListener.onStart(StartupTestSeq.TEST_SIMPLE);

        mockListener.onNextTask(SimpleTestTaskEnum.TEST_1);
        mockListener.onTaskFinished(eqAbstractStartupTask(SimpleTestTaskEnum.TEST_1));

        mockListener.onNextTask(SimpleTestTaskEnum.TEST_2);
        mockListener.onTaskFinished(eqAbstractStartupTask(SimpleTestTaskEnum.TEST_2));

        mockListener.onNextTask(SimpleTestTaskEnum.TEST_3);
        mockListener.onTaskFinished(eqAbstractStartupTask(SimpleTestTaskEnum.TEST_3));

        mockListener.onStartupFinished(eqStartupTaskInfo(StartupTestSeq.TEST_SIMPLE), geq(0L));

        // 2
        mockListener.onStart(StartupTestSeq.TEST_DEFERRED);

        mockListener.onNextTask(DeferredTestTaskEnum.TEST_1);
        mockListener.onTaskFinished(eqAbstractStartupTask(DeferredTestTaskEnum.TEST_1));

        mockListener.onNextTask(DeferredTestTaskEnum.TEST_2);
        mockListener.onTaskFinished(eqAbstractStartupTask(DeferredTestTaskEnum.TEST_2));

        mockListener.onNextTask(DeferredTestTaskEnum.TEST_3);
        mockListener.onTaskFinished(eqAbstractStartupTask(DeferredTestTaskEnum.TEST_3));

        mockListener.onStartupFinished(eqStartupTaskInfo(StartupTestSeq.TEST_DEFERRED), geq(0L));

        // 3
        mockListener.onStart(StartupTestSeq.TEST_BACKGROUND);

        mockListener.onNextTask(DeferredBackgroundTestTaskEnum.TEST_1);
        mockListener.onTaskFinished(eqAbstractStartupTask(DeferredBackgroundTestTaskEnum.TEST_1));

        mockListener.onNextTask(DeferredBackgroundTestTaskEnum.TEST_2_BACKGROUND);

        mockListener.onNextTask(DeferredBackgroundTestTaskEnum.TEST_3);
        mockListener.onTaskFinished(eqAbstractStartupTask(DeferredBackgroundTestTaskEnum.TEST_3));

        mockListener.onNextTask(DeferredBackgroundTestTaskEnum.TEST_4);
        mockListener.onTaskFinished(eqAbstractStartupTask(DeferredBackgroundTestTaskEnum.TEST_4));

        mockListener.onTaskFinished(eqAbstractStartupTask(DeferredBackgroundTestTaskEnum.TEST_2_BACKGROUND));


        mockListener.onStartupFinished(eqStartupTaskInfo(new StartupTaskEnum[]{DeferredBackgroundTestTaskEnum.TEST_1,
                DeferredBackgroundTestTaskEnum.TEST_3,
                DeferredBackgroundTestTaskEnum.TEST_4,
                DeferredBackgroundTestTaskEnum.TEST_2_BACKGROUND}), geq(0L));

        // 4
        mockListener.onStart(StartupTestSeq.TEST_DEFERRED_FINISH);

        mockListener.onNextTask(DeferredFinishTestTaskEnum.TEST_1);
        mockListener.onTaskFinished(eqAbstractStartupTask(DeferredFinishTestTaskEnum.TEST_1));

        mockListener.onNextTask(DeferredFinishTestTaskEnum.TEST_2_FINISH);
        mockListener.onTaskFinished(eqAbstractStartupTask(DeferredFinishTestTaskEnum.TEST_2_FINISH));

        mockListener.onNextTask(DeferredFinishTestTaskEnum.TEST_3);
        mockListener.onTaskFinished(eqAbstractStartupTask(DeferredFinishTestTaskEnum.TEST_3));


        mockListener.onStartupFinished(eqStartupTaskInfo(StartupTestSeq.TEST_DEFERRED_FINISH), geq(0L));

        // 5
        mockListener.onStart(StartupTestSeq.TEST_DEFERRED_BACKGROUND_FINISH);

        mockListener.onNextTask(DeferredBackgroundFinishTestTaskEnum.TEST_1);
        mockListener.onTaskFinished(eqAbstractStartupTask(DeferredBackgroundFinishTestTaskEnum.TEST_1));

        mockListener.onNextTask(DeferredBackgroundFinishTestTaskEnum.TEST_2_BACKGROUND);

        mockListener.onNextTask(DeferredBackgroundFinishTestTaskEnum.TEST_3_DEFERRED_BACKGROUND_FINISH);
        mockListener.onTaskFinished(eqAbstractStartupTask(DeferredBackgroundFinishTestTaskEnum.TEST_3_DEFERRED_BACKGROUND_FINISH));

        mockListener.onNextTask(DeferredBackgroundFinishTestTaskEnum.TEST_4_DEFERRED_FINISH);
        mockListener.onTaskFinished(eqAbstractStartupTask(DeferredBackgroundFinishTestTaskEnum.TEST_4_DEFERRED_FINISH));

        mockListener.onTaskFinished(eqAbstractStartupTask(DeferredBackgroundFinishTestTaskEnum.TEST_2_BACKGROUND));

        mockListener.onStartupFinished(eqStartupTaskInfo(new StartupTaskEnum[]{DeferredBackgroundFinishTestTaskEnum.TEST_1,
                DeferredBackgroundFinishTestTaskEnum.TEST_3_DEFERRED_BACKGROUND_FINISH,
                DeferredBackgroundFinishTestTaskEnum.TEST_4_DEFERRED_FINISH,
                DeferredBackgroundFinishTestTaskEnum.TEST_2_BACKGROUND
        }), geq(0L));

        replay(mockListener);
        Boot boot = createClientRunner();
        boot.addStartupProgressListener(mockListener);

        // 1
        boot.start(StartupTestSeq.TEST_SIMPLE);

        // 2
        boot.start(StartupTestSeq.TEST_DEFERRED);
        getStartupTestTaskMonitor().getDeferredStartupTestTask(0).finished();
        getStartupTestTaskMonitor().getDeferredStartupTestTask(1).finished();
        getStartupTestTaskMonitor().getDeferredStartupTestTask(2).finished();

        // 3
        boot.start(StartupTestSeq.TEST_BACKGROUND);
        getStartupTestTaskMonitor().getDeferredStartupTestTask(3).finished();
        getStartupTestTaskMonitor().getDeferredStartupTestTask(4).finished();
        getStartupTestTaskMonitor().getDeferredStartupTestTask(5).finished();
        getStartupTestTaskMonitor().getDeferredBackgroundStartupTestTask(0).finished();

        // 4
        boot.start(StartupTestSeq.TEST_DEFERRED_FINISH);
        getStartupTestTaskMonitor().getDeferredStartupTestTask(6).finished();
        getStartupTestTaskMonitor().getDeferredStartupTestTask(7).finished();

        // 5
        boot.start(StartupTestSeq.TEST_DEFERRED_BACKGROUND_FINISH);
        getStartupTestTaskMonitor().getDeferredStartupTestTask(8).finished();
        getStartupTestTaskMonitor().getDeferredBackgroundStartupTestTask(1).finished();

        verify(mockListener);
        failOnException();
    }

    @Test
    public void runSimpleException() {
        StartupProgressListener mockListener = createStrictMock(StartupProgressListener.class);
        mockListener.onStart(StartupTestSeq.TEST_SIMPLE_EXCEPTION);

        mockListener.onNextTask(SimpleExceptionTestTaskEnum.TEST_1);
        mockListener.onTaskFinished(eqAbstractStartupTask(SimpleExceptionTestTaskEnum.TEST_1));

        mockListener.onNextTask(SimpleExceptionTestTaskEnum.TEST_2_EXCEPTION);
        mockListener.onTaskFailed(eqAbstractStartupTask(SimpleExceptionTestTaskEnum.TEST_2_EXCEPTION), contains(SimpleExceptionStartupTestTask.ERROR_STRING), eq(SimpleExceptionStartupTestTask.EXCEPTION));

        mockListener.onStartupFailed(eqStartupTaskInfo(new StartupTaskEnum[]{SimpleExceptionTestTaskEnum.TEST_1, SimpleExceptionTestTaskEnum.TEST_2_EXCEPTION}), geq(0L));
        replay(mockListener);
        Boot boot = createClientRunner();
        boot.addStartupProgressListener(mockListener);
        boot.start(StartupTestSeq.TEST_SIMPLE_EXCEPTION);
        verify(mockListener);
        failOnException();
    }

    @Test
    public void runDeferredFail() {
        StartupProgressListener mockListener = createStrictMock(StartupProgressListener.class);
        mockListener.onStart(StartupTestSeq.TEST_BACKGROUND);

        mockListener.onNextTask(DeferredBackgroundTestTaskEnum.TEST_1);
        mockListener.onTaskFinished(eqAbstractStartupTask(DeferredBackgroundTestTaskEnum.TEST_1));

        mockListener.onNextTask(DeferredBackgroundTestTaskEnum.TEST_2_BACKGROUND);

        mockListener.onNextTask(DeferredBackgroundTestTaskEnum.TEST_3);
        mockListener.onTaskFailed(eqAbstractStartupTask(DeferredBackgroundTestTaskEnum.TEST_3), contains(ERROR_TEXT), eq(null));


        mockListener.onStartupFailed(eqStartupTaskInfo(new StartupTaskEnum[]{DeferredBackgroundTestTaskEnum.TEST_1,
                        DeferredBackgroundTestTaskEnum.TEST_3,
                        DeferredBackgroundTestTaskEnum.TEST_4}),
                geq(0L));

        replay(mockListener);
        Boot boot = createClientRunner();
        boot.addStartupProgressListener(mockListener);
        boot.start(StartupTestSeq.TEST_BACKGROUND);
        getStartupTestTaskMonitor().getDeferredStartupTestTask(0).finished();
        getStartupTestTaskMonitor().getDeferredStartupTestTask(1).failed(ERROR_TEXT);

        verify(mockListener);
        failOnException();
    }

    @Test
    public void runDeferredBackgroundException() {
        StartupProgressListener mockListener = createStrictMock(StartupProgressListener.class);
        mockListener.onStart(StartupTestSeq.TEST_BACKGROUND);

        mockListener.onNextTask(DeferredBackgroundTestTaskEnum.TEST_1);
        mockListener.onTaskFinished(eqAbstractStartupTask(DeferredBackgroundTestTaskEnum.TEST_1));

        mockListener.onNextTask(DeferredBackgroundTestTaskEnum.TEST_2_BACKGROUND);

        mockListener.onNextTask(DeferredBackgroundTestTaskEnum.TEST_3);
        mockListener.onTaskFinished(eqAbstractStartupTask(DeferredBackgroundTestTaskEnum.TEST_3));

        mockListener.onNextTask(DeferredBackgroundTestTaskEnum.TEST_4);

        Exception exception = new Exception("Test");
        mockListener.onTaskFailed(eqAbstractStartupTask(DeferredBackgroundTestTaskEnum.TEST_2_BACKGROUND), eq(ExceptionUtil.setupStackTrace(null, exception)), eq(exception));


        mockListener.onStartupFailed(eqStartupTaskInfo(new StartupTaskEnum[]{DeferredBackgroundTestTaskEnum.TEST_1,
                        DeferredBackgroundTestTaskEnum.TEST_3,
                        DeferredBackgroundTestTaskEnum.TEST_2_BACKGROUND}),
                geq(0L));

        replay(mockListener);
        Boot boot = createClientRunner();
        boot.addStartupProgressListener(mockListener);
        boot.start(StartupTestSeq.TEST_BACKGROUND);
        getStartupTestTaskMonitor().getDeferredStartupTestTask(0).finished();
        getStartupTestTaskMonitor().getDeferredStartupTestTask(1).finished();
        getStartupTestTaskMonitor().getDeferredBackgroundStartupTestTask(0).failed(exception);
        getStartupTestTaskMonitor().getDeferredStartupTestTask(2).finished();

        verify(mockListener);
        failOnException();
    }

    @Test
    public void runMultiStartup() {
        StartupProgressListener mockListener = createStrictMock(StartupProgressListener.class);
        mockListener.onStart(StartupTestSeq.TEST_MULTI);

        mockListener.onNextTask(MultiTestTaskEnum.SIMPLE);
        mockListener.onTaskFinished(eqAbstractStartupTask(MultiTestTaskEnum.SIMPLE));

        mockListener.onNextTask(MultiTestTaskEnum.DEFERRED);
        mockListener.onTaskFinished(eqAbstractStartupTask(MultiTestTaskEnum.DEFERRED));

        mockListener.onNextTask(MultiTestTaskEnum.SIMPLE_2);
        mockListener.onTaskFinished(eqAbstractStartupTask(MultiTestTaskEnum.SIMPLE_2));

        mockListener.onNextTask(MultiTestTaskEnum.DEFERRED_BACKGROUND_FINISH);
        mockListener.onTaskFinished(eqAbstractStartupTask(MultiTestTaskEnum.DEFERRED_BACKGROUND_FINISH));

        mockListener.onNextTask(MultiTestTaskEnum.SIMPLE_3);
        mockListener.onTaskFinished(eqAbstractStartupTask(MultiTestTaskEnum.SIMPLE_3));

        mockListener.onStartupFinished(eqStartupTaskInfo(StartupTestSeq.TEST_MULTI), geq(0L));

        replay(mockListener);
        Boot boot = createClientRunner();
        boot.addStartupProgressListener(mockListener);
        boot.start(StartupTestSeq.TEST_MULTI);
        getStartupTestTaskMonitor().getDeferredStartupTestTask(0).finished();
        verify(mockListener);
        failOnException();
    }

    @Test
    public void runWaitForBackgroundSimple() {
        StartupProgressListener mockListener = createStrictMock(StartupProgressListener.class);
        mockListener.onStart(StartupTestSeq.TEST_WAIT_FOR_BACKGROUND_SIMPLE);

        mockListener.onNextTask(WaitForBackgroundTestTaskEnum.TEST_1_BACKGROUND);

        mockListener.onNextTask(WaitForBackgroundTestTaskEnum.TEST_2_SIMPLE);
        mockListener.onTaskFinished(eqAbstractStartupTask(WaitForBackgroundTestTaskEnum.TEST_2_SIMPLE));

        mockListener.onTaskFinished(eqAbstractStartupTask(WaitForBackgroundTestTaskEnum.TEST_1_BACKGROUND));

        mockListener.onNextTask(WaitForBackgroundTestTaskEnum.TEST_3_SIMPLE_WAIT_FOR_BACKGROUND);
        mockListener.onTaskFinished(eqAbstractStartupTask(WaitForBackgroundTestTaskEnum.TEST_3_SIMPLE_WAIT_FOR_BACKGROUND));

        mockListener.onNextTask(WaitForBackgroundTestTaskEnum.TEST_4_SIMPLE);
        mockListener.onTaskFinished(eqAbstractStartupTask(WaitForBackgroundTestTaskEnum.TEST_4_SIMPLE));

        mockListener.onStartupFinished(eqStartupTaskInfo(new StartupTaskEnum[]{WaitForBackgroundTestTaskEnum.TEST_2_SIMPLE,
                WaitForBackgroundTestTaskEnum.TEST_1_BACKGROUND,
                WaitForBackgroundTestTaskEnum.TEST_3_SIMPLE_WAIT_FOR_BACKGROUND,
                WaitForBackgroundTestTaskEnum.TEST_4_SIMPLE
        }), geq(0L));
        replay(mockListener);

        Boot boot = createClientRunner();
        boot.addStartupProgressListener(mockListener);
        boot.start(StartupTestSeq.TEST_WAIT_FOR_BACKGROUND_SIMPLE);
        getStartupTestTaskMonitor().getDeferredBackgroundStartupTestTask(0).finished();
        verify(mockListener);
        failOnException();
    }

    @Test
    public void runWaitForBackgroundComplex() {
        StartupProgressListener mockListener = createStrictMock(StartupProgressListener.class);
        mockListener.onStart(StartupTestSeq.TEST_WAIT_FOR_BACKGROUND_COMPLEX);

        mockListener.onNextTask(WaitForBackgroundComplexTestTaskEnum.TEST_1_DEFERRED_BACKGROUND);

        mockListener.onNextTask(WaitForBackgroundComplexTestTaskEnum.TEST_2_DEFERRED);
        mockListener.onTaskFinished(eqAbstractStartupTask(WaitForBackgroundComplexTestTaskEnum.TEST_2_DEFERRED));

        mockListener.onNextTask(WaitForBackgroundComplexTestTaskEnum.TEST_3_DEFERRED_BACKGROUND);

        mockListener.onTaskFinished(eqAbstractStartupTask(WaitForBackgroundComplexTestTaskEnum.TEST_1_DEFERRED_BACKGROUND));

        mockListener.onNextTask(WaitForBackgroundComplexTestTaskEnum.TEST_4_DEFERRED_BACKGROUND);

        mockListener.onNextTask(WaitForBackgroundComplexTestTaskEnum.TEST_5_DEFERRED_BACKGROUND);

        mockListener.onNextTask(WaitForBackgroundComplexTestTaskEnum.TEST_6_SIMPLE);
        mockListener.onTaskFinished(eqAbstractStartupTask(WaitForBackgroundComplexTestTaskEnum.TEST_6_SIMPLE));

        mockListener.onTaskFinished(eqAbstractStartupTask(WaitForBackgroundComplexTestTaskEnum.TEST_3_DEFERRED_BACKGROUND));
        mockListener.onNextTask(WaitForBackgroundComplexTestTaskEnum.TEST_7_SIMPLE);
        mockListener.onTaskFinished(eqAbstractStartupTask(WaitForBackgroundComplexTestTaskEnum.TEST_7_SIMPLE));

        mockListener.onTaskFinished(eqAbstractStartupTask(WaitForBackgroundComplexTestTaskEnum.TEST_5_DEFERRED_BACKGROUND));
        mockListener.onTaskFinished(eqAbstractStartupTask(WaitForBackgroundComplexTestTaskEnum.TEST_4_DEFERRED_BACKGROUND));
        mockListener.onNextTask(WaitForBackgroundComplexTestTaskEnum.TEST_8_SIMPLE);
        mockListener.onTaskFinished(eqAbstractStartupTask(WaitForBackgroundComplexTestTaskEnum.TEST_8_SIMPLE));
        mockListener.onNextTask(WaitForBackgroundComplexTestTaskEnum.TEST_9_SIMPLE);
        mockListener.onTaskFinished(eqAbstractStartupTask(WaitForBackgroundComplexTestTaskEnum.TEST_9_SIMPLE));

        mockListener.onStartupFinished(eqStartupTaskInfo(new StartupTaskEnum[]{WaitForBackgroundComplexTestTaskEnum.TEST_2_DEFERRED,
                WaitForBackgroundComplexTestTaskEnum.TEST_1_DEFERRED_BACKGROUND,
                WaitForBackgroundComplexTestTaskEnum.TEST_6_SIMPLE,
                WaitForBackgroundComplexTestTaskEnum.TEST_3_DEFERRED_BACKGROUND,
                WaitForBackgroundComplexTestTaskEnum.TEST_7_SIMPLE,
                WaitForBackgroundComplexTestTaskEnum.TEST_5_DEFERRED_BACKGROUND,
                WaitForBackgroundComplexTestTaskEnum.TEST_4_DEFERRED_BACKGROUND,
                WaitForBackgroundComplexTestTaskEnum.TEST_8_SIMPLE,
                WaitForBackgroundComplexTestTaskEnum.TEST_9_SIMPLE
        }), geq(0L));

        replay(mockListener);

        Boot boot = createClientRunner();
        boot.addStartupProgressListener(mockListener);
        boot.start(StartupTestSeq.TEST_WAIT_FOR_BACKGROUND_COMPLEX);
        getStartupTestTaskMonitor().getDeferredStartupTestTask(0).finished();
        getStartupTestTaskMonitor().getDeferredBackgroundStartupTestTask(0).finished();
        getStartupTestTaskMonitor().getDeferredBackgroundStartupTestTask(1).finished();
        getStartupTestTaskMonitor().getDeferredBackgroundStartupTestTask(3).finished();
        getStartupTestTaskMonitor().getDeferredBackgroundStartupTestTask(2).finished();
        verify(mockListener);
        failOnException();
    }

    @Test
    public void runAlarmRaising() {
        StartupProgressListener mockListener = createStrictMock(StartupProgressListener.class);
        mockListener.onStart(StartupTestSeq.TEST_ALARM_RAISING);

        mockListener.onNextTask(AlarmRaisingTestTaskEnum.RAISE_ALARM);
        mockListener.onTaskFailed(eqAbstractStartupTask(AlarmRaisingTestTaskEnum.RAISE_ALARM), contains(Alarm.Type.NO_WARM_GAME_UI_CONTEXT.name()), notNull());

        mockListener.onStartupFailed(eqStartupTaskInfo(new StartupTaskEnum[]{AlarmRaisingTestTaskEnum.RAISE_ALARM}), geq(0L));

        replay(mockListener);

        Boot boot = createClientRunner();
        boot.addStartupProgressListener(mockListener);
        boot.start(StartupTestSeq.TEST_ALARM_RAISING);

        verify(mockListener);
        failOnException();

//        List<Alarm> alarms = weldContainer.select(AlarmService.class).get().getAlarms();
//        Assert.assertEquals(alarms.size(), 1);
//        Assert.assertEquals((Integer) 124, alarms.get(0).getId());
//        Assert.assertEquals("Test boot alarm", alarms.get(0).getText());
//        Assert.assertEquals(Alarm.Type.NO_WARM_GAME_UI_CONTEXT, alarms.get(0).getType());
//        Assert.assertNotNull(alarms.get(0).getDate());
    }

    @Test
    public void runDeferredAlarmRaising() {
        StartupProgressListener mockListener = createStrictMock(StartupProgressListener.class);
        mockListener.onStart(StartupTestSeq.TEST_DEFERRED_ALARM_RAISING);

        mockListener.onNextTask(DeferredAlarmRaisingTestTaskEnum.DEFERRED_RAISE_ALARM);
        mockListener.onTaskFailed(eqAbstractStartupTask(DeferredAlarmRaisingTestTaskEnum.DEFERRED_RAISE_ALARM), contains(ALARM_RAISED_EXCEPTION.getType().name()), notNull());

        mockListener.onStartupFailed(eqStartupTaskInfo(new StartupTaskEnum[]{DeferredAlarmRaisingTestTaskEnum.DEFERRED_RAISE_ALARM}), geq(0L));

        replay(mockListener);

        Boot boot = createClientRunner();
        boot.addStartupProgressListener(mockListener);
        boot.start(StartupTestSeq.TEST_DEFERRED_ALARM_RAISING);
        getStartupTestTaskMonitor().getDeferredStartupTestTask(0).failed(ALARM_RAISED_EXCEPTION);

        verify(mockListener);
        failOnException();

//        List<Alarm> alarms = weldContainer.select(AlarmService.class).get().getAlarms();
//        Assert.assertEquals(alarms.size(), 1);
//        Assert.assertEquals(ALARM_RAISED_EXCEPTION.getId(), alarms.get(0).getId());
//        Assert.assertEquals(ALARM_RAISED_EXCEPTION.getText(), alarms.get(0).getText());
//        Assert.assertEquals(ALARM_RAISED_EXCEPTION.getType(), alarms.get(0).getType());
//        Assert.assertNotNull(alarms.get(0).getDate());
    }

    public static List<StartupTaskInfo> eqStartupTaskInfo(StartupSeq startupSeq) {
        EasyMock.reportMatcher(new StartupTaskInfoEquals(startupSeq));
        return null;
    }

    public static List<StartupTaskInfo> eqStartupTaskInfo(StartupTaskEnum[] taskEnum) {
        EasyMock.reportMatcher(new StartupTaskInfoEquals(taskEnum));
        return null;
    }

    public static class StartupTaskInfoEquals implements IArgumentMatcher {
        private String errorString;
        private StartupTaskEnum[] taskEnum;

        public StartupTaskInfoEquals(StartupSeq startupSeq) {
            taskEnum = startupSeq.getAbstractStartupTaskEnum();
        }

        public StartupTaskInfoEquals(StartupTaskEnum[] taskEnum) {
            this.taskEnum = taskEnum;
        }

        @Override
        public boolean matches(Object o) {
            List<StartupTaskInfo> startupTaskInfo = (List<StartupTaskInfo>) o;
            for (int i = 0, startupTaskInfoSize = startupTaskInfo.size(); i < startupTaskInfoSize; i++) {
                StartupTaskInfo taskInfo = startupTaskInfo.get(i);
                if (taskInfo.getDuration() < 0) {
                    errorString = "Invalid duration: " + taskInfo.getDuration();
                    return false;
                }
                if (taskInfo.getTaskEnum() != taskEnum[i]) {
                    errorString = "Expected task enum: " + taskEnum[i] + " actual task enum: " + taskInfo.getTaskEnum();
                    return false;
                }
            }
            return true;
        }

        @Override
        public void appendTo(StringBuffer stringBuffer) {
            stringBuffer.append(errorString);
        }
    }

    public static AbstractStartupTask eqAbstractStartupTask(StartupTaskEnum taskEnum) {
        EasyMock.reportMatcher(new AbstractStartupTaskEquals(taskEnum));
        return null;
    }

    public static class AbstractStartupTaskEquals implements IArgumentMatcher {
        private StartupTaskEnum taskEnum;
        private String errorString;

        public AbstractStartupTaskEquals(StartupTaskEnum taskEnum) {
            this.taskEnum = taskEnum;
        }

        @Override
        public boolean matches(Object o) {
            AbstractStartupTask abstractStartupTask = (AbstractStartupTask) o;
            if (abstractStartupTask.getDuration() < 0) {
                errorString = "Invalid duration: " + abstractStartupTask.getDuration();
                return false;
            }
            if (abstractStartupTask.getTaskEnum() != taskEnum) {
                errorString = "Expected task enum: " + taskEnum + " actual task enum: " + abstractStartupTask.getTaskEnum();
                return false;
            }
            return true;
        }

        @Override
        public void appendTo(StringBuffer stringBuffer) {
            stringBuffer.append(errorString);
        }
    }

    private Boot createClientRunner() {
        Weld weld = new Weld();
        weldContainer = weld.initialize();
        // return weldContainer.instance().select(Boot.class).get();
        return null;
    }

    public StartupTestTaskMonitor getStartupTestTaskMonitor() {
        // return weldContainer.select(StartupTestTaskMonitor.class).get();
        return null;
    }

    private void failOnException() {
        // weldContainer.select(TestExceptionHandler.class).get().failIfException();
    }

}
