package com.myorg.muleguard.checks;

import com.myorg.muleguard.model.Check;
import com.myorg.muleguard.model.CheckResult;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;

import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class RULE_004_PomDependencyRemovedCheck extends AbstractCheck {

    @Override
    public CheckResult execute(Path projectRoot, Check check) {
        Path pomPath = projectRoot.resolve("pom.xml");
        if (!Files.exists(pomPath)) {
            return CheckResult.fail("RULE-004", check.getDescription(), "pom.xml not found");
        }

        @SuppressWarnings("unchecked")
        List<String> forbiddenDeps = (List<String>) check.getParams().get("dependencies");
        if (forbiddenDeps == null || forbiddenDeps.isEmpty()) {
            return CheckResult.pass("RULE-004", check.getDescription(), "No dependencies to check");
        }

        try (FileReader reader = new FileReader(pomPath.toFile())) {
            MavenXpp3Reader mavenReader = new MavenXpp3Reader();
            Model model = mavenReader.read(reader);

            StringBuilder failures = new StringBuilder();

            
            if (model.getDependencies() != null) {
                for (Dependency dep : model.getDependencies()) {
                    String coord = dep.getGroupId() + ":" + dep.getArtifactId();
                    if (forbiddenDeps.contains(coord)) {
                        failures.append("• ").append(coord);
                        if (dep.getVersion() != null) {
                            failures.append(" (").append(dep.getVersion()).append(")");
                        }
                        failures.append("\n");
                    }
                }
            }

            
            DependencyManagement depMgmt = model.getDependencyManagement();
            if (depMgmt != null && depMgmt.getDependencies() != null) {
                for (Dependency dep : depMgmt.getDependencies()) {
                    String coord = dep.getGroupId() + ":" + dep.getArtifactId();
                    if (forbiddenDeps.contains(coord)) {
                        failures.append("• ").append(coord)
                                .append(" (in dependencyManagement)\n");
                    }
                }
            }

            if (failures.length() == 0) {
                return CheckResult.pass("RULE-004", check.getDescription(),
                    "Forbidden dependencies successfully removed");
            } else {
                return CheckResult.fail("RULE-004", check.getDescription(),
                    "Found forbidden dependencies:\n" + failures.toString().trim());
            }

        } catch (Exception e) {
            return CheckResult.fail("RULE-004", check.getDescription(),
                "Error parsing pom.xml: " + e.getMessage());
        }
    }
}