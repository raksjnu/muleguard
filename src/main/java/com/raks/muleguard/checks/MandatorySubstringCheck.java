package com.raks.muleguard.checks;

import com.raks.muleguard.model.Check;
import com.raks.muleguard.model.CheckResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Mandatory substring check for configuration files.
 * Validates that required tokens exist as exact substrings in specified files.
 * 
 * Supports:
 * - Configurable case sensitivity
 * - Multiple file extensions (.properties, .policy, etc.)
 * - Environment-specific file filtering
 * - Exact substring matching (no leading/trailing spaces)
 * 
 * @author Rakesh Kumar (raksjnu@gmail.com)
 */
public class MandatorySubstringCheck extends AbstractCheck {

    @Override
    public CheckResult execute(Path projectRoot, Check check) {
        List<String> fileExtensions = (List<String>) check.getParams().get("fileExtensions");
        @SuppressWarnings("unchecked")
        List<String> tokens = (List<String>) check.getParams().get("tokens");
        List<String> environments = resolveEnvironments(check);

        // Default to case-sensitive if not specified
        boolean caseSensitive = (Boolean) check.getParams().getOrDefault("caseSensitive", true);

        // Validation
        if (fileExtensions == null || fileExtensions.isEmpty()) {
            return CheckResult.fail(check.getRuleId(), check.getDescription(),
                    "Configuration error: 'fileExtensions' parameter is required");
        }

        if (tokens == null || tokens.isEmpty()) {
            return CheckResult.fail(check.getRuleId(), check.getDescription(),
                    "Configuration error: 'tokens' parameter is required");
        }

        if (environments == null || environments.isEmpty()) {
            return CheckResult.fail(check.getRuleId(), check.getDescription(),
                    "Configuration error: 'environments' parameter is required");
        }

        List<String> failures = new ArrayList<>();

        try (Stream<Path> paths = Files.walk(projectRoot)) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> matchesEnvironmentFile(path, environments, fileExtensions))
                    .forEach(file -> {
                        validateTokensInFile(file, tokens, caseSensitive, projectRoot, failures);
                    });

        } catch (IOException e) {
            return CheckResult.fail(check.getRuleId(), check.getDescription(),
                    "Error scanning files: " + e.getMessage());
        }

        if (failures.isEmpty()) {
            return CheckResult.pass(check.getRuleId(), check.getDescription(),
                    String.format("All required tokens found in environment files (case-sensitive: %s)",
                            caseSensitive));
        } else {
            return CheckResult.fail(check.getRuleId(), check.getDescription(),
                    "Validation failures:\n• " + String.join("\n• ", failures));
        }
    }

    /**
     * Check if file matches environment and extension criteria
     */
    private boolean matchesEnvironmentFile(Path path, List<String> environments, List<String> fileExtensions) {
        String fileName = path.getFileName().toString();
        String fileBaseName = fileName.contains(".") ? fileName.substring(0, fileName.lastIndexOf('.')) : fileName;
        String extension = fileName.contains(".") ? fileName.substring(fileName.lastIndexOf('.')) : "";

        return environments.contains(fileBaseName) && fileExtensions.contains(extension);
    }

    /**
     * Validate that all required tokens exist in the file
     */
    private void validateTokensInFile(Path file, List<String> tokens, boolean caseSensitive,
            Path projectRoot, List<String> failures) {
        try {
            String content = Files.readString(file);

            for (String token : tokens) {
                boolean found = containsToken(content, token, caseSensitive);

                if (!found) {
                    failures.add(String.format(
                            "Token '%s' not found in file: %s (case-sensitive: %s)",
                            token, projectRoot.relativize(file), caseSensitive));
                }
            }
        } catch (IOException e) {
            failures.add(String.format("Could not read file: %s (Error: %s)",
                    projectRoot.relativize(file), e.getMessage()));
        }
    }

    /**
     * Check if content contains token with configurable case sensitivity
     */
    private boolean containsToken(String content, String token, boolean caseSensitive) {
        if (caseSensitive) {
            return content.contains(token);
        } else {
            return content.toLowerCase().contains(token.toLowerCase());
        }
    }
}
