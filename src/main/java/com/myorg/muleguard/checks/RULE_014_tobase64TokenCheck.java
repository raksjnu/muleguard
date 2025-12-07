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

public class RULE_014_tobase64TokenCheck extends AbstractCheck {

    @Override
    public CheckResult execute(Path projectRoot, Check check) {
        String elementName = (String) check.getParams().get("elementName");
        String forbiddenToken = (String) check.getParams().get("forbiddenToken");

        if (elementName == null || forbiddenToken == null) {
            return CheckResult.fail(check.getRuleId(), check.getDescription(), "Rule is not configured correctly. Missing 'elementName' or 'forbiddenToken'.");
        }

        List<String> failures = new ArrayList<>();
        Path muleSources = projectRoot.resolve("src/main/mule");

        if (!Files.isDirectory(muleSources)) {
            return CheckResult.pass(check.getRuleId(), check.getDescription(), "No Mule source files found in src/main/mule.");
        }

        String regex = String.format("(?s)<(?:[a-zA-Z0-9-]+:)?%s\\b[^>]*?%s[^>]*?>",
            Pattern.quote(elementName), Pattern.quote(forbiddenToken));
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);

        try (Stream<Path> paths = Files.walk(muleSources)) {
            paths.filter(path -> path.toString().toLowerCase().endsWith(".xml"))
                .forEach(file -> {
                    try {
                        String content = Files.readString(file);
                        Matcher matcher = pattern.matcher(content);
                        if (matcher.find()) {
                            failures.add(String.format("Forbidden token '%s' found in <%s> element in file: %s",
                                forbiddenToken, elementName, projectRoot.relativize(file)));
                        }
                    } catch (IOException e) {
                     
                    }
                });
        } catch (IOException e) {
            return CheckResult.fail(check.getRuleId(), check.getDescription(), "Error scanning files: " + e.getMessage());
        }

        if (failures.isEmpty()) {
            return CheckResult.pass(check.getRuleId(), check.getDescription(),
                String.format("No forbidden token '%s' found in any <%s> elements.", forbiddenToken, elementName));
        } else {
            return CheckResult.fail(check.getRuleId(), check.getDescription(),
                "Found forbidden token in element:\n• " + String.join("\n• ", failures));
        }
    }
}