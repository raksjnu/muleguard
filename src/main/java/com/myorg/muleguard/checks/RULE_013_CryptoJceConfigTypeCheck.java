package com.myorg.muleguard.checks;

import com.myorg.muleguard.model.Check;
import com.myorg.muleguard.model.CheckResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class RULE_013_CryptoJceConfigTypeCheck extends AbstractCheck {

    @Override
    public CheckResult execute(Path projectRoot, Check check) {
        String pathPattern = (String) check.getParams().get("path");
        String elementName = (String) check.getParams().get("elementName");
        String requiredAttribute = (String) check.getParams().get("requiredAttribute");
        List<String> failures = new ArrayList<>();
        AtomicBoolean elementFoundInAnyFile = new AtomicBoolean(false);

        String regex = String.format("(?i)<%s(?!.*\\b%s\\s*=)[^>]*>", Pattern.quote(elementName), Pattern.quote(requiredAttribute));
        Pattern violationPattern = Pattern.compile(regex);
        Pattern elementExistsPattern = Pattern.compile(String.format("<%s", Pattern.quote(elementName)));

        try (Stream<Path> paths = Files.walk(projectRoot)) {
            paths.filter(path -> !Files.isDirectory(path))
                .filter(path -> path.getFileSystem().getPathMatcher("glob:**/" + pathPattern).matches(path))
                .forEach(file -> {
                    try {
                        String content = Files.readString(file);
                        if (elementExistsPattern.matcher(content).find()) {
                            elementFoundInAnyFile.set(true);
                            Matcher violationMatcher = violationPattern.matcher(content);
                            if (violationMatcher.find()) {
                                failures.add(String.format("FAIL: Found <%s> element without required '%s' attribute in file %s.",
                                    elementName, requiredAttribute, projectRoot.relativize(file)));
                            }
                        }
                    } catch (IOException e) {
                        failures.add("Could not read file: " + file.toString());
                    }
                });
        } catch (IOException e) {
            return CheckResult.fail(check.getRuleId(), check.getDescription(), "Error scanning files: " + e.getMessage());
        }

        if (!elementFoundInAnyFile.get() || failures.isEmpty()) {
            return CheckResult.pass(check.getRuleId(), check.getDescription(), "No crypto:jce-config without 'type' attribute found.");
        } else {
            return CheckResult.fail(check.getRuleId(), check.getDescription(), String.join("\n", failures));
        }
    }
}