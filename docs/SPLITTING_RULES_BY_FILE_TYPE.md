# Splitting Regex Rules by File Type - Configuration Guide

## Problem
When you have one rule validating both `.policy` and `.properties` files with different regex patterns, you get unnecessary failure messages for patterns that don't apply to certain file types.

## Solution
Split into **separate rules** - one for each file type.

## Before (Single Rule - Not Recommended)

```yaml
- id: "RULE-103"
  checks:
    - type: GENERIC_PROPERTY_FILE
      params:
        fileExtensions:
          - ".properties"
          - ".policy"
        regexPatterns:
          - "headerinjection.policy.applied=true"  # Only in .policy files
          - "db.connection=jdbc:.*"                 # Only in .properties files
```

**Problem**: `.properties` files will fail for policy patterns, and `.policy` files will fail for properties patterns.

## After (Separate Rules - RECOMMENDED)

### RULE-103: Policy Files Only
```yaml
- id: "RULE-103"
  name: "Validate policy file property patterns"
  description: "Validates that .policy files contain required policy properties."
  enabled: true
  severity: HIGH
  checks:
    - type: GENERIC_PROPERTY_FILE
      params:
        parseMode: "REGEX_PATTERN"
        fileExtensions:
          - ".policy"
        regexPatterns:
          - "headerinjection.policy.applied=true"
          - "headerinjection.policy.version=1.3.2"
          - "ratelimit.policy.applied=true"
          - "ratelimit.policy.version=1.4.2"
```

### RULE-104: Properties Files Only
```yaml
- id: "RULE-104"
  name: "Validate properties file patterns"
  description: "Validates that .properties files contain required configuration."
  enabled: true
  severity: MEDIUM
  checks:
    - type: GENERIC_PROPERTY_FILE
      params:
        parseMode: "REGEX_PATTERN"
        fileExtensions:
          - ".properties"
        regexPatterns:
          - ".*db\\.connection=jdbc:.*"
          - ".*api\\..*\\.url=https://.*"
```

## Benefits

✅ **Clean Validation**: Each file type only checked against relevant patterns  
✅ **Clear Reports**: No confusing "pattern not found" for irrelevant patterns  
✅ **Easy Maintenance**: Add patterns to the appropriate rule  
✅ **Different Severities**: Policy files can be HIGH, properties can be MEDIUM  

## Alternative: Multiple Checks in One Rule

If you prefer to keep one rule, you can use multiple checks:

```yaml
- id: "RULE-103"
  name: "Validate config files"
  checks:
    # Check 1: Policy files
    - type: GENERIC_PROPERTY_FILE
      description: "Policy file validation"
      params:
        parseMode: "REGEX_PATTERN"
        fileExtensions: [".policy"]
        regexPatterns:
          - "headerinjection.policy.applied=true"
    
    # Check 2: Properties files
    - type: GENERIC_PROPERTY_FILE
      description: "Properties file validation"
      params:
        parseMode: "REGEX_PATTERN"
        fileExtensions: [".properties"]
        regexPatterns:
          - ".*db\\.connection=jdbc:.*"
```

**Note**: This keeps them in one rule but still separates the validation logic.

## Recommendation

**Use separate rules (RULE-103 and RULE-104)** for:
- Better organization
- Independent enable/disable
- Different severity levels
- Clearer reporting
