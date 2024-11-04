package com.btxtech.shared.gameengine.planet.gui.userobject;

import com.btxtech.shared.datatypes.DecimalPosition;

import java.util.function.BiConsumer;

/**
 * Created by Beat
 * on 15.12.2017.
 */
public class TestCaseGenerator {
    private BiConsumer<DecimalPosition, Body> testGeneratorCallback;
    private String testCaseName = "testCase";
    private String throwsString;

    public TestCaseGenerator setTestGeneratorCallback(BiConsumer<DecimalPosition, Body> testGeneratorCallback) {
        this.testGeneratorCallback = testGeneratorCallback;
        return this;
    }

    public TestCaseGenerator setTestCaseName(String testCaseName) {
        this.testCaseName = testCaseName;
        return this;
    }

    public TestCaseGenerator setThrowsString(String throwsString) {
        this.throwsString = throwsString;
        return this;
    }

    public void onTestGenerationButton(DecimalPosition position) {
        Body body = new Body("        ");
        if (testGeneratorCallback != null) {
            testGeneratorCallback.accept(position, body);
        }
        String throwsStringCode = "";
        if (throwsString != null) {
            throwsStringCode = "throws " + throwsString + " ";
        }
        System.out.println("    @Test");
        System.out.println("    public void " + testCaseName + "() " + throwsStringCode + "{");
        System.out.print(body.getCode());
        System.out.println("    }");
    }

    public class Body {
        private String code = "";
        private String indent;

        public Body(String indent) {
            this.indent = indent;
        }

        public void appendLine(String line) {
            code = code + indent + line + "\n";
        }

        public String getCode() {
            return code;
        }
    }
}
