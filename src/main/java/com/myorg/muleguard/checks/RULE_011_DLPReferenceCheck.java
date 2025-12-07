package com.myorg.muleguard.checks;

import com.myorg.muleguard.model.Check;
import com.myorg.muleguard.model.CheckResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class RULE_011_DLPReferenceCheck extends AbstractCheck {

    @Override
    public CheckResult execute(Path projectRoot, Check check) {
        List<String> filesToScan = (List<String>) check.getParams().get("files");
        List<String> tokensToFind = (List<String>) check.getParams().get("tokens");
        List<String> failures = new ArrayList<>();

        for (String filePathPattern : filesToScan) {
            try (Stream<Path> paths = Files.walk(projectRoot)) {
                paths.filter(path -> !Files.isDirectory(path))
                    .filter(path -> path.getFileSystem().getPathMatcher("glob:**/src/main/" + filePathPattern).matches(path))
                    .forEach(file -> {
                        try {
                            String content = Files.readString(file);
                            for (String token : tokensToFind) {
                                if (content.contains(token)) {
                                    failures.add(String.format("DLP reference found in %s (token: %s)",
                                        projectRoot.relativize(file), token));
                                }
                            }
                        } catch (IOException e) {
                            failures.add("Could not read file: " + file.toString());
                        }
                    });
            } catch (IOException e) {
                return CheckResult.fail(check.getRuleId(), check.getDescription(), "Error scanning files: " + e.getMessage());
            }
        }

        if (failures.isEmpty()) {
            return CheckResult.pass(check.getRuleId(), check.getDescription(), "No DLP references found.");
        } else {
            return CheckResult.fail(check.getRuleId(), check.getDescription(), String.join("\n", failures));
        }
    }
}