package com.raks.muleguard.checks;

import com.raks.muleguard.model.Check;
import com.raks.muleguard.model.CheckResult;
import java.nio.file.Path;
import java.util.List;

public abstract class AbstractCheck {
    public abstract CheckResult execute(Path projectRoot, Check check);

    protected CheckResult pass(String message) {
        return new CheckResult("", "", true, message);
    }

    protected CheckResult fail(String message) {
        return new CheckResult("", "", false, message);
    }

    /**
     * Resolves environment list from check parameters.
     * If environments contains "ALL", returns the environments list as-is
     * (the global list is already injected by MuleGuardMain).
     * 
     * @param check The check containing environment parameters
     * @return List of environments to process
     */
    protected List<String> resolveEnvironments(Check check) {
        @SuppressWarnings("unchecked")
        List<String> environments = (List<String>) check.getParams().get("environments");

        // Note: If environments contains "ALL", MuleGuardMain has already replaced it
        // with the global environment list, so we just return it as-is
        return environments;
    }
}