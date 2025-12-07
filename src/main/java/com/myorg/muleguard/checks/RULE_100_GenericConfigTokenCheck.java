package com.myorg.muleguard.checks;

import com.myorg.muleguard.model.Check;
import com.myorg.muleguard.model.CheckResult;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

public class RULE_100_GenericConfigTokenCheck extends AbstractCheck {

    @Override
    public CheckResult execute(Path projectRoot, Check check) {
        @SuppressWarnings("unchecked")
        List<String> filePatterns = (List<String>) check.getParams().get("filePatterns");
        @SuppressWarnings("unchecked")
        List<String> tokens = (List<String>) check.getParams().get("tokens");
        String type = check.getType();

        boolean tokenFound = false;
        String foundToken = null;

        File searchDir = projectRoot.toFile();
        if (!searchDir.exists() || !searchDir.isDirectory()) {
            return CheckResult.pass(check.getRuleId(), check.getDescription(), "Project directory not found.");
        }


        IOFileFilter fileFilter = new IOFileFilter() {
            @Override
            public boolean accept(File file) {
                String normalizedPath = FilenameUtils.separatorsToUnix(file.getAbsolutePath());
                for (String pattern : filePatterns) {
                    if (FilenameUtils.wildcardMatch(normalizedPath, "**/" + pattern)) {
                        return true;
                    }
                }
                return false;
            }
            @Override
            public boolean accept(File dir, String name) { return accept(new File(dir, name)); }
        };

        Collection<File> files = FileUtils.listFiles(searchDir, fileFilter, TrueFileFilter.INSTANCE);

        for (File file : files) {
            try {
                String content = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
                for (String token : tokens) {
                    if (content.contains(token)) {
                        tokenFound = true;
                        foundToken = token;
                        break;
                    }
                }
            } catch (IOException e) {

            }
            if (tokenFound) break;
        }

        if ("SUBSTRING_TOKEN_CHECK".equals(type)) {
            if (tokenFound) {
                String message = String.format("Forbidden token(s) - '%s' found in specified files.", foundToken);
                return CheckResult.fail(check.getRuleId(), check.getDescription(), message);
            }
            return CheckResult.pass(check.getRuleId(), check.getDescription(), "Forbidden token(s) not found.");
        } else { 
            if (!tokenFound) {
                return CheckResult.fail(check.getRuleId(), check.getDescription(), "Required token(s) not found in specified files.");
            }
            return CheckResult.pass(check.getRuleId(), check.getDescription(), "Required token(s) found.");
        }
    }
}