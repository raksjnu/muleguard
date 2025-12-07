package com.myorg.muleguard.checks;

import com.myorg.muleguard.model.Check;
import com.myorg.muleguard.model.CheckResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class RULE_102_ConfigPolicyCheck extends AbstractCheck {

    @Override
    public CheckResult execute(Path projectRoot, Check check) {
        @SuppressWarnings("unchecked")
        List<String> propertyNames = (List<String>) check.getParams().get("propertyNames");
        @SuppressWarnings("unchecked")
        List<String> fileExtensions = (List<String>) check.getParams().get("fileExtensions");
        @SuppressWarnings("unchecked")
        List<String> environments = (List<String>) check.getParams().get("environments");

        if (propertyNames == null || propertyNames.isEmpty() || fileExtensions == null || fileExtensions.isEmpty() || environments == null || environments.isEmpty()) {
            return CheckResult.fail(check.getRuleId(), check.getDescription(), "Rule is not configured correctly. Missing 'propertyNames', 'fileExtensions', or 'environments'.");
        }

        List<String> failures = new ArrayList<>();

        try (Stream<Path> paths = Files.walk(projectRoot)) {
            paths.filter(Files::isRegularFile)
                .filter(path -> {
                    String fileName = path.getFileName().toString();
                    String fileBaseName = fileName.contains(".") ? fileName.substring(0, fileName.lastIndexOf('.')) : fileName;
                    String extension = fileName.contains(".") ? fileName.substring(fileName.lastIndexOf('.')) : "";
                    return environments.contains(fileBaseName) && fileExtensions.contains(extension);
                })
                .forEach(file -> {
                    try {
                        String content = Files.readString(file);
                        for (String propertyName : propertyNames) {
                            if (!content.contains(propertyName)) {
                                failures.add(String.format("Property '%s' not found in file: %s", propertyName, projectRoot.relativize(file)));
                            }
                        }
                    } catch (IOException e) {
                        failures.add("Could not read file: " + projectRoot.relativize(file));
                    }
                });
        } catch (IOException e) {
            return CheckResult.fail(check.getRuleId(), check.getDescription(), "Error scanning files: " + e.getMessage());
        }

        return failures.isEmpty() ? CheckResult.pass(check.getRuleId(), check.getDescription(), "Required properties found in all relevant policy files.") : CheckResult.fail(check.getRuleId(), check.getDescription(), "Missing required properties in files:\n• " + String.join("\n• ", failures));
    }
}