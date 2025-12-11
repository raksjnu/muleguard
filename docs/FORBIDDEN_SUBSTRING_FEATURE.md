# Forbidden Substring Check Feature (RULE-107 & RULE-108)

## Overview
Added support for **forbidden substring checks** that fail if specified tokens are found in configuration files. This is the opposite of mandatory substring checks.

---

## Implementation

### **searchMode Parameter**
Updated `MandatorySubstringCheck` to support a `searchMode` parameter:

- **`searchMode: REQUIRED`** (default) → Token MUST exist (fail if NOT found)
- **`searchMode: FORBIDDEN`** → Token must NOT exist (fail if found)

---

## New Rules

### **RULE-107: Forbidden Substring Check for .properties Files**
```yaml
- id: "RULE-107"
  name: "Forbidden substring check for .properties files"
  description: "Fails if forbidden tokens are found in .properties files"
  enabled: true
  severity: HIGH
  checks:
    - type: MANDATORY_SUBSTRING_CHECK
      params:
        fileExtensions: [".properties"]
        caseSensitive: true
        searchMode: FORBIDDEN  # Fail if token is found
        environments: ["ALL"]
        tokens:
          - "hardcoded.password"
          - "TODO"
          - "FIXME"
```

### **RULE-108: Forbidden Substring Check for .policy Files**
```yaml
- id: "RULE-108"
  name: "Forbidden substring check for .policy files"
  description: "Fails if forbidden tokens are found in .policy files"
  enabled: true
  severity: HIGH
  checks:
    - type: MANDATORY_SUBSTRING_CHECK
      params:
        fileExtensions: [".policy"]
        caseSensitive: true
        searchMode: FORBIDDEN  # Fail if token is found
        environments: ["ALL"]
        tokens:
          - "deprecated.policy"
          - "old.version"
```

---

## Usage Examples

### **Forbidden Tokens (Fail if Found)**
```yaml
params:
  searchMode: FORBIDDEN
  tokens:
    - "hardcoded.password"
    - "TODO"
    - "FIXME"
    - "localhost"
    - "127.0.0.1"
```

### **Required Tokens (Fail if NOT Found)**
```yaml
params:
  searchMode: REQUIRED  # or omit (default)
  tokens:
    - "apiId"
    - "environment"
```

---

## Flexibility

✅ **Works with any file extension**: `.properties`, `.policy`, `.xml`, `.yaml`, `.txt`, etc.  
✅ **Case sensitivity**: Configurable via `caseSensitive` parameter  
✅ **Environment selection**: Use `["ALL"]` or specific environments  
✅ **Reusable**: One check type handles both positive and negative validation  

---

## Common Use Cases

### **Security Checks**
```yaml
tokens:
  - "hardcoded.password"
  - "secret.key="
  - "admin.password"
```

### **Code Quality Checks**
```yaml
tokens:
  - "TODO"
  - "FIXME"
  - "HACK"
  - "XXX"
```

### **Environment Checks**
```yaml
tokens:
  - "localhost"
  - "127.0.0.1"
  - "dev.server"
```

### **Deprecated Features**
```yaml
tokens:
  - "deprecated.api"
  - "old.version"
  - "legacy.feature"
```

---

## Files Modified

1. **`MandatorySubstringCheck.java`**: Added `searchMode` parameter support
2. **`rules.yaml`**: Added RULE-107 and RULE-108

---

## Build Status

✅ **BUILD SUCCESS** (`mvn clean compile -DskipTests`)

The feature is ready to use!
