package com.myorg.muleguard;

import com.myorg.muleguard.MuleGuardMain.ApiResult;
import com.myorg.muleguard.engine.ReportGenerator;
import com.myorg.muleguard.engine.ValidationEngine;
import com.myorg.muleguard.model.Rule;
import com.myorg.muleguard.model.ValidationReport;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.TypeDescription;

import java.io.InputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;


public class MuleGuardMain {
    

    public static void main(String[] args) {
    Path parentFolder;

    if (args.length == 0 || args[0].isEmpty()) {
        parentFolder = showFolderDialog();
        if (parentFolder == null) {
            System.out.println("No folder selected. Exiting.");
            return;
        }
    } 
    else if (args.length >= 2 && "-p".equals(args[0])) {
        parentFolder = Paths.get(args[1]);
    } 
    else {
        System.err.println("Usage: java -jar muleguard.jar -p <folder>   OR   double-click to select folder");
        return;
    }

    if (!Files.isDirectory(parentFolder)) {
        System.err.println("Error: Not a valid folder: " + parentFolder);
        return;
    }

    System.out.println("Starting MuleGuard validation on: " + parentFolder);
        System.out.println("Scanning for Mule API projects...\n");


        RootWrapper configWrapper = loadConfig();
        List<Rule> allRules = configWrapper.getRules();
        String configFolderPattern = configWrapper.getConfig().getFolderPattern();
        int configRuleStart = configWrapper.getConfig().getRules().get("start");
        int configRuleEnd = configWrapper.getConfig().getRules().get("end");
        List<String> globalEnvironments = configWrapper.getConfig().getEnvironments();

        List<ApiResult> results = new ArrayList<>();
        Path reportsRoot = parentFolder.resolve("muleguard-reports");

        try {
            Files.createDirectories(reportsRoot);  
        } catch (IOException e) {
            System.err.println("Failed to create reports directory: " + e.getMessage());
            System.exit(1);
        }

        try (var stream = Files.list(parentFolder)) {
    stream.filter(Files::isDirectory)
          .filter(dir -> {
    String name = dir.getFileName().toString();


    Set<String> EXACT_IGNORED = Set.of("muleguard-reports", "target", "bin", "build");

    if (EXACT_IGNORED.contains(name) || name.startsWith(".")) {
        return false;
    }


    boolean isCodeProject = Files.exists(dir.resolve("pom.xml")) || Files.exists(dir.resolve("mule-artifact.json"));
    boolean isConfigProject = name.matches(configFolderPattern);

    return isCodeProject || isConfigProject;
})
                  .forEach(apiDir -> {
                      String apiName = apiDir.getFileName().toString();
                      boolean isConfigProject = apiName.matches(configFolderPattern);
                      System.out.printf("Validating %s: %s%n", isConfigProject ? "Config" : "API", apiName);


                      List<Rule> applicableRules = allRules.stream()
                          .filter(Rule::isEnabled)
                          .filter(rule -> {
                              int ruleIdNum = Integer.parseInt(rule.getId().replace("RULE-", ""));
                              boolean isConfigRule = (ruleIdNum >= configRuleStart && ruleIdNum <= configRuleEnd);

                              
                              if (isConfigRule && globalEnvironments != null && !globalEnvironments.isEmpty()) {
                                  rule.getChecks().forEach(check -> check.getParams().putIfAbsent("environments", globalEnvironments));
                              }

                              return isConfigProject == isConfigRule;
                          }).collect(Collectors.toList());

                      ValidationEngine engine = new ValidationEngine(applicableRules, apiDir);
                      ValidationReport report = engine.validate();
                      report.projectPath = apiName + " (" + apiDir.toString() + ")";

                      Path apiReportDir = reportsRoot.resolve(apiName);
                      
                      try {
                          Files.createDirectories(apiReportDir);
                      } catch (IOException e) {
                          System.err.println("Failed to create report dir for " + apiName + ": " + e.getMessage());
                          return;
                      }
                      
                      
                      ReportGenerator.generateHtml(report, apiReportDir.resolve("report.html"));
                      ReportGenerator.generateExcel(report, apiReportDir.resolve("report.xlsx"));

                      int passed = report.passed.size();
                      int failed = report.failed.size();
                      results.add(new ApiResult(apiName, apiDir, passed, failed, apiReportDir));

                      System.out.println("   " + (failed == 0 ? "PASS" : "FAIL") +
                          " | Passed: " + passed + " | Failed: " + failed + "\n");
                  });
        } catch (Exception e) {
            System.err.println("Error scanning folders: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

       try {
    ReportGenerator.generateConsolidatedReport(results, reportsRoot);
} catch (Throwable t) {
    System.err.println("FAILED TO GENERATE CONSOLIDATED REPORT!");
    System.err.println("Exception: " + t.getClass().getSimpleName());
    String msg = t.getMessage();
    if (msg != null) {
        System.err.println("Message: " + msg.replace('%', '％'));  // ← Full-width percent ％ (U+FF05)
    }
    t.printStackTrace(System.err);
}


        System.out.println("BATCH VALIDATION COMPLETE!");
        System.out.println("Consolidated report: " + reportsRoot.resolve("CONSOLIDATED-REPORT.html"));
        System.out.println("Individual reports in: " + reportsRoot);
        
        int totalFailed = results.stream().mapToInt(r -> r.failed).sum();
        System.exit(totalFailed > 0 ? 1 : 0); 
    }

    private static RootWrapper loadConfig() {
        LoaderOptions options = new LoaderOptions();
        Constructor constructor = new Constructor(RootWrapper.class, options);
        TypeDescription td = new TypeDescription(RootWrapper.class);
        td.addPropertyParameters("rules", Rule.class);
        constructor.addTypeDescription(td);

        Yaml yaml = new Yaml(constructor);
        InputStream input = MuleGuardMain.class.getClassLoader().getResourceAsStream("rules/rules.yaml");
        if (input == null) {
            System.err.println("rules.yaml not found!");
            System.exit(1);
        }
        return yaml.loadAs(input, RootWrapper.class);
    }

private static Path showFolderDialog() {

    final Path[] selected = new Path[1];
    try {
        javax.swing.SwingUtilities.invokeAndWait(() -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Select Parent Folder containing Mule APIs");
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setAcceptAllFileFilterUsed(false);

            if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                selected[0] = chooser.getSelectedFile().toPath();
            }
        });
    } catch (Exception e) {
        e.printStackTrace();
    }
    return selected[0];
}

    public static class ConfigSection {
        private String folderPattern;
        private Map<String, Integer> rules;
        private List<String> environments;

        public String getFolderPattern() {
            return folderPattern;
        }

        public void setFolderPattern(String folderPattern) {
            this.folderPattern = folderPattern;
        }

        public Map<String, Integer> getRules() {
            return rules;
        }

        public void setRules(Map<String, Integer> rules) {
            this.rules = rules;
        }

        public List<String> getEnvironments() {
            return environments;
        }
        public void setEnvironments(List<String> environments) {
            this.environments = environments;
        }
    }

    public static class RootWrapper {
        private ConfigSection config;
        private List<Rule> rules;

        public ConfigSection getConfig() { return config; }
        public void setConfig(ConfigSection config) { this.config = config; }
        public List<Rule> getRules() { return rules; }
        public void setRules(List<Rule> rules) { this.rules = rules; }
    }

    public static class ApiResult {
        public final String name;
        public final Path path;
        public final int passed, failed;
        public final Path reportDir;

        public ApiResult(String name, Path path, int passed, int failed, Path reportDir) {
            this.name = name; this.path = path; this.passed = passed; this.failed = failed; this.reportDir = reportDir;
        }
    }
}