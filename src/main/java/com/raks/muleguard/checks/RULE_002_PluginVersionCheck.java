package com.raks.muleguard.checks;

import com.raks.muleguard.model.Check;
import com.raks.muleguard.model.CheckResult;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.Xpp3Dom;

import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;

public class RULE_002_PluginVersionCheck extends AbstractCheck {
    @Override
    public CheckResult execute(Path projectRoot, Check check) {
        Path pomPath = projectRoot.resolve("pom.xml");
        if (!Files.exists(pomPath)) {
            return CheckResult.fail("RULE-002", check.getDescription(), "pom.xml not found");
        }

        String propertyName = (String) check.getParams().get("property");
        String expectedValue = (String) check.getParams().get("expectedValue");

        try (FileReader reader = new FileReader(pomPath.toFile())) {
            MavenXpp3Reader mavenReader = new MavenXpp3Reader();
            Model model = mavenReader.read(reader);

            String actualValue = null;

            if (model.getProperties() != null) {
                actualValue = model.getProperties().getProperty(propertyName);
            }

            if (actualValue == null && model.getBuild() != null && model.getBuild().getPlugins() != null) {
                for (Plugin plugin : model.getBuild().getPlugins()) {
                    if ("org.mule.tools.maven".equals(plugin.getGroupId()) &&
                            "mule-maven-plugin".equals(plugin.getArtifactId())) {

                        if (plugin.getConfiguration() instanceof Xpp3Dom configDom) {
                            Xpp3Dom versionNode = configDom.getChild("version");
                            if (versionNode != null) {
                                actualValue = versionNode.getValue();
                            }
                        }
                        break;
                    }
                }
            }

            if (actualValue == null) {
                return CheckResult.fail("RULE-002", check.getDescription(),
                        String.format("Property '%s' not found in pom.xml", propertyName));
            }

            actualValue = actualValue.trim();
            boolean passed = expectedValue.equals(actualValue);

            String message = passed
                    ? String.format("Property %s = %s (correct)", propertyName, actualValue)
                    : String.format("Expected %s=%s, but found %s", propertyName, expectedValue, actualValue);

            return passed
                    ? CheckResult.pass("RULE-002", check.getDescription(), message)
                    : CheckResult.fail("RULE-002", check.getDescription(), message);

        } catch (Exception e) {
            return CheckResult.fail("RULE-002", check.getDescription(),
                    "Error reading pom.xml: " + e.getMessage());
        }
    }
}