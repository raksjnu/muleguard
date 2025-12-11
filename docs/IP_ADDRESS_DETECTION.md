# IP Address Detection Rules (RULE-109 & RULE-110)

## Overview
Added rules to detect and block IP addresses in configuration files, enforcing a **hostname-only policy**.

---

## Rules Added

### **RULE-109: IP Address Detection in .properties Files**
Detects IPv4 addresses and localhost references in `.properties` files.

### **RULE-110: IP Address Detection in .policy Files**
Detects IPv4 addresses and localhost references in `.policy` files.

---

## Configuration

```yaml
# RULE-109: IP Address Detection in .properties files
- id: "RULE-109"
  name: "IP address detection in .properties files"
  description: "Fails if IP addresses are found (hostname-only policy)"
  enabled: true
  severity: HIGH
  checks:
    - type: GENERIC_TOKEN_SEARCH
      params:
        filePatterns: [".properties"]
        searchMode: FORBIDDEN  # Fail if IP found
        matchMode: REGEX
        tokens:
          # IPv4 pattern: xxx.xxx.xxx.xxx
          - '\b(?:[0-9]{1,3}\.){3}[0-9]{1,3}\b'
          # Localhost variations
          - '\blocalhost\b'
          - '\b127\.0\.0\.1\b'
```

---

## What Gets Detected

### ❌ **BLOCKED (IP Addresses)**
```properties
server.url=http://192.168.1.100:8080
database.host=10.0.0.50
api.endpoint=https://172.16.0.1/api
localhost.url=http://127.0.0.1:8081
cache.server=localhost:6379
```

### ✅ **ALLOWED (Hostnames)**
```properties
server.url=https://api.example.com
database.host=db.internal.company.com
api.endpoint=https://prod-api.mycompany.com/v1
cache.server=redis.internal:6379
```

---

## Reversing the Logic

**To REQUIRE IP addresses instead** (enforce IPs, block hostnames):

```yaml
params:
  searchMode: REQUIRED  # Change from FORBIDDEN to REQUIRED
  matchMode: REGEX
  tokens:
    - '\b(?:[0-9]{1,3}\.){3}[0-9]{1,3}\b'
```

This would:
- ✅ PASS if IP addresses are found
- ❌ FAIL if NO IP addresses are found

---

## Regex Pattern Explanation

**IPv4 Pattern**: `\b(?:[0-9]{1,3}\.){3}[0-9]{1,3}\b`

- `\b` = Word boundary (ensures exact match)
- `[0-9]{1,3}` = 1-3 digits
- `\.` = Literal dot
- `{3}` = Repeat 3 times (for first 3 octets)
- `[0-9]{1,3}` = Last octet

**Matches**: `192.168.1.1`, `10.0.0.50`, `172.16.0.100`  
**Does NOT match**: `api.example.com`, `db-server`, `1234.5678.9.1` (invalid IP)

---

## Common Use Cases

### **Security & Compliance**
- Prevent hardcoded IP addresses in production configs
- Enforce DNS-based service discovery
- Ensure environment portability

### **Best Practices**
- Use hostnames for better flexibility
- Enable DNS-based load balancing
- Avoid environment-specific IPs in configs

---

## Build Status

✅ **BUILD SUCCESS** (`mvn clean compile -DskipTests`)

The rules are ready to use!
