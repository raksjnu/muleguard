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

public class RULE_010_UnsupportedApplicationCodeCheck extends AbstractCheck {

	@Override
	public CheckResult execute(Path projectRoot, Check check) {
		String pathPattern = (String) check.getParams().get("path");
		List<String> elements = (List<String>) check.getParams().get("elements");
		List<String> attributes = (List<String>) check.getParams().get("attributes");

		List<String> issues = new ArrayList<>();

		try (Stream<Path> paths = Files.walk(projectRoot)) {
			paths.filter(path -> !Files.isDirectory(path)).filter(path -> matchesPath(path, pathPattern))
					.forEach(path -> {
						try {
							String content = Files.readString(path);
							checkForAttributes(content, path, projectRoot, elements, attributes, issues);
						} catch (Exception e) {

						}
					});
		} catch (Exception e) {
			return CheckResult.fail(check.getRuleId(), check.getDescription(),
					"Error while checking for unsupported application code attributes: " + e.getMessage());
		}

		if (issues.isEmpty()) {
			return CheckResult.pass(check.getRuleId(), check.getDescription(), "No unsupported attributes found.");
		} else {
			return CheckResult.fail(check.getRuleId(), check.getDescription(),
					"Found unsupported from/to ApplicationCode attributes:\n" + String.join("\n", issues));
		}
	}

	private void checkForAttributes(String content, Path filePath, Path projectRoot, List<String> elements, List<String> attributes, List<String> issues) throws IOException {
	
		String elementsPattern = String.join("|", elements);
		String attributesPattern = String.join("|", attributes);
		String regex = String.format("(?si)<[a-zA-Z0-9_-]*:?(?:%s)\\s+[^>]*?(?:%s)\\s*=", elementsPattern, attributesPattern);

		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(content);

		if (matcher.find()) {

			issues.add(String.format("Found unsupported 'fromApplicationCode' or 'toApplicationCode' attribute in a logging element in file %s.",
					projectRoot.relativize(filePath)));
		}
	}

	private boolean matchesPath(Path path, String pattern) {
		return path.getFileSystem().getPathMatcher("glob:**/" + pattern).matches(path);
	}
}