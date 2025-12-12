# MuleGuard - MuleSoft Static Analysis Tool
# raks
# By - Rakesh Kumar

MuleGuard is a static analysis tool for MuleSoft applications. It validates Mule projects against a defined set of rules to enforce coding standards, security best practices, and migration readiness.

The tool scans various project files, including:
- Maven configurations (`pom.xml`)
- Mule runtime manifests (`mule-artifact.json`)
- XML configurations
- DataWeave scripts
- Environment-specific property files (`*.yaml`, `*.properties`)

It generates detailed HTML and Excel reports highlighting compliance and identifying any violations.

## Features

- **Comprehensive Rule Set**: Validates against a wide range of best practices.
- **Multi-Project Analysis**: Scan a single API or a directory containing multiple APIs.
- **Consolidated Reporting**: Generates a summary dashboard for all scanned APIs.
- **Detailed Individual Reports**: Provides rule-by-rule results for each API, with links from the consolidated report.
- **Multiple Formats**: Reports are generated in both user-friendly HTML and easy-to-process Excel formats.
- **Checklist View**: Includes a `checklist.html` page that lists all possible validation rules the tool checks for.

## Prerequisites

To build and run MuleGuard, you will need the following installed:

- **Java 17**: The project is built using Java 17.
- **Apache Maven**: Required for compiling the source code and packaging the application.

## How to Build

1.  **Clone the repository** (if you haven't already):
    ```sh
    git clone <your-repository-url>
    cd muleguard
    ```

2.  **Build the project using Maven**:
    This command will compile the code, run tests, and package the application into a single, executable "fat JAR" in the `target/` directory.

    ```sh
    mvn clean package
    ```

    The resulting JAR file will be named `muleguard-1.0.0-jar-with-dependencies.jar`.

## How to Use

MuleGuard is a command-line tool. You can run it by executing the JAR file you built in the previous step.

The tool can scan a single Mule project or a directory containing multiple projects.

### Scanning a Single Project

Use the following command to scan a single MuleSoft API project. You need to provide the path to the project directory and specify an output directory for the reports.

```sh
# Usage: java -jar <jar-file> <path-to-mule-project> <output-directory>
java -jar target/muleguard-1.0.0-jar-with-dependencies.jar -p /path/to/your/mule-api
```
example: java -jar .\target\muleguard-1.0.0-jar-with-dependencies.jar -p C:\Users\raksj\Documents\IBM\tmp\t2mtemp\MigrationOutput\Tibco2MuleCode

This will generate an individual report for the API in `report.html` and `report.xlsx` inside the specified output directory.

### Scanning Multiple Projects

To scan multiple projects at once, provide the path to a directory that contains all the MuleSoft API project folders.

```sh
# Usage: java -jar <jar-file> <path-to-directory-of-mule-projects> <output-directory>
java -jar target/muleguard-1.0.0-jar-with-dependencies.jar -p /path/to/your/apis 
```
example: java -jar .\target\muleguard-1.0.0-jar-with-dependencies.jar -p C:\Users\raksj\Documents\IBM\tmp\t2mtemp\MigrationOutput\Tibco2MuleCode

This will generate:
1.  **A Consolidated Report**: `CONSOLIDATED-REPORT.html` and `CONSOLIDATED-REPORT.xlsx` in the root of the output directory. This report provides a summary of all scanned APIs.
2.  **Individual Reports**: A sub-directory for each API, containing its specific `report.html` and `report.xlsx`.
3.  **Checklist**: A `checklist.html` file listing all rules.


## Rule Types Documentation

MuleGuard supports 18 different rule types for comprehensive validation:

### Code Rules (12 types)

Validate Mule application code files (XML, JSON, POM, DataWeave):

| Rule Type | Description | Documentation |
|-----------|-------------|---------------|
| `GENERIC_TOKEN_SEARCH_REQUIRED` | Ensure required tokens exist in files | [📖 Docs](docs/GENERIC_TOKEN_SEARCH_REQUIRED.md) |
| `GENERIC_TOKEN_SEARCH_FORBIDDEN` | Prevent forbidden tokens in files | [📖 Docs](docs/GENERIC_TOKEN_SEARCH_FORBIDDEN.md) |
| `XML_XPATH_EXISTS` | Validate required XPath expressions match | [📖 Docs](docs/XML_XPATH_EXISTS.md) |
| `XML_XPATH_NOT_EXISTS` | Ensure forbidden XPath expressions don't match | [📖 Docs](docs/XML_XPATH_NOT_EXISTS.md) |
| `XML_ATTRIBUTE_EXISTS` | Validate required XML attributes | [📖 Docs](docs/XML_ATTRIBUTE_EXISTS.md) |
| `XML_ATTRIBUTE_NOT_EXISTS` | Prevent forbidden XML attributes | [📖 Docs](docs/XML_ATTRIBUTE_NOT_EXISTS.md) |
| `XML_ELEMENT_CONTENT_REQUIRED` | Ensure XML elements contain required content | [📖 Docs](docs/XML_ELEMENT_CONTENT_REQUIRED.md) |
| `XML_ELEMENT_CONTENT_FORBIDDEN` | Prevent forbidden content in XML elements | [📖 Docs](docs/XML_ELEMENT_CONTENT_FORBIDDEN.md) |
| `POM_VALIDATION_REQUIRED` | Validate required Maven POM elements | [📖 Docs](docs/POM_VALIDATION_REQUIRED.md) |
| `POM_VALIDATION_FORBIDDEN` | Prevent forbidden Maven POM elements | [📖 Docs](docs/POM_VALIDATION_FORBIDDEN.md) |
| `JSON_VALIDATION_REQUIRED` | Ensure required JSON elements exist | [📖 Docs](docs/JSON_VALIDATION_REQUIRED.md) |
| `JSON_VALIDATION_FORBIDDEN` | Prevent forbidden JSON elements | [📖 Docs](docs/JSON_VALIDATION_FORBIDDEN.md) |

### Config Rules (6 types)

Validate environment-specific configuration files:

| Rule Type | Description | Documentation |
|-----------|-------------|---------------|
| `MANDATORY_SUBSTRING_CHECK` | Required/forbidden tokens in environment files | [📖 Docs](docs/MANDATORY_SUBSTRING_CHECK.md) |
| `MANDATORY_PROPERTY_VALUE_CHECK` | Validate required property name-value pairs | [📖 Docs](docs/MANDATORY_PROPERTY_VALUE_CHECK.md) |
| `OPTIONAL_PROPERTY_VALUE_CHECK` | Validate optional property values when present | [📖 Docs](docs/OPTIONAL_PROPERTY_VALUE_CHECK.md) |
| `GENERIC_TOKEN_SEARCH` | Advanced token search with environment filtering | [📖 Docs](docs/GENERIC_TOKEN_SEARCH.md) |
| `CLIENTIDMAP_VALIDATOR` | Validate client ID mappings and secure properties | [📖 Docs](docs/CLIENTIDMAP_VALIDATOR.md) |
| `GENERIC_PROPERTY_FILE_CHECK` | Generic property file validation | [📖 Docs](docs/GENERIC_PROPERTY_FILE_CHECK.md) |

### Configuration Guide

All rules are configured in `src/main/resources/rules/rules.yaml`. Each rule type documentation includes:
- **Parameter reference** - All required and optional parameters
- **Configuration examples** - Real-world usage scenarios
- **Error message format** - What to expect when validation fails
- **Related rule types** - Alternative or complementary rules

## Report Output

After running the tool, the output directory will contain:

```
muleguard-reports/
├── CONSOLIDATED-REPORT.html      # Dashboard for all APIs
├── CONSOLIDATED-REPORT.xlsx      # Excel summary
├── checklist.html                # All validation rules reference
├── api-name-1/
│   ├── report.html              # Individual API report
│   └── report.xlsx              # Individual API Excel
└── api-name-2/
    ├── report.html
    └── report.xlsx
```


## License & Attribution

MuleGuard is distributed under the [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0).

### Third-Party Dependencies

This software includes the following open-source components:

- **DOM4J** - XML parsing library  
  Copyright © 2001-2016 MetaStuff, Ltd. and DOM4J contributors  
  Licensed under the BSD-3-Clause License  
  https://dom4j.github.io/

- **Apache POI**, **Apache Commons IO**, **Apache Commons Text**, **Apache Maven Model**, **Log4j**  
  Licensed under the Apache License 2.0  
  https://www.apache.org/licenses/LICENSE-2.0

- **SnakeYAML**, **Jackson**, **Picocli**, **Jaxen**  
  Licensed under the Apache License 2.0  
  https://www.apache.org/licenses/LICENSE-2.0

For a complete list of dependencies and their versions, see `pom.xml`.

### Security

For security vulnerabilities or concerns, please contact: **raksjnu@gmail.com**

**Last Security Audit**: December 2025  
**Status**: All dependencies updated to secure versions (see `SECURITY_COMPLIANCE_REPORT.md`)
