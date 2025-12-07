package com.myorg.muleguard.checks;

import com.myorg.muleguard.model.Check;
import com.myorg.muleguard.model.CheckResult;
import com.myorg.muleguard.PropertyResolver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class RULE_005_ibmmqciphervaluecheck extends AbstractCheck {

    @Override
    public CheckResult execute(Path projectRoot, Check check) {
        String expectedValue = (String) check.getParams().get("expectedValue");

        if (expectedValue == null || expectedValue.isBlank()) {
            return CheckResult.fail(check.getRuleId(), check.getDescription(), "Expected value is missing in rule definition.");
        }

        Path muleSources = projectRoot.resolve("src/main/mule");
        if (!Files.isDirectory(muleSources)) {
            return CheckResult.pass(check.getRuleId(), check.getDescription(), "No Mule configuration files found in src/main/mule.");
        }

        PropertyResolver propertyResolver = new PropertyResolver(projectRoot);
        List<String> failures = new ArrayList<>();
        AtomicBoolean mqConfigFoundInAnyFile = new AtomicBoolean(false);

        try (Stream<Path> files = Files.walk(muleSources)) {
            files.filter(file -> file.toString().toLowerCase().endsWith(".xml"))
                .forEach(xmlFile -> {
                    try {
                        String content = Files.readString(xmlFile);
                        
                        Pattern configPattern = Pattern.compile("<ibm-mq:config", Pattern.CASE_INSENSITIVE);
                        if (configPattern.matcher(content).find()) {
                            mqConfigFoundInAnyFile.set(true);
                            validateCipherSuite(content, xmlFile.getFileName().toString(), expectedValue, propertyResolver, failures);
                        }
                    } catch (IOException e) {
                        failures.add(String.format("FAIL: Could not read file %s. Error: %s", xmlFile.getFileName(), e.getMessage()));
                    }
                });

        } catch (IOException e) {
            return CheckResult.fail(check.getRuleId(), check.getDescription(), "Error reading Mule configuration files: " + e.getMessage());
        }

        
        if (!mqConfigFoundInAnyFile.get()) {
            return CheckResult.pass(check.getRuleId(), check.getDescription(), "No IBM MQ configuration found in any file.");
        }

        if (failures.isEmpty()) {
            return CheckResult.pass(check.getRuleId(), check.getDescription(), "IBM MQ cipherSuite is correctly configured in all relevant files.");
        } else {
            return CheckResult.fail(check.getRuleId(), check.getDescription(), String.join("\n", failures));
        }
    }

    private void validateCipherSuite(String content, String fileName, String expectedValue, PropertyResolver propertyResolver, List<String> failures) {
       
        String regex = "<ibm-mq:connection[^>]*?cipherSuite\\s*=\\s*\"([^\"]*)\"";
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(content);

        if (!matcher.find()) {
           
            failures.add(String.format("FAIL: The 'cipherSuite' attribute is missing in <ibm-mq:connection> in file %s.", fileName));
            return;
        }

        matcher.reset();
        while (matcher.find()) {
            String actualValue = matcher.group(1);
            String resolvedValue = propertyResolver.resolve(actualValue);

            if (!expectedValue.equals(resolvedValue)) {
                failures.add(String.format("FAIL: Incorrect 'cipherSuite' value in %s. Found: \"%s\", Expected: \"%s\".", fileName, resolvedValue, expectedValue));
            }
        }
    }
}