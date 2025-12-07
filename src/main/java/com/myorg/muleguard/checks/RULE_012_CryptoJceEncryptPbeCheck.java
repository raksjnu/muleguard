package com.myorg.muleguard.checks;

import com.myorg.muleguard.model.Check;
import com.myorg.muleguard.model.CheckResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class RULE_012_CryptoJceEncryptPbeCheck extends AbstractCheck {

    @Override
    public CheckResult execute(Path projectRoot, Check check) {
        String pathPattern = (String) check.getParams().get("path");
        String elementName = (String) check.getParams().get("elementName");
        String forbiddenValue = (String) check.getParams().get("forbiddenValue");
        List<String> failures = new ArrayList<>();

        String regex = String.format("(?i)<%s[^>]*?%s[^>]*?>", Pattern.quote(elementName), Pattern.quote(forbiddenValue));
        Pattern pattern = Pattern.compile(regex);

        try (Stream<Path> paths = Files.walk(projectRoot)) {
            paths.filter(path -> !Files.isDirectory(path))
                .filter(path -> path.getFileSystem().getPathMatcher("glob:**/" + pathPattern).matches(path))
                .forEach(file -> {
                    try {
                        String content = Files.readString(file);
                        Matcher matcher = pattern.matcher(content);
                        if (matcher.find()) {
                            failures.add(String.format("FAIL: Found forbidden value '%s' in <%s> element in file %s.",
                                forbiddenValue, elementName, projectRoot.relativize(file)));
                        }
                    } catch (IOException e) {
                        failures.add("Could not read file: " + file.toString());
                    }
                });
        } catch (IOException e) {
            return CheckResult.fail(check.getRuleId(), check.getDescription(), "Error scanning files: " + e.getMessage());
        }

        if (failures.isEmpty()) {
            return CheckResult.pass(check.getRuleId(), check.getDescription(), "No legacy JCE PBE encryption found.");
        } else {
            return CheckResult.fail(check.getRuleId(), check.getDescription(), String.join("\n", failures));
        }
    }
}