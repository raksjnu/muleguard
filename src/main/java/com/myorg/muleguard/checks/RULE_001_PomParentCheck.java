package com.myorg.muleguard.checks;

import com.myorg.muleguard.model.Check;
import com.myorg.muleguard.model.CheckResult;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;

import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;

public class RULE_001_PomParentCheck extends AbstractCheck {

    @Override
    public CheckResult execute(Path projectRoot, Check check) {
        Path pomPath = projectRoot.resolve("pom.xml");
        if (!Files.exists(pomPath)) {
            return CheckResult.fail("RULE-001", check.getDescription(), "pom.xml not found");
        }

        String expectedGroupId = (String) check.getParams().get("groupId");
        String expectedArtifactId = (String) check.getParams().get("artifactId");
        String expectedVersion = (String) check.getParams().get("versionPattern");

        try (FileReader reader = new FileReader(pomPath.toFile())) {
            MavenXpp3Reader mavenReader = new MavenXpp3Reader();
            Model model = mavenReader.read(reader);
            Parent parent = model.getParent();

            if (parent == null) {
                return CheckResult.fail("RULE-001", check.getDescription(), "No <parent> defined in pom.xml");
            }

            boolean groupMatch = expectedGroupId.equals(parent.getGroupId());
            boolean artifactMatch = expectedArtifactId.equals(parent.getArtifactId());

            boolean versionMatch = true;
            if (expectedVersion != null && !expectedVersion.isEmpty()) {
                versionMatch = expectedVersion.equals(parent.getVersion()) ||
                               parent.getVersion().matches(expectedVersion.replace(".", "\\.").replace("*", ".*"));
            }

            boolean passed = groupMatch && artifactMatch && versionMatch;

            String actual = parent.getGroupId() + ":" + parent.getArtifactId() +
                           (parent.getVersion() != null ? ":" + parent.getVersion() : "");

            String message = passed
                ? "Correct parent found: " + actual
                : String.format("Expected %s:%s%s, found %s",
                    expectedGroupId, expectedArtifactId,
                    expectedVersion != null ? ":" + expectedVersion : "",
                    actual);

            return passed
                ? CheckResult.pass("RULE-001", check.getDescription(), message)
                : CheckResult.fail("RULE-001", check.getDescription(), message);

        } catch (Exception e) {
            return CheckResult.fail("RULE-001", check.getDescription(),
                "Error parsing pom.xml: " + e.getMessage());
        }
    }
}