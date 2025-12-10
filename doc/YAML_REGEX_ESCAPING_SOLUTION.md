# YAML Regex Escaping - Final Solution

## The Winning Pattern

```yaml
regexPatterns:
  - 'secure::[^=]*=\\^\\{[^=]*=\\}'
```

## Why This Works

**Single quotes** + **Double backslashes** = Perfect!

1. **Single quotes (`'`)**: Prevent YAML from processing backslashes
2. **Double backslashes (`\\`)**: Regex engine processes these as single backslash escapes

## The Journey

### ❌ Attempt 1: No escaping
```yaml
- "secure::*=^{*=}"
```
**Error**: `Illegal repetition` - `*` without preceding character

### ❌ Attempt 2: Double quotes with double backslashes  
```yaml
- "secure::[^=]*=\\^\\{[^=]*=\\}"
```
**Error**: `Unclosed character class` - YAML consumed one backslash

### ❌ Attempt 3: Double quotes with quad backslashes
```yaml
- "secure::[^=]*=\\\\^\\\\{[^=]*=\\\\}"
```
**Error**: Still `Unclosed character class` - confusing escaping

### ❌ Attempt 4: Single quotes with single backslashes
```yaml
- 'secure::[^=]*=\^\{[^=]*=\}'
```
**Error**: `Unclosed character class` - regex needs escaped special chars

### ✅ SOLUTION: Single quotes with double backslashes
```yaml
- 'secure::[^=]*=\\^\\{[^=]*=\\}'
```
**Success!** ✅ No errors, pattern works perfectly

## Rule for YAML Regex Patterns

**Always use this format**:

```yaml
regexPatterns:
  - 'pattern with \\special \\chars'
```

- **Single quotes** (`'...'`) - prevents YAML escaping
- **Double backslash** (`\\`) - for regex special characters

## Examples

```yaml
# Literal dot
- 'app\\.name=.*'

# Literal caret and braces
- 'secure::[^=]*=\\^\\{[^=]*=\\}'

# Literal dollar sign
- 'price=\\$[0-9]+'

# Literal parentheses
- 'value=\\(.*\\)'

# Literal brackets
- 'item=\\[.*\\]'
```

## Quick Reference

| To Match | In YAML (single quotes) | Explanation |
|----------|-------------------------|-------------|
| `app.name` | `'app\\.name'` | Literal dot |
| `^{value}` | `'\\^\\{value\\}'` | Literal `^` and `{}` |
| `$100` | `'\\$100'` | Literal `$` |
| `(note)` | `'\\(note\\)'` | Literal `()` |
| `[item]` | `'\\[item\\]'` | Literal `[]` |

---

**Bottom Line**: Use single quotes with double backslashes for all regex patterns in YAML!
