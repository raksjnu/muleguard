package com.myorg.muleguard.checks;

import com.myorg.muleguard.model.Check;
import com.myorg.muleguard.model.CheckResult;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;

import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class RULE_006_PomDependencyAddedCheck extends AbstractCheck {

    @Override
    public CheckResult execute(Path projectRoot, Check check) {
        Path pomPath = projectRoot.resolve("pom.xml");
        if (!Files.exists(pomPath)) {
            return CheckResult.fail("RULE-006", check.getDescription(), "pom.xml not found");
        }

        String groupId = (String) check.getParams().get("groupId");
        String artifactId = (String) check.getParams().get("artifactId");

        try (FileReader reader = new FileReader(pomPath.toFile())) {
            MavenXpp3Reader mavenReader = new MavenXpp3Reader();
            Model model = mavenReader.read(reader);

            boolean found = model.getDependencies().stream()
                .anyMatch(d -> groupId.equals(d.getGroupId()) && artifactId.equals(d.getArtifactId()));

            return found
                ? CheckResult.pass("RULE-006", check.getDescription(),
                    String.format("Required dependency %s:%s is present", groupId, artifactId))
                : CheckResult.fail("RULE-006", check.getDescription(),
                    String.format("Missing required dependency: %s:%s", groupId, artifactId));

        } catch (Exception e) {
            return CheckResult.fail("RULE-006", check.getDescription(),
                "Error parsing pom.xml: " + e.getMessage());
        }
    }
}