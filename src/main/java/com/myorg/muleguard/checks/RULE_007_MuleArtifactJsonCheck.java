package com.myorg.muleguard.checks;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myorg.muleguard.model.Check;
import com.myorg.muleguard.model.CheckResult;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RULE_007_MuleArtifactJsonCheck extends AbstractCheck {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public CheckResult execute(Path projectRoot, Check check) {

        Path jsonPath = projectRoot.resolve("mule-artifact.json");
        if (!Files.exists(jsonPath)) {
            return CheckResult.fail("RULE-007", check.getDescription(),
                "mule-artifact.json not found in project root");
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> params = (Map<String, Object>) check.getParams();

        try {
            JsonNode root = mapper.readTree(jsonPath.toFile());
            List<String> failures = new ArrayList<>();

            @SuppressWarnings("unchecked")
            Map<String, String> requiredStrings = (Map<String, String>) params.getOrDefault("requiredStrings", Map.of());
            requiredStrings.forEach((key, expected) -> {
                JsonNode node = root.path(key);
                if (node.isMissingNode() || !expected.equals(node.asText())) {
                    failures.add(String.format("%s must be '%s' (found: %s)",
                        key, expected, node.isMissingNode() ? "missing" : "'" + node.asText() + "'"));
                }
            });

            @SuppressWarnings("unchecked")
            Map<String, String> minVersions = (Map<String, String>) params.getOrDefault("minVersions", Map.of());
            minVersions.forEach((key, minVersion) -> {
                JsonNode node = root.path(key);
                String actual = node.asText(""); 

                if (node.isMissingNode()) { 
                    failures.add(String.format("Required field '%s' is missing (expected >= %s)", key, minVersion));
                } else if (!isVersionAtLeast(actual, minVersion)) {
                    failures.add(String.format("Field '%s' must be at least '%s' (found: '%s')", key, minVersion, actual));
                }
            });

            @SuppressWarnings("unchecked")
            List<String> requiredElements = (List<String>) params.getOrDefault("requiredElements", List.of());
            requiredElements.forEach(key -> {
                if (root.path(key).isMissingNode()) {
                    failures.add(key + " element is missing");
                }
            });

            if (failures.isEmpty()) {
                return CheckResult.pass("RULE-007", check.getDescription(),
                    "mule-artifact.json is fully compliant");
            } else {
                return CheckResult.fail("RULE-007", check.getDescription(),
                    "mule-artifact.json validation failed:\n• " + String.join("\n• ", failures));
            }

        } catch (Exception e) {
            return CheckResult.fail("RULE-007", check.getDescription(),
                "Error parsing mule-artifact.json: " + e.getMessage());
        }
    }

    private boolean isVersionAtLeast(String actual, String minimum) {
        try {
            String[] a = actual.split("\\.");
            String[] m = minimum.split("\\.");

            for (int i = 0; i < Math.max(a.length, m.length); i++) {
                int av = i < a.length ? Integer.parseInt(a[i].replaceAll("[^0-9].*", "")) : 0;
                int mv = i < m.length ? Integer.parseInt(m[i].replaceAll("[^0-9].*", "")) : 0;
                if (av > mv) return true;
                if (av < mv) return false;
            }
            return true; // equal
        } catch (Exception e) {
            return false; // fallback
        }
    }
}