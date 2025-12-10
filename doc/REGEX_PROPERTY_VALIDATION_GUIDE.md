# Regex Pattern Validation for Config Properties

## Overview

The `GenericPropertyFileCheck` now supports **regex pattern validation** for config files, allowing you to define flexible patterns for both property names and values.

## Use Case

You want to validate that config files contain properties matching specific patterns, such as:
- Property name starting with `a` and value containing `.z`
- Property name containing `db` and value starting with `jdbc`
- Any property ending with `.url` and value starting with `http`

## Configuration

### Parse Mode: `REGEX_PATTERN`

```yaml
- id: "RULE-103"
  name: "Validate config property patterns with regex"
  description: "Validates that config files contain properties matching specific regex patterns."
  enabled: true
  severity: MEDIUM
  checks:
    - type: GENERIC_PROPERTY_FILE
      description: "Check for properties matching regex patterns"
      params:
        parseMode: "REGEX_PATTERN"
        fileExtensions:
          - ".properties"
          - ".policy"
        regexPatterns:
          - "a.*=.*\\.z.*"           # Property starting with 'a', value containing '.z'
          - ".*db.*=jdbc:.*"         # Property containing 'db', value starting with 'jdbc'
          - ".*\\.url=https?://.*"   # Property ending with '.url', value starting with 'http'
```

### Pattern Format

Each regex pattern follows the format: `propertyNameRegex=propertyValueRegex`

- **Left side** (before `=`): Regex pattern for property name
- **Right side** (after `=`): Regex pattern for property value

**Important**: Whitespace around the `=` sign is **automatically trimmed**, so your patterns don't need to account for spaces.

### Whitespace Handling

✅ **Automatic Trimming**: The validator automatically trims whitespace from both property names and values.

All of these formats will match the pattern `headerinjection\.policy\.version=1\.3\.2`:
```properties
headerinjection.policy.version=1.3.2        # No whitespace
headerinjection.policy.version = 1.3.2      # Whitespace on both sides
headerinjection.policy.version =1.3.2       # Whitespace before =
headerinjection.policy.version= 1.3.2       # Whitespace after =
headerinjection.policy.version  =  1.3.2    # Multiple spaces
```

**You don't need to use `\s*` in your patterns** - just write the pattern as if there's no whitespace!

### Examples

| Pattern | Matches | Example Property |
|---------|---------|------------------|
| `a.*=.*\\.z.*` | Property name starts with 'a', value contains '.z' | `app.name=com.z.service` |
| `.*db.*=jdbc:.*` | Property name contains 'db', value starts with 'jdbc' | `database.url=jdbc:mysql://localhost` |
| `.*\\.url=https?://.*` | Property name ends with '.url', value is HTTP(S) URL | `service.url=https://api.example.com` |
| `api\\..*=.*\\.endpoint` | Property starts with 'api.', value ends with '.endpoint' | `api.gateway=http://service.endpoint` |

### Regex Syntax

Use standard Java regex syntax:
- `.` - Any single character
- `*` - Zero or more of the preceding element
- `.*` - Any sequence of characters
- `\\.` - Literal dot (escaped)
- `^` - Start of string
- `$` - End of string
- `[a-z]` - Character class
- `(abc|def)` - Alternation

## How It Works

1. **File Scanning**: Scans config files matching specified environments and extensions
2. **Line Parsing**: Reads each line and parses `property=value` pairs
3. **Pattern Matching**: Tests each property against all regex patterns
4. **Validation**: Passes if at least one property matches each pattern

## Complete Example

```yaml
- id: "RULE-103"
  name: "Validate database and API config patterns"
  enabled: true
  severity: HIGH
  checks:
    - type: GENERIC_PROPERTY_FILE
      params:
        parseMode: "REGEX_PATTERN"
        fileExtensions:
          - ".properties"
        regexPatterns:
          # Database connection must use JDBC
          - ".*db\\.connection=jdbc:.*"
          
          # API endpoints must be HTTPS
          - ".*api\\..*\\.url=https://.*"
          
          # Log level must be INFO, WARN, or ERROR
          - ".*log\\.level=(INFO|WARN|ERROR)"
          
          # Timeout values must be numeric
          - ".*\\.timeout=[0-9]+"
```

## Benefits

✅ **Flexible Validation**: Define complex property patterns  
✅ **Multiple Patterns**: Check for multiple patterns in one rule  
✅ **File Type Agnostic**: Works with `.properties`, `.policy`, and other text files  
✅ **Environment Specific**: Apply to specific environments (dev, qa, prod)  

## Migration from Simple Property Check

### Before (Simple Check)
```yaml
params:
  propertyNames:
    - "app.name"
    - "db.connection"
```

### After (Regex Pattern)
```yaml
params:
  parseMode: "REGEX_PATTERN"
  regexPatterns:
    - "app\\.name=.*"        # Property must exist with any value
    - "db\\.connection=.*"   # Property must exist with any value
```

## Troubleshooting

### Pattern Not Matching
- Ensure you escape special regex characters (e.g., `\\.` for literal dot)
- Test your regex pattern using an online regex tester
- Check that property files use `=` as separator (not `:`)

### Invalid Pattern Error
- Verify pattern format: `namePattern=valuePattern`
- Ensure both sides of `=` are valid regex patterns
- Check for unescaped special characters

## See Also

- [RULE_CONFIGURATION_GUIDE.yaml](RULES_CONFIGURATION_GUIDE.yaml) - Complete rules configuration
- [GenericPropertyFileCheck.java](src/main/java/com/raks/muleguard/checks/GenericPropertyFileCheck.java) - Implementation
