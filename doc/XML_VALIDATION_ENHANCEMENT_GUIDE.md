# GenericXmlValidationCheck - Enhanced Features Guide

## New Feature: Multiple XPath Checks in One Rule

The `GenericXmlValidationCheck` now supports checking **multiple XPath expressions** in a single rule configuration!

---

## Configuration Options

### Option 1: Single XPath (Original Format - Still Supported)
```yaml
- id: "RULE-008"
  checks:
    - type: XML_XPATH_EXISTS
      params:
        validationType: EXISTS
        xpath: "//*[local-name()='autodiscovery']"
        failureMessage: "Autodiscovery element missing"
```

### Option 2: Multiple XPaths (NEW!)
```yaml
- id: "RULE-XML-MULTI-CHECK"
  name: "Multiple XML Element Validations"
  checks:
    - type: GENERIC_XML_VALIDATION
      params:
        validationType: EXISTS
        path: "src/main/mule/*.xml"
        # Use 'xpaths' (plural) for multiple checks
        xpaths:
          - "//*[local-name()='autodiscovery']"
          - "//*[local-name()='logger']"
          - "//http:listener-config"
        failureMessage: "One or more required elements are missing"
```

### Option 3: Mixed Single + Multiple
```yaml
- id: "RULE-XML-COMBINED"
  checks:
    - type: GENERIC_XML_VALIDATION
      params:
        validationType: NOT_EXISTS
        # Both 'xpath' and 'xpaths' can be used together!
        xpath: "//*[local-name()='deprecated-element']"
        xpaths:
          - "//*[local-name()='old-connector']"
          - "//*[local-name()='legacy-component']"
```

---

## Validation Types Supported

### 1. EXISTS - Element MUST exist
```yaml
validationType: EXISTS
xpaths:
  - "//*[local-name()='autodiscovery']"
  - "//http:listener-config"
  - "//secure-properties:config"
```
**Result**: PASS if ALL XPaths are found, FAIL if ANY is missing

### 2. NOT_EXISTS - Element must NOT exist
```yaml
validationType: NOT_EXISTS
xpaths:
  - "//*[local-name()='deprecated-connector']"
  - "//*[local-name()='old-api-gateway']"
```
**Result**: PASS if NONE are found, FAIL if ANY is found

### 3. ATTRIBUTE_VALUE - Check attribute values
```yaml
validationType: ATTRIBUTE_VALUE
xpath: "//ibm-mq:connection/@cipherSuite"
expectedValue: "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384"
propertyResolution: true
```

### 4. ATTRIBUTE_EXISTS - Attribute must be present
```yaml
validationType: ATTRIBUTE_EXISTS
elementName: "crypto:jce-config"
requiredAttribute: "type"
```

### 5. FORBIDDEN_VALUE - Element must NOT contain value
```yaml
validationType: FORBIDDEN_VALUE
elementName: "crypto:jce-encrypt-pbe"
forbiddenValue: "PBEWithHmacSHA256AndAES_256"
```

### 6. FORBIDDEN_ATTRIBUTE - Attributes must NOT exist
```yaml
validationType: FORBIDDEN_ATTRIBUTE
elements:
  - "request_in"
  - "request_out"
attributes:
  - "fromApplicationCode"
  - "toApplicationCode"
```

---

## Real-World Examples

### Example 1: Verify Multiple Required Connectors
```yaml
- id: "RULE-REQUIRED-CONNECTORS"
  name: "Verify all required connectors are configured"
  enabled: true
  severity: HIGH
  checks:
    - type: GENERIC_XML_VALIDATION
      params:
        validationType: EXISTS
        path: "src/main/mule/*.xml"
        xpaths:
          - "//http:listener-config"           # HTTP listener
          - "//db:config"                       # Database config
          - "//api-gateway:autodiscovery"       # API Gateway
          - "//secure-properties:config"        # Secure properties
        failureMessage: "Missing one or more required connector configurations"
```

### Example 2: Check for Deprecated Elements
```yaml
- id: "RULE-NO-DEPRECATED"
  name: "Ensure no deprecated elements are used"
  enabled: true
  severity: MEDIUM
  checks:
    - type: GENERIC_XML_VALIDATION
      params:
        validationType: NOT_EXISTS
        path: "src/main/mule/*.xml"
        xpaths:
          - "//*[local-name()='spring-security']"
          - "//*[local-name()='mule-module-spring-config']"
          - "//*[local-name()='db2jcc']"
        failureMessage: "Found deprecated elements that must be removed"
```

### Example 3: Merge RULE-005 + RULE-008 (Optional)
```yaml
# Before: Two separate rules
- id: "RULE-005"
  checks:
    - type: IBM_MQ_CIPHER_CHECK
      params:
        validationType: ATTRIBUTE_VALUE
        xpath: "//ibm-mq:connection/@cipherSuite"
        expectedValue: "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384"

- id: "RULE-008"
  checks:
    - type: XML_XPATH_EXISTS
      params:
        validationType: EXISTS
        xpath: "//*[local-name()='autodiscovery']"

# After: Can be merged (but not required - backward compatibility maintained)
- id: "RULE-XML-VALIDATIONS"
  name: "Combined XML Validations"
  checks:
    - type: GENERIC_XML_VALIDATION
      params:
        validationType: EXISTS
        xpaths:
          - "//*[local-name()='autodiscovery']"
    - type: GENERIC_XML_VALIDATION
      params:
        validationType: ATTRIBUTE_VALUE
        xpath: "//ibm-mq:connection/@cipherSuite"
        expectedValue: "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384"
        propertyResolution: true
```

---

## Output Examples

### Single XPath - PASS
```
✓ XPath found: //*[local-name()='autodiscovery']
```

### Multiple XPaths - PASS
```
All XPath validations passed:
✓ XPath found: //*[local-name()='autodiscovery']
✓ XPath found: //http:listener-config
✓ XPath found: //db:config
```

### Multiple XPaths - FAIL
```
XPath validation failures:
✗ XPath not found: //http:listener-config
✗ XPath not found: //secure-properties:config
```

### NOT_EXISTS - PASS
```
All XPath validations passed:
✓ XPath not found (as expected): //*[local-name()='deprecated-element']
✓ XPath not found (as expected): //*[local-name()='old-connector']
```

---

## Benefits

✅ **Consolidate Multiple Rules**: Check 10+ XPaths in one rule instead of 10 separate rules  
✅ **Backward Compatible**: Old 'xpath' parameter still works  
✅ **Clear Output**: Shows exactly which XPaths passed/failed  
✅ **Flexible**: Mix EXISTS and NOT_EXISTS checks as needed  
✅ **Maintainable**: Add new XPath checks via config only

---

## Migration Guide

**No migration required!** All existing rules continue to work.

**Optional Enhancement**: If you have multiple XML validation rules, you can optionally consolidate them:

```yaml
# Before: 3 separate rules
- id: "RULE-A"
  checks:
    - type: XML_XPATH_EXISTS
      params:
        xpath: "//element-a"

- id: "RULE-B"
  checks:
    - type: XML_XPATH_EXISTS
      params:
        xpath: "//element-b"

- id: "RULE-C"
  checks:
    - type: XML_XPATH_EXISTS
      params:
        xpath: "//element-c"

# After: 1 consolidated rule (optional)
- id: "RULE-ABC"
  checks:
    - type: GENERIC_XML_VALIDATION
      params:
        validationType: EXISTS
        xpaths:
          - "//element-a"
          - "//element-b"
          - "//element-c"
```

---

**Enhancement Complete!** 🎉  
Multiple XPath checks now supported in GenericXmlValidationCheck.
