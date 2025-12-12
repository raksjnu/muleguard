package com.raks.muleguard.engine;

import com.raks.muleguard.model.ValidationReport;
import com.raks.muleguard.model.ValidationReport.RuleResult;
import com.raks.muleguard.MuleGuardMain.ApiResult;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ReportGenerator {

    // Generate individual API report (HTML + Excel)
    public static void generateIndividualReports(ValidationReport report, Path outputDir) {
        try {
            Files.createDirectories(outputDir);
            generateHtml(report, outputDir.resolve("report.html"));
            generateExcel(report, outputDir.resolve("report.xlsx"));

            // Copy logo.svg to individual report directory
            try (InputStream logoStream = ReportGenerator.class.getResourceAsStream("/logo.svg")) {
                if (logoStream != null) {
                    Path logoPath = outputDir.resolve("logo.svg");
                    Files.copy(logoStream, logoPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("   → logo.svg copied to " + outputDir.getFileName());
                } else {
                    System.err.println("Warning: logo.svg not found in resources for " + outputDir.getFileName());
                }
            } catch (Exception logoEx) {
                System.err.println(
                        "Warning: Failed to copy logo.svg to " + outputDir.getFileName() + ": " + logoEx.getMessage());
            }
        } catch (Exception e) {
            System.err.println("Failed to generate individual reports: " + e.getMessage());
        }
    }

    // Generate beautiful HTML report
    public static void generateHtml(ValidationReport report, Path outputPath) {
        try {
            Files.createDirectories(outputPath.getParent());

            StringBuilder rows = new StringBuilder();

            for (RuleResult r : report.passed) {
                rows.append(String.format(
                        "<tr style='background-color:#e8f5e9'><td>%s</td><td>%s</td><td>%s</td><td><strong style='color:green'>PASS</strong></td><td>All checks passed</td></tr>",
                        escape(r.id), escape(r.name), escape(r.severity)));
            }

            for (RuleResult r : report.failed) {
                List<String> messages = r.checks.stream()
                        .filter(c -> !c.passed)
                        .map(c -> escape(c.message))
                        .toList();

                String details = messages.stream()
                        .collect(Collectors.groupingBy(
                                s -> s.contains(" not found in file: ") ? s.substring(0, s.lastIndexOf(":") + 1) : s,
                                Collectors.mapping(
                                        s -> s.contains(" not found in file: ")
                                                ? s.substring(s.lastIndexOf(":") + 1).trim()
                                                : "",
                                        Collectors.joining(", "))))
                        .entrySet().stream()
                        .map(entry -> "• " + entry.getKey() + entry.getValue())
                        .collect(Collectors.joining("<br>"));

                rows.append(String.format(
                        "<tr style='background-color:#ffebee'><td>%s</td><td>%s</td><td>%s</td><td><strong style='color:red'>FAIL</strong></td><td>%s</td></tr>",
                        escape(r.id), escape(r.name), escape(r.severity), details));
            }

            String html = """
                    <!DOCTYPE html>
                    <html>
                    <head>
                        <meta charset="UTF-8">
                        <title>MuleGuard Report - %s</title>
                        <style>
                            :root {
                                --truist-purple: #663399;
                                --truist-purple-light: #7d4fb2;
                                --text-white: #FFFFFF;
                            }
                            body {font-family: Arial, sans-serif; margin: 0; background-color: #f5f5f5;}
                            .report-container { border: 5px solid var(--truist-purple); padding: 20px 40px; margin: 20px; border-radius: 8px; background-color: white; position: relative; }
                            h1 {color: var(--truist-purple);}
                            .summary {background: #e3f2fd; padding: 20px; border-radius: 8px; margin-bottom: 20px;}
                            table {width: 100%%; border-collapse: collapse; box-shadow: 0 4px 12px rgba(0,0,0,0.1);}
                            th, td {border: 1px solid #ddd; padding: 12px; text-align: left;}
                            th {background-color: var(--truist-purple); color: var(--text-white);}
                            .contact-button {
                                background-color: var(--truist-purple);
                                color: var(--text-white);
                                border: none;
                                padding: 12px 24px;
                                text-align: center;
                                text-decoration: none;
                                display: inline-block;
                                font-size: 16px;
                                font-weight: bold;
                                margin-top: 25px;
                                cursor: pointer;
                                border-radius: 5px;
                                transition: background-color 0.3s ease;
                                box-shadow: 0 2px 4px rgba(0,0,0,0.2);
                            }
                            .contact-button:hover { background-color: var(--truist-purple-light); }
                            .dashboard-button {
                                position: absolute;
                                top: 20px;
                                right: 40px;
                                background-color: var(--truist-purple);
                                color: var(--text-white);
                                border: none;
                                padding: 10px 20px;
                                text-align: center;
                                text-decoration: none;
                                display: inline-block;
                                font-size: 14px;
                                font-weight: bold;
                                cursor: pointer;
                                border-radius: 5px;
                                transition: background-color 0.3s ease;
                                box-shadow: 0 2px 4px rgba(0,0,0,0.2);
                            }
                            .dashboard-button:hover { background-color: var(--truist-purple-light); }
                        </style>
                    </head>
                    <body>
                        <div class="report-container">
                            <a href="../CONSOLIDATED-REPORT.html" class="dashboard-button" title="Return to main dashboard">← Dashboard</a>
                            <div style="display: flex; align-items: center; margin-bottom: 20px;">
                                <img src="logo.svg" alt="MuleGuard Logo" style="height: 40px; margin-right: 15px;">
                                <h1 style="margin: 0;">MuleGuard - Mulesoft Application Review & Validation</h1>
                            </div>
                            <div class="summary">
                                <strong>Project:</strong> %s<br>
                                <strong>Generated:</strong> %s<br>
                                <strong>Total Rules:</strong> %d | <strong style="color:green">Passed:</strong> %d | <strong style="color:red">Failed:</strong> %d
                            </div>
                            <table><tr><th>Rule ID</th><th>Name</th><th>Severity</th><th>Status</th><th>Details</th></tr>%s</table>
                            <a href="../checklist.html" class="contact-button" title="View all validation checklist items.">Checklist</a>
                            <a href="" class="contact-button" title="Mulesoft Runtime Upgrade Project Runbook.">Runbook</a>
                            <a href="" class="contact-button" title="Mulesoft Runtime Upgrade Project Developer Playbook.">Dev Playbook</a>
                            <a href="../help.html" class="contact-button" title="View help and documentation about MuleGuard" style="margin-left: 10px;">Help</a>
                        </div>
                    </body>
                    </html>
                    """
                    .formatted(
                            escape(report.projectPath),
                            escape(report.projectPath),
                            LocalDateTime.now(),
                            report.passed.size() + report.failed.size(),
                            report.passed.size(),
                            report.failed.size(),
                            rows.toString());

            Files.writeString(outputPath, html);

        } catch (Exception e) {
            System.err.println("Failed to generate HTML: " + e.getMessage());
        }
    }

    // Generate Excel report (.xlsx)
    public static void generateExcel(ValidationReport report, Path outputPath) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Validation Results");

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            CellStyle passStyle = workbook.createCellStyle();
            passStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
            passStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            CellStyle failStyle = workbook.createCellStyle();
            failStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
            failStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            Row header = sheet.createRow(0);
            String[] columns = { "Rule ID", "Name", "Severity", "Status", "Details" };
            for (int i = 0; i < columns.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (RuleResult r : report.passed) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(r.id);
                row.createCell(1).setCellValue(r.name);
                row.createCell(2).setCellValue(r.severity);
                row.createCell(3).setCellValue("PASS");
                row.createCell(4).setCellValue("All checks passed");
                for (int i = 0; i < 5; i++)
                    row.getCell(i).setCellStyle(passStyle);
            }

            for (RuleResult r : report.failed) {
                String details = r.checks.stream()
                        .filter(c -> !c.passed)
                        .map(c -> "• " + c.message)
                        .collect(Collectors.joining("\n"));

                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(r.id);
                row.createCell(1).setCellValue(r.name);
                row.createCell(2).setCellValue(r.severity);
                row.createCell(3).setCellValue("FAIL");
                row.createCell(4).setCellValue(details.isEmpty() ? "Failed" : details);
                for (int i = 0; i < 5; i++)
                    row.getCell(i).setCellStyle(failStyle);
            }

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            try (FileOutputStream fos = new FileOutputStream(outputPath.toFile())) {
                workbook.write(fos);
            }
        } catch (Exception e) {
            System.err.println("Failed to generate Excel: " + e.getMessage());
        }
    }

    // Consolidated multi-API report (HTML + CSV)
    public static void generateConsolidatedReport(List<ApiResult> results, Path outputPath) {
        try {
            if (results == null || results.isEmpty()) {
                System.err.println("No results to generate consolidated report.");
                return;
            }

            Files.createDirectories(outputPath.getParent());

            StringBuilder tableRows = new StringBuilder();
            int totalApis = results.size();
            int totalPassed = 0;
            int totalFailed = 0;

            for (ApiResult r : results) {
                if (r == null || r.name == null || r.reportDir == null)
                    continue;

                totalPassed += r.passed;
                totalFailed += r.failed;

                String status = r.failed == 0 ? "PASS" : "FAIL";
                String color = r.failed == 0 ? "#e8f5e9" : "#ffebee";

                // Safely build relative path
                Path target = null;
                try {
                    target = r.reportDir.resolve("report.html");
                    if (!Files.exists(target)) {
                        System.err.println("Warning: Report not found: " + target);
                    }
                } catch (Exception e) {
                    System.err.println("Invalid report path for API: " + r.name);
                    continue;
                }

                String relativeLink;
                try {
                    // relativeLink = outputPath.getParent().relativize(target)
                    relativeLink = outputPath.relativize(target)
                            .toString().replace("\\", "/");
                } catch (Exception e) {
                    relativeLink = "report.html"; // fallback
                }

                tableRows.append("<tr style='background-color:").append(color).append("'>")
                        .append("<td>").append(escape(r.name)).append("</td>")
                        .append("<td>").append(r.passed + r.failed).append("</td>")
                        .append("<td>").append(r.passed).append("</td>")
                        .append("<td>").append(r.failed).append("</td>")
                        .append("<td><strong style='color:").append(r.failed == 0 ? "green" : "red").append("'>")
                        .append(status).append("</strong></td>")
                        .append("<td><a href='").append(escape(relativeLink)).append("'>View Report</a></td>")
                        .append("</tr>\n");
            }

            int totalRules = totalPassed + totalFailed;

            String html = """
                    <!DOCTYPE html>
                    <html>
                    <head>
                        <meta charset="UTF-8">
                        <title>MuleGuard - Consolidated Report</title>
                        <style>
                            :root {
                                --truist-purple: #663399;
                                --truist-purple-light: #7d4fb2;
                                --text-white: #FFFFFF;
                            }
                            body {font-family: Arial, sans-serif; margin: 0; background-color: #f0f0f0;}
                            .report-container { border: 5px solid var(--truist-purple); padding: 20px 40px; margin: 20px; border-radius: 8px; background-color: white; }
                            h1 {color: var(--truist-purple);}
                            .card {background: white; padding: 20px; border-radius: 10px; box-shadow: 0 4px 20px rgba(0,0,0,0.1); margin-bottom: 20px;}
                            table {width: 100%%; border-collapse: collapse;}
                            th, td {border: 1px solid #ddd; padding: 12px; text-align: left;}
                            th {background: var(--truist-purple); color: var(--text-white);}
                            .contact-button {
                                background-color: var(--truist-purple);
                                color: var(--text-white);
                                border: none;
                                padding: 12px 24px;
                                text-align: center;
                                text-decoration: none;
                                display: inline-block;
                                font-size: 16px;
                                font-weight: bold;
                                margin-top: 25px;
                                cursor: pointer;
                                border-radius: 5px;
                                transition: background-color 0.3s ease;
                                box-shadow: 0 2px 4px rgba(0,0,0,0.2);
                            }
                            .contact-button:hover { background-color: var(--truist-purple-light); }
                        </style>
                    </head>
                    <body>
                        <div class="report-container">
                            <div style="display: flex; align-items: center; margin-bottom: 20px;">
                                <img src="logo.svg" alt="MuleGuard Logo" style="height: 40px; margin-right: 15px;">
                                <h1 style="margin: 0;">MuleGuard - Mulesoft Application Review & Validation</h1>
                            </div>


                            <div style="border: 1px solid #ccc; padding: 10px 20px; margin-top: 15px; margin-bottom: 20px; background-color: #fbfbfbff; border-radius: 5px;">
                            <h4 style="margin-top: 0; color: #333;">Report Details:</h4>
                                <strong>Generated:</strong> %s<br>
                                <strong>Total APIs Scanned:</strong> %d<br>
                                <strong>Total Rules:</strong> %d | <strong style="color:green">Passed:</strong> %d | <strong style="color:red">Failed:</strong> %d
                            </div>
                            <table><tr><th>API Name</th><th>Total Rules</th><th>Passed</th><th>Failed</th><th>Status</th><th>Report</th></tr>%s</table>
                            <a href="checklist.html" class="contact-button" title="View all validation checklist items.">Checklist</a>
                            <a href="" class="contact-button" title="Mulesoft Runtime Upgrade Project Runbook.">Runbook</a>
                            <a href="" class="contact-button" title="Mulesoft Runtime Upgrade Project Developer Playbook.">Dev Playbook</a>
                            <a href="help.html" class="contact-button" title="View help and documentation about MuleGuard" style="margin-left: 10px;">Help</a>



                            </div>
                    </body>
                    </html>
                    """
                    .formatted(
                            LocalDateTime.now(),
                            totalApis,
                            totalRules,
                            totalPassed,
                            totalFailed,
                            tableRows);

            Path htmlPath = outputPath.resolve("CONSOLIDATED-REPORT.html");
            Files.writeString(htmlPath, html);

            System.out.println("CONSOLIDATED REPORT GENERATED:");
            System.out.println("   → " + htmlPath.toAbsolutePath());

            // Copy help.html to reports directory
            copyHelpFile(outputPath);

            generateConsolidatedExcel(results, outputPath);
            generateChecklistReport(outputPath); // Generate checklist.html here

        } catch (Throwable t) {

            System.err.println("FAILED TO GENERATE CONSOLIDATED REPORT!");
            System.err.println("Error type: " + t.getClass().getName());
            System.err.println("Message: " + (t.getMessage() != null ? t.getMessage().replace('%', '％') : "null"));
            t.printStackTrace(System.err); // This bypasses logger
        }
    }

    private static void generateChecklistReport(Path outputDir) {
        try {
            StringBuilder rows = new StringBuilder();
            try (InputStream is = ReportGenerator.class.getResourceAsStream("/rulemapping.csv");
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(Objects.requireNonNull(is, "Cannot find rulemapping.csv")))) {

                reader.readLine();

                int srNo = 1;
                String line;
                while ((line = reader.readLine()) != null) {

                    String[] parts = line.split(",", 3);
                    if (parts.length == 3) {
                        rows.append(String.format("<tr><td>%d</td><td>%s</td><td>%s</td><td>%s</td></tr>", srNo++,
                                escape(parts[0]), escape(parts[1]), escape(parts[2])));
                    }
                }
            }

            String html = """
                    <!DOCTYPE html>
                    <html>
                    <head>
                        <meta charset="UTF-8">
                        <title>MuleGuard - Validation Checklist</title>
                        <style>
                            :root {
                                --truist-purple: #663399;
                                --truist-purple-light: #7d4fb2;
                                --text-white: #FFFFFF;
                            }
                            body {font-family: Arial, sans-serif; margin: 0; background-color: #f0f0f0;}
                            .report-container { border: 5px solid var(--truist-purple); padding: 20px 40px; margin: 20px; border-radius: 8px; background-color: white; position: relative; }
                            h1 {color: var(--truist-purple);}
                            table {width: 100%%; border-collapse: collapse; box-shadow: 0 4px 12px rgba(0,0,0,0.1);}
                            th, td {border: 1px solid #ddd; padding: 12px; text-align: left;}
                            th {background-color: var(--truist-purple); color: var(--text-white);}
                            .contact-button {
                                background-color: var(--truist-purple);
                                color: var(--text-white);
                                border: none;
                                padding: 12px 24px;
                                text-align: center;
                                text-decoration: none;
                                display: inline-block;
                                font-size: 16px;
                                font-weight: bold;
                                margin-top: 25px;
                                cursor: pointer;
                                border-radius: 5px;
                                transition: background-color 0.3s ease;
                                box-shadow: 0 2px 4px rgba(0,0,0,0.2);
                            }
                            .contact-button:hover { background-color: var(--truist-purple-light); }
                        .dashboard-button {
                            position: absolute;
                            top: 20px;
                            right: 40px;
                            background-color: var(--truist-purple);
                            color: var(--text-white);
                            border: none;
                            padding: 10px 20px;
                            text-align: center;
                            text-decoration: none;
                            display: inline-block;
                            font-size: 14px;
                            font-weight: bold;
                            cursor: pointer;
                            border-radius: 5px;
                            transition: background-color 0.3s ease;
                            box-shadow: 0 2px 4px rgba(0,0,0,0.2);
                        }
                        .dashboard-button:hover { background-color: var(--truist-purple-light); }
                        </style>
                    </head>
                    <body>
                        <div class="report-container">
                            <a href="CONSOLIDATED-REPORT.html" class="dashboard-button" title="Return to main dashboard">← Dashboard</a>
                            <div style="display: flex; align-items: center; margin-bottom: 20px;">
                                <img src="logo.svg" alt="MuleGuard Logo" style="height: 40px; margin-right: 15px;">
                                <h1 style="margin: 0;">MuleGuard - Mulesoft Application Review & Validation</h1>
                            </div>
                            <p>This page lists all the individual checks performed by the MuleGuard tool.</p>
                            <table>
                                <tr><th>Sr.#</th><th>ChecklistItem</th><th>ChecklistType</th><th>RuleId</th></tr>
                                %s
                            </table>
                            <a href="help.html" class="contact-button" title="View help and documentation about MuleGuard">Help</a>
                        </div>
                    </body>
                    </html>
                    """
                    .formatted(rows.toString());

            Path checklistPath = outputDir.resolve("checklist.html");
            Files.writeString(checklistPath, html);
            System.out.println("   → checklist.html generated");
        } catch (Exception e) {
            System.err.println("Failed to generate checklist report: " + e.getMessage());
        }
    }

    private static void generateConsolidatedExcel(List<ApiResult> results, Path outputDir) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("MuleGuard Summary");

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            CellStyle passStyle = workbook.createCellStyle();
            passStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
            passStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            CellStyle failStyle = workbook.createCellStyle();
            failStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
            failStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            Row header = sheet.createRow(0);
            String[] columns = { "API Name", "Total Rules", "Passed", "Failed", "Status", "Report Path" };
            for (int i = 0; i < columns.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            int totalRules = 0, totalPassed = 0, totalFailed = 0;

            for (ApiResult r : results) {
                if (r == null || r.name == null)
                    continue;

                totalRules += r.passed + r.failed;
                totalPassed += r.passed;
                totalFailed += r.failed;

                Row row = sheet.createRow(rowNum++);
                CellStyle rowStyle = (r.failed == 0) ? passStyle : failStyle;

                row.createCell(0).setCellValue(r.name);
                row.createCell(1).setCellValue(r.passed + r.failed);
                row.createCell(2).setCellValue(r.passed);
                row.createCell(3).setCellValue(r.failed);
                row.createCell(4).setCellValue(r.failed == 0 ? "PASS" : "FAIL");
                row.createCell(5).setCellValue(r.reportDir.resolve("report.html").toString());

                for (int i = 0; i < 6; i++) {
                    row.getCell(i).setCellStyle(rowStyle);
                }
            }

            Row summary = sheet.createRow(rowNum++);
            summary.createCell(0).setCellValue("TOTAL");
            summary.createCell(1).setCellValue(totalRules);
            summary.createCell(2).setCellValue(totalPassed);
            summary.createCell(3).setCellValue(totalFailed);
            summary.createCell(4).setCellValue(totalFailed == 0 ? "ALL PASS" : "SOME FAILURES");

            CellStyle bold = workbook.createCellStyle();
            Font boldFont = workbook.createFont();
            boldFont.setBold(true);
            bold.setFont(boldFont);
            summary.getCell(0).setCellStyle(bold);

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            Path excelPath = outputDir.resolve("CONSOLIDATED-REPORT.xlsx");
            try (FileOutputStream fos = new FileOutputStream(excelPath.toFile())) {
                workbook.write(fos);
            }
            System.out.println("   → CONSOLIDATED-REPORT.xlsx generated");

        } catch (Exception e) {
            System.err.println("Failed to generate consolidated Excel report: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String escape(String s) {
        if (s == null)
            return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    // Copy help.html from resources to reports directory
    private static void copyHelpFile(Path outputDir) {
        try {
            // Copy help.html
            InputStream helpStream = ReportGenerator.class.getResourceAsStream("/help.html");
            if (helpStream == null) {
                System.err.println("Warning: help.html not found in resources");
            } else {
                Path helpPath = outputDir.resolve("help.html");
                Files.copy(helpStream, helpPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                helpStream.close();
                System.out.println("   → help.html copied");
            }

            // Copy logo.svg
            InputStream logoStream = ReportGenerator.class.getResourceAsStream("/logo.svg");
            if (logoStream == null) {
                System.err.println("Warning: logo.svg not found in resources");
            } else {
                Path logoPath = outputDir.resolve("logo.svg");
                Files.copy(logoStream, logoPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                logoStream.close();
                System.out.println("   → logo.svg copied");
            }
        } catch (Exception e) {
            System.err.println("Warning: Failed to copy help files: " + e.getMessage());
        }
    }
}