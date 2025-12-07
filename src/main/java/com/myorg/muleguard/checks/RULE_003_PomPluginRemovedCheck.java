package com.myorg.muleguard.checks;

import com.myorg.muleguard.model.Check;
import com.myorg.muleguard.model.CheckResult;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;

import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.List;

public class RULE_003_PomPluginRemovedCheck extends AbstractCheck {

    @Override
    public CheckResult execute(Path projectRoot, Check check) {
        Path pomPath = projectRoot.resolve("pom.xml");
        if (!Files.exists(pomPath)) {
            return CheckResult.fail("RULE-003", check.getDescription(), "pom.xml not found");
        }

        @SuppressWarnings("unchecked")
        List<String> forbiddenPlugins = (List<String>) check.getParams().get("plugins");

        try (FileReader reader = new FileReader(pomPath.toFile())) {
            MavenXpp3Reader mavenReader = new MavenXpp3Reader();
            Model model = mavenReader.read(reader);

            Properties modelProperties = model.getProperties();

            StringBuilder found = new StringBuilder();

            if (model.getBuild() != null && model.getBuild().getPlugins() != null) {
                for (Plugin plugin : model.getBuild().getPlugins()) {
                    String ga = plugin.getGroupId() + ":" + plugin.getArtifactId();
                    String gav = ga;
                    String version = plugin.getVersion();
                    if (version != null) {
                        if (version.startsWith("${") && version.endsWith("}")) {
                            String propName = version.substring(2, version.length() - 1);
                            version = modelProperties.getProperty(propName, version); 
                        }
                        gav += ":" + version;
                    }


                    
                    if (forbiddenPlugins.contains(ga) || forbiddenPlugins.contains(gav)) {
                        
                        found.append("• ").append(gav).append("\n");
                    }
                }
            }

            if (found.length() == 0) {
                return CheckResult.pass("RULE-003", check.getDescription(),
                    "All old Maven plugins successfully removed");
            } else {
                return CheckResult.fail("RULE-003", check.getDescription(),
                    "Found forbidden plugins:\n" + found.toString().trim());
            }

        } catch (Exception e) {
            return CheckResult.fail("RULE-003", check.getDescription(),
                "Error parsing pom.xml: " + e.getMessage());
        }
    }
}