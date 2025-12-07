package com.myorg.muleguard.checks;

import com.myorg.muleguard.model.Check;
import com.myorg.muleguard.model.CheckResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;

public class RULE_101_ConfigPropertyCheck extends AbstractCheck {

    @Override
    public CheckResult execute(Path projectRoot, Check check) {
        @SuppressWarnings("unchecked")
        List<String> propertyNames = (List<String>) check.getParams().get("propertyNames");
        @SuppressWarnings("unchecked")
        List<String> fileExtensions = (List<String>) check.getParams().get("fileExtensions");
        @SuppressWarnings("unchecked")
        List<String> environments = (List<String>) check.getParams().get("environments");

        if (propertyNames == null || propertyNames.isEmpty() || fileExtensions == null || fileExtensions.isEmpty() || environments == null || environments.isEmpty()) {
            return CheckResult.fail(check.getRuleId(), check.getDescription(), "Rule is not configured correctly. Missing 'propertyName', 'fileExtensions', or 'environments'.");
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
                         Properties props = new Properties();
                         props.load(Files.newInputStream(file));

                         for (String propertyName : propertyNames) {
                             if (!props.containsKey(propertyName)) {
                                 failures.add(String.format("Property '%s' not found in file: %s",
                                         propertyName, projectRoot.relativize(file)));
                             }
                         }

                     } catch (IOException e) {

                     }
                 });
        } catch (IOException e) {
            return CheckResult.fail(check.getRuleId(), check.getDescription(), "Error scanning files: " + e.getMessage());
        }

        if (failures.isEmpty()) {
            return CheckResult.pass(check.getRuleId(), check.getDescription(),
                String.format("Required properties (%s) were found in all relevant configuration files.", String.join(", ", propertyNames)));
        } else {
            return CheckResult.fail(check.getRuleId(), check.getDescription(),
                "Missing required properties in files:\n• " + String.join("\n• ", failures));
        }
    }
}