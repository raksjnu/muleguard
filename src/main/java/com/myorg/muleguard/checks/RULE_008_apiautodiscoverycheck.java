package com.myorg.muleguard.checks;

import com.myorg.muleguard.model.Check;
import com.myorg.muleguard.model.CheckResult;
import org.dom4j.Document;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RULE_008_apiautodiscoverycheck extends AbstractCheck {

    @Override
    public CheckResult execute(Path projectRoot, Check check) {
        String pathPattern = (String) check.getParams().get("path");
        String xpathExpression = (String) check.getParams().get("xpath");
        String failureMessage = (String) check.getParams().get("failureMessage");

        if (xpathExpression == null || xpathExpression.isBlank() || pathPattern == null || pathPattern.isBlank()) {
            return CheckResult.fail(check.getRuleId(), check.getDescription(), "XPath expression is missing in rule definition.");
        }

        PathMatcher pathMatcher = projectRoot.getFileSystem().getPathMatcher("glob:**/" + pathPattern);

        try (Stream<Path> files = Files.walk(projectRoot)) {
            List<Path> xmlFiles = files
                .filter(path -> !Files.isDirectory(path) && pathMatcher.matches(path))
                .collect(Collectors.toList());

            if (xmlFiles.isEmpty()) {
                 return CheckResult.fail(check.getRuleId(), check.getDescription(), "No files found matching path: " + pathPattern);
            }

            for (Path xmlFile : xmlFiles) {
                try {
                    SAXReader reader = new SAXReader();
                    Document document = reader.read(xmlFile.toFile());
                    List<Node> nodes = document.selectNodes(xpathExpression);

                    if (nodes != null && !nodes.isEmpty()) {

                        return CheckResult.pass(check.getRuleId(), check.getDescription(), "API Autodiscovery element is present.");
                    }
                } catch (Exception e) {

                }
            }


            String finalFailureMessage = (failureMessage != null && !failureMessage.isBlank())
                ? failureMessage
                : "Required XML element not found in any files matching the path.";
            return CheckResult.fail(check.getRuleId(), check.getDescription(), finalFailureMessage);

        } catch (IOException e) {
            return CheckResult.fail(check.getRuleId(), check.getDescription(), "Error scanning project files: " + e.getMessage());
        }
    }
}