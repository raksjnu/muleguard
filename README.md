# MuleGuard - MuleSoft Static Analysis Tool
# By - Rakesh Kumar ; RAKSJNU@GMAIL.COM

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

### Report Output

After running the tool, the output directory will look something like this:

```
output/
├── CONSOLIDATED-REPORT.html
├── CONSOLIDATED-REPORT.xlsx
├── checklist.html
├── my-api-1/
|   ├── report.html
|   └── report.xlsx
└── my-api-2/
    ├── report.html
    └── report.xlsx
```