package io.ribot.app.util;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import io.ribot.app.injection.TestComponentRule;

/**
 * Test rule that given a TestComponentRule clears the local data such as databases or
 * shared preferences
 */
public class ClearDataRule implements TestRule {

    private TestComponentRule mTestComponentRule;

    public ClearDataRule(TestComponentRule testComponentRule) {
        mTestComponentRule = testComponentRule;
    }

    public void clearData() {
        mTestComponentRule.getDatabaseHelper().clearTables();
        mTestComponentRule.getPreferencesHelper().clear();
    }

    @Override
    public Statement apply(final Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                try {
                    clearData();
                    base.evaluate();
                } finally {
                    mTestComponentRule = null;
                }
            }
        };
    }
}
