# JSON_VALIDATION_REQUIRED

## Overview

Validates that **required JSON elements exist** in JSON files. This rule **fails** if required elements are **NOT found**.

## Use Cases

- Ensure required configuration keys exist in JSON files
- Validate mule-artifact.json structure
- Check for mandatory metadata in JSON configs
- Enforce JSON schema compliance

## Parameters

### Required Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `filePattern` | String | Filename pattern to match (e.g., `mule-artifact.json`) |
| `requiredElements` | List<String> | List of required JSON keys (top-level only) |

## Configuration Examples

### Example 1: Validate mule-artifact.json

```yaml
- id: "RULE-100"
  name: "Mule Artifact Required Fields"
  description: "mule-artifact.json must have required fields"
  enabled: true
  severity: HIGH
  checks:
    - type: JSON_VALIDATION_REQUIRED
      params:
        filePattern: "mule-artifact.json"
        requiredElements:
          - "minMuleVersion"
          - "classLoaderModelLoaderDescriptor"
```

### Example 2: Validate Configuration JSON

```yaml
- id: "RULE-101"
  name: "App Config Required Fields"
  description: "Application config JSON must have required fields"
  enabled: true
  severity: MEDIUM
  checks:
    - type: JSON_VALIDATION_REQUIRED
      params:
        filePattern: "app-config.json"
        requiredElements:
          - "appName"
          - "version"
          - "environment"
```

## Error Messages

```
mule-artifact.json is missing required element: minMuleVersion
app-config.json is missing required element: appName
```

## Related Rule Types

- **[JSON_VALIDATION_FORBIDDEN](JSON_VALIDATION_FORBIDDEN.md)** - Opposite: ensures elements do NOT exist
- **[GENERIC_TOKEN_SEARCH_REQUIRED](GENERIC_TOKEN_SEARCH_REQUIRED.md)** - More flexible JSON content validation
