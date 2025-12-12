# XML_ATTRIBUTE_EXISTS

## Overview

Validates that **required XML attributes exist** on specified elements. This rule **fails** if required attributes are **NOT found**.

## Use Cases

- Ensure flow elements have required attributes (name, doc:id)
- Validate configuration completeness
- Enforce naming conventions
- Check for mandatory metadata

## Parameters

### Required Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `filePatterns` | List<String> | Glob patterns to match XML files |
| `elementName` | String | XML element name to check |
| `requiredAttributes` | List<Map> | List of required attributes with optional value validation |

#### Required Attribute Map Structure

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `name` | String | Yes | Attribute name |
| `values` | List<String> | No | If specified, attribute value must match one of these |
| `caseSensitive` | Boolean | No | Override global case sensitivity for this attribute |

### Optional Parameters

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `caseSensitiveNames` | Boolean | `true` | Case sensitivity for attribute names |
| `caseSensitiveValues` | Boolean | `true` | Case sensitivity for attribute values |
| `propertyResolution` | Boolean | `false` | Enable `${property}` placeholder resolution |

## Configuration Examples

### Example 1: Basic - Ensure Flow Has Name

```yaml
- id: "RULE-040"
  name: "Flow Name Required"
  description: "All flow elements must have a name attribute"
  enabled: true
  severity: HIGH
  checks:
    - type: XML_ATTRIBUTE_EXISTS
      params:
        filePatterns:
          - "src/main/mule/**/*.xml"
        elementName: "flow"
        requiredAttributes:
          - name: "name"
```

### Example 2: Validate Attribute Values

```yaml
- id: "RULE-041"
  name: "HTTP Method Validation"
  description: "HTTP request must use allowed methods"
  enabled: true
  severity: MEDIUM
  checks:
    - type: XML_ATTRIBUTE_EXISTS
      params:
        filePatterns:
          - "**/*.xml"
        elementName: "http:request"
        requiredAttributes:
          - name: "method"
            values: ["GET", "POST", "PUT", "DELETE", "PATCH"]
```

### Example 3: Multiple Required Attributes

```yaml
- id: "RULE-042"
  name: "Logger Configuration Complete"
  description: "Logger must have level and message attributes"
  enabled: true
  severity: LOW
  checks:
    - type: XML_ATTRIBUTE_EXISTS
      params:
        filePatterns:
          - "src/main/mule/**/*.xml"
        elementName: "logger"
        requiredAttributes:
          - name: "level"
            values: ["DEBUG", "INFO", "WARN", "ERROR"]
          - name: "message"
```

### Example 4: Property Placeholder Validation

```yaml
- id: "RULE-043"
  name: "Externalized Configuration"
  description: "HTTP listener must use property placeholders for host and port"
  enabled: true
  severity: HIGH
  checks:
    - type: XML_ATTRIBUTE_EXISTS
      params:
        filePatterns:
          - "**/global-config.xml"
        elementName: "http:listener-config"
        requiredAttributes:
          - name: "host"
          - name: "port"
        propertyResolution: true
```

## Error Messages

```
config.xml element 'flow' is missing required attribute: name
api-main.xml element 'http:request' attribute 'method' has invalid value (expected one of: GET, POST, PUT, DELETE, PATCH)
```

## Related Rule Types

- **[XML_ATTRIBUTE_NOT_EXISTS](XML_ATTRIBUTE_NOT_EXISTS.md)** - Opposite: ensures attributes do NOT exist
- **[XML_XPATH_EXISTS](XML_XPATH_EXISTS.md)** - More complex XPath-based validation
