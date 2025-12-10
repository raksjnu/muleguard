# Whitespace Handling in Regex Pattern Validation - Quick Reference

## ✅ YES - Whitespace is Automatically Handled!

The `GenericPropertyFileCheck` with `parseMode: "REGEX_PATTERN"` **automatically trims whitespace** from both property names and values.

## Your Pattern

```yaml
regexPatterns:
  - "headerinjection.policy.version=1.3.2"
```

## Will Match ALL of These

```properties
✅ headerinjection.policy.version=1.3.2
✅ headerinjection.policy.version = 1.3.2
✅ headerinjection.policy.version =1.3.2
✅ headerinjection.policy.version= 1.3.2
✅ headerinjection.policy.version  =  1.3.2
```

## How It Works

The code automatically trims whitespace:

```java
String propName = line.substring(0, equalsIndex).trim();   // Removes leading/trailing spaces
String propValue = line.substring(equalsIndex + 1).trim(); // Removes leading/trailing spaces
```

## What This Means

- ✅ **No need to add `\s*` to your patterns**
- ✅ **Write patterns as if there's no whitespace**
- ✅ **Works with any amount of whitespace**
- ✅ **Handles tabs and spaces**

## Example Patterns

```yaml
# Exact match (will handle whitespace automatically)
- "headerinjection.policy.version=1.3.2"

# Regex pattern (whitespace still handled)
- ".*\\.version=[0-9]+\\.[0-9]+\\.[0-9]+"

# Multiple alternatives
- "log\\.level=(INFO|WARN|ERROR)"
```

## Testing

Test file created at: `testData/test-config/dev.properties`

Run validation:
```bash
java -jar target/muleguard-1.0.0-jar-with-dependencies.jar -p testData/test-config
```

---

**Bottom Line**: Your pattern `headerinjection.policy.version=1.3.2` will work perfectly with all whitespace variations! 🎉
