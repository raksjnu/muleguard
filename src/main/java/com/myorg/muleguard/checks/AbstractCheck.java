package com.myorg.muleguard.checks;

import com.myorg.muleguard.model.Check;
import com.myorg.muleguard.model.CheckResult;
import java.nio.file.Path;

public abstract class AbstractCheck {
    public abstract CheckResult execute(Path projectRoot, Check check);

    protected CheckResult pass(String message) {
        return new CheckResult("", "", true, message);
    }

    protected CheckResult fail(String message) {
        return new CheckResult("", "", false, message);
    }
}