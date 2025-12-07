package com.myorg.muleguard.engine;

import com.myorg.muleguard.model.Check;
import com.myorg.muleguard.model.CheckResult;

public class RuleExecutor {
    public static CheckResult executeCheck(com.myorg.muleguard.checks.AbstractCheck check, java.nio.file.Path root, Check config) {
        return check.execute(root, config);
    }
}