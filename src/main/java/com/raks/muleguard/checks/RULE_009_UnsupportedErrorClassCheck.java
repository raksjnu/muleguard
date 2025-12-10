package com.raks.muleguard.checks;

import com.raks.muleguard.model.Check;
import com.raks.muleguard.model.CheckResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class RULE_009_UnsupportedErrorClassCheck extends AbstractCheck {

    @Override
    public CheckResult execute(Path projectRoot, Check check) {
        @SuppressWarnings("unchecked")
        List<String> unsupportedTokens = (List<String>) check.getParams().get("unsupportedTokens");

        if (unsupportedTokens == null || unsupportedTokens.isEmpty()) {
            return CheckResult.fail(check.getRuleId(), check.getDescription(),
                    "No unsupported tokens defined in rule.");
        }

        List<Path> filesToScan = new ArrayList<>();

        Path muleSources = projectRoot.resolve("src/main/mule");
        if (Files.isDirectory(muleSources)) {
            try (Stream<Path> walk = Files.walk(muleSources)) {
                walk.filter(p -> p.toString().toLowerCase().endsWith(".xml"))
                        .forEach(filesToScan::add);
            } catch (IOException e) {
                return CheckResult.fail(check.getRuleId(), check.getDescription(),
                        "Error reading Mule source files: " + e.getMessage());
            }
        }

        Path resources = projectRoot.resolve("src/main/resources");
        if (Files.isDirectory(resources)) {
            try (Stream<Path> walk = Files.walk(resources)) {
                walk.filter(p -> p.toString().toLowerCase().endsWith(".dwl"))
                        .forEach(filesToScan::add);
            } catch (IOException e) {
                return CheckResult.fail(check.getRuleId(), check.getDescription(),
                        "Error reading DataWeave resource files: " + e.getMessage());
            }
        }

        for (Path file : filesToScan) {
            try {
                String content = Files.readString(file);
                for (String token : unsupportedTokens) {
                    if (content.contains(token)) {
                        String failureMessage = String.format(
                                "Unsupported error expression '%s' found in file: %s",
                                token,
                                projectRoot.relativize(file));
                        return CheckResult.fail(check.getRuleId(), check.getDescription(), failureMessage);
                    }
                }
            } catch (IOException e) {

            }
        }

        return CheckResult.pass(check.getRuleId(), check.getDescription(), "No unsupported error expressions found.");
    }
}