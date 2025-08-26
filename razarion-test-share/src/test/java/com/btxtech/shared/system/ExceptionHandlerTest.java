package com.btxtech.shared.system;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class ExceptionHandlerTest {

    @Test
    public void handleExceptionOnlyOnce1() {
        Exception ex1 = new Exception();
        Exception ex2 = new Exception();

        List<String> messages = new ArrayList<>();
        ExceptionHandler.handleExceptionOnlyOnce(messages::add, ex1.getMessage(), ex1);
        ExceptionHandler.handleExceptionOnlyOnce(messages::add, ex2.getMessage(), ex2);

        Assert.assertEquals("!!Further exception will be suppressed!! null", messages.get(0));
        Assert.assertEquals("!!Further exception will be suppressed!! null", messages.get(1));
    }

    @Test
    public void handleExceptionOnlyOnce2() {
        Exception[] exceptions = generateExceptions("X1", "X1");

        List<String> messages = new ArrayList<>();
        ExceptionHandler.handleExceptionOnlyOnce(messages::add, exceptions[0].getMessage(), exceptions[0]);
        ExceptionHandler.handleExceptionOnlyOnce(messages::add, exceptions[1].getMessage(), exceptions[1]);

        Assert.assertEquals("!!Further exception will be suppressed!! X1", messages.get(0));
        Assert.assertEquals(1, messages.size());
    }

    @Test
    public void handleExceptionOnlyOnce3() {
        Exception[] exceptions = generateExceptions("X1", "X2");

        List<String> messages = new ArrayList<>();
        ExceptionHandler.handleExceptionOnlyOnce(messages::add, exceptions[0].getMessage(), exceptions[0]);
        ExceptionHandler.handleExceptionOnlyOnce(messages::add, exceptions[1].getMessage(), exceptions[1]);

        Assert.assertEquals("!!Further exception will be suppressed!! X1", messages.get(0));
        Assert.assertEquals("!!Further exception will be suppressed!! X2", messages.get(1));
    }

    @Test
    public void handleExceptionOnlyOnceInner() {
        Exception[] exceptions = generateInnerExceptions("X1", "X1");

        List<String> messages = new ArrayList<>();
        ExceptionHandler.handleExceptionOnlyOnce(messages::add, exceptions[0].getMessage(), exceptions[0]);
        ExceptionHandler.handleExceptionOnlyOnce(messages::add, exceptions[1].getMessage(), exceptions[1]);

        Assert.assertEquals("!!Further exception will be suppressed!! java.lang.Exception: java.lang.Exception: X1", messages.get(0));
        Assert.assertEquals(1, messages.size());
    }

    @Test
    public void handleExceptionOnlyOnceMessage1() throws Exception {
        Exception ex1 = new Exception();
        Exception ex2 = new Exception();

        List<String> messages = new ArrayList<>();
        ExceptionHandler.handleExceptionOnlyOnce(messages::add, "xx1", ex1);
        ExceptionHandler.handleExceptionOnlyOnce(messages::add, "xx2", ex2);

        Assert.assertEquals("!!Further exception will be suppressed!! xx1", messages.get(0));
        Assert.assertEquals("!!Further exception will be suppressed!! xx2", messages.get(1));
    }

    @Test
    public void handleExceptionOnlyOnceMessage2() throws Exception {
        Exception[] exceptions = generateExceptions("X1", "X1");

        List<String> messages = new ArrayList<>();
        ExceptionHandler.handleExceptionOnlyOnce(messages::add, "m1", exceptions[0]);
        ExceptionHandler.handleExceptionOnlyOnce(messages::add, "m1", exceptions[1]);

        Assert.assertEquals("!!Further exception will be suppressed!! m1", messages.get(0));
        Assert.assertEquals(1, messages.size());
    }

    @Test
    public void handleExceptionOnlyOnceMessage3() throws Exception {
        Exception[] exceptions = generateExceptions("X1", "X1");

        List<String> messages = new ArrayList<>();
        ExceptionHandler.handleExceptionOnlyOnce(messages::add, "m1", exceptions[0]);
        ExceptionHandler.handleExceptionOnlyOnce(messages::add, "m2", exceptions[1]);

        Assert.assertEquals("!!Further exception will be suppressed!! m1", messages.get(0));
        Assert.assertEquals("!!Further exception will be suppressed!! m2", messages.get(1));
    }

    private Exception[] generateExceptions(String message1, String message2) {
        Exception[] result = new Exception[2];

        for (int i = 0; i < result.length; i++) {
            result[i] = new Exception(i == 0 ? message1 : message2);
        }

        return result;
    }

    private Exception[] generateInnerExceptions(String message1, String message2) {
        Exception[] result = new Exception[2];

        for (int i = 0; i < result.length; i++) {
            result[i] = new Exception(new Exception(new Exception(i == 0 ? message1 : message2)));
        }

        return result;
    }

}
