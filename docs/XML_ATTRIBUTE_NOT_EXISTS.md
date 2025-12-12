# XML_ATTRIBUTE_NOT_EXISTS

## Overview

Validates that **forbidden XML attributes do NOT exist** on specified elements. This rule **fails** if forbidden attributes **ARE found**.

## Use Cases

- Prevent deprecated attributes
- Block hardcoded values in specific attributes
- Enforce architectural constraints
- Disallow anti-patterns

## Parameters

### Required Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `filePatterns` | List<String> | Glob patterns to match XML files |
| `elementName` | String | XML element name to check |
| `forbiddenAttributes` | List<Map> | List of forbidden attributes with optional value patterns |

#### Forbidden Attribute Map Structure

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `name` | String | Yes | Attribute name that should NOT exist |
| `values` | List<String> | No | If specified, only fail if attribute has one of these values |

### Optional Parameters

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `caseSensitiveNames` | Boolean | `true` | Case sensitivity for attribute names |
| `caseSensitiveValues` | Boolean | `true` | Case sensitivity for attribute values |

## Configuration Examples

### Example 1: Block Deprecated Attribute

```yaml
- id: "RULE-050"
  name: "No Deprecated processingStrategy"
  description: "Prevent usage of deprecated processingStrategy attribute"
  enabled: true
  severity: MEDIUM
  checks:
    - type: XML_ATTRIBUTE_NOT_EXISTS
      params:
        filePatterns:
          - "**/*.xml"
        elementName: "flow"
        forbiddenAttributes:
          - name: "processingStrategy"
```

### Example 2: Prevent Hardcoded Values

```yaml
- id: "RULE-051"
  name: "No Hardcoded Localhost"
  description: "HTTP listener should not use hardcoded localhost"
  enabled: true
  severity: HIGH
  checks:
    - type: XML_ATTRIBUTE_NOT_EXISTS
      params:
        filePatterns:
          - "**/*.xml"
        elementName: "http:listener-config"
        forbiddenAttributes:
          - name: "host"
            values: ["localhost", "127.0.0.1"]
```

### Example 3: Block Specific Configurations

```yaml
- id: "RULE-052"
  name: "No Synchronous Processing"
  description: "Flows should not use synchronous processing strategy"
  enabled: true
  severity: MEDIUM
  checks:
    - type: XML_ATTRIBUTE_NOT_EXISTS
      params:
        filePatterns:
          - "src/main/mule/**/*.xml"
        elementName: "flow"
        forbiddenAttributes:
          - name: "processingStrategy"
            values: ["synchronous"]
```

## Error Messages

```
config.xml element 'flow' has forbidden attribute: processingStrategy
global-config.xml element 'http:listener-config' has forbidden attribute 'host' with value: localhost
```

## Related Rule Types

- **[XML_ATTRIBUTE_EXISTS](XML_ATTRIBUTE_EXISTS.md)** - Opposite: ensures attributes DO exist
- **[XML_XPATH_NOT_EXISTS](XML_XPATH_NOT_EXISTS.md)** - More complex XPath-based validation
