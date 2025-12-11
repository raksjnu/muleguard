# Regex Special Characters - Escaping Guide

## CRITICAL: YAML Double-Escaping

**In YAML files, you need FOUR backslashes (`\\\\`) for each special character!**

Why? Because:
1. **YAML processes the string first**: `\\\\` becomes `\\`
2. **Regex processes it second**: `\\` becomes a literal character

## Problem
When using regex patterns in RULE-103 or RULE-104, certain characters have special meanings in regex and must be escaped.

## Special Characters That Need Escaping

| Character | Meaning in Regex | In YAML | Example |
|-----------|------------------|---------|---------|
| `.` | Any character | `\\\\.` | `app\\\\.name` matches "app.name" |
| `*` | Zero or more | `\\\\*` | `value\\\\*` matches "value*" |
| `+` | One or more | `\\\\+` | `count\\\\+` matches "count+" |
| `?` | Zero or one | `\\\\?` | `optional\\\\?` matches "optional?" |
| `^` | Start of string | `\\\\^` | `\\\\^start` matches "^start" |
| `$` | End of string | `\\\\$` | `price\\\\$` matches "price$" |
| `{` `}` | Repetition | `\\\\{` `\\\\}` | `\\\\{value\\\\}` matches "{value}" |
| `[` `]` | Character class | `\\\\[` `\\\\]` | `\\\\[item\\\\]` matches "[item]" |
| `(` `)` | Grouping | `\\\\(` `\\\\)` | `\\\\(note\\\\)` matches "(note)" |
| `|` | Alternation | `\\\\|` | `a\\\\|b` matches "a|b" |
| `\` | Escape character | `\\\\\\\\` | `path\\\\\\\\to` matches "path\to" |

## Common Patterns

### 1. Secure Encrypted Properties
**Property**: `secure::somename=^{someencryptedvalue=}`

**Wrong** ❌:
```yaml
- "secure::*=^{*=}"  # Error: Illegal repetition
```

**Wrong** ❌:
```yaml
- "secure::[^=]*=\\^\\{[^=]*=\\}"  # Error: YAML consumes backslashes
```

**Correct** ✅:
```yaml
- "secure::[^=]*=\\\\^\\\\{[^=]*=\\\\}"  # Four backslashes in YAML!
```

**Breakdown**:
- `secure::` - Literal text
- `[^=]*` - Any characters except `=` (property name)
- `=` - Literal equals
- `\\\\^` - Literal `^` (four backslashes in YAML → two in regex → one literal)
- `\\\\{` - Literal `{` (four backslashes in YAML)
- `[^=]*` - Any characters except `=` (encrypted value)
- `=` - Literal equals
- `\\\\}` - Literal `}` (four backslashes in YAML)

### 2. URL Properties
**Property**: `api.url=https://example.com`

**Wrong** ❌:
```yaml
- "api.url=https://.*"  # Matches "apixurl" too!
```

**Correct** ✅:
```yaml
- "api\\.url=https://.*"  # Only matches "api.url"
```

### 3. Version Numbers
**Property**: `version=1.2.3`

**Wrong** ❌:
```yaml
- "version=[0-9].[0-9].[0-9]"  # Dot matches any character
```

**Correct** ✅:
```yaml
- "version=[0-9]+\\.[0-9]+\\.[0-9]+"  # Literal dots
```

### 4. Dollar Signs in Values
**Property**: `price=$100`

**Wrong** ❌:
```yaml
- "price=$.*"  # $ means end of string!
```

**Correct** ✅:
```yaml
- "price=\\$.*"  # Literal dollar sign
```

## Quick Reference

### Match Literal Characters
```yaml
# Match: app.name=value
- "app\\.name=.*"

# Match: count*=5
- "count\\*=.*"

# Match: price$=100
- "price\\$=.*"

# Match: {key}=value
- "\\{key\\}=.*"

# Match: [item]=value
- "\\[item\\]=.*"
```

### Match Patterns (Don't Escape)
```yaml
# Any characters
- ".*"

# One or more digits
- "[0-9]+"

# Specific values
- "(INFO|WARN|ERROR)"

# Character class
- "[a-zA-Z]+"
```

## Testing Your Regex

Use an online regex tester:
1. Go to https://regex101.com/
2. Select "Java" flavor
3. Test your pattern against sample properties

## Common Mistakes

❌ **Mistake**: `secure::*=^{*=}`
- `*` without preceding character
- `^` and `{` not escaped

✅ **Fix**: `secure::[^=]*=\\^\\{[^=]*=\\}`
- `[^=]*` matches any characters except `=`
- `\\^` and `\\{` are properly escaped

---

**Remember**: In YAML strings, you need **double backslashes** (`\\`) because YAML processes the first backslash, and regex processes the second.
