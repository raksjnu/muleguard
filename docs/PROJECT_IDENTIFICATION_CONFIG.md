# Externalized Project Identification Configuration

## Overview
Project identification logic has been fully externalized to `rules.yaml`, making it easy to customize without modifying Java code.

## Configuration Location
`src/main/resources/rules/rules.yaml` → `config.projectIdentification`

---

## Configuration Structure

```yaml
config:
  projectIdentification:
    # Config Folder Identification
    configFolder:
      namePattern: ".*_config.*"
    
    # Mule API Project Identification  
    muleApiProject:
      markerFiles:
        - "pom.xml"
        - "mule-artifact.json"
    
    # Folders to Ignore
    ignoredFolders:
      exactNames:
        - "muleguard-reports"
        - "target"
        - "bin"
        - "build"
      prefixes:
        - "."
```

---

## 1. Config Folder Identification

### **Parameter:** `configFolder.namePattern`
**Type:** String (Regex)  
**Purpose:** Identifies configuration folders by name pattern

### **Default:**
```yaml
namePattern: ".*_config.*"
```

### **How It Works:**
- Matches any folder name containing `_config`
- Uses Java regex pattern matching

### **Examples:**

**Match these folders:**
- `muleapp1_config` ✅
- `api_config_v2` ✅
- `test_config` ✅
- `config_backup` ✅

**Don't match these:**
- `muleapp1` ❌
- `customerOrder` ❌

### **Customization Examples:**

**Match folders ending with `_config`:**
```yaml
namePattern: ".*_config$"
```

**Match folders with `_config` OR `-config`:**
```yaml
namePattern: ".*(_config|-config).*"
```

**Match specific config folders:**
```yaml
namePattern: "(prod_config|dev_config|test_config)"
```

---

## 2. Mule API Project Identification

### **Parameters:**
- `muleApiProject.matchMode` - String ("ANY" or "ALL")
- `muleApiProject.markerFiles` - List of Strings

**Purpose:** Identifies Mule API projects by presence of specific files

### **Default:**
```yaml
muleApiProject:
  matchMode: "ALL"  # Require ALL marker files (AND logic)
  markerFiles:
    - "pom.xml"
    - "mule-artifact.json"
```

### **Match Modes:**

**`matchMode: "ALL"` (AND Logic - Recommended)**
- **ALL** marker files must exist
- More precise - avoids false positives
- Example: Requires BOTH `pom.xml` AND `mule-artifact.json`
- **Use case:** Avoid matching generic Maven projects that only have `pom.xml`

**`matchMode: "ANY"` (OR Logic)**
- **At least ONE** marker file must exist
- More permissive
- Example: Matches if EITHER `pom.xml` OR `mule-artifact.json` exists
- **Use case:** When projects might have different build systems

### **How It Works:**

**With `matchMode: "ALL"`:**
```
Folder: customerOrder/
  ├── pom.xml ✅
  ├── mule-artifact.json ✅
  └── src/
Result: ✅ Identified as Mule API project (both files present)

Folder: generic-maven-project/
  ├── pom.xml ✅
  └── src/
Result: ❌ NOT identified (missing mule-artifact.json)
```

**With `matchMode: "ANY"`:**
```
Folder: customerOrder/
  ├── pom.xml ✅
  └── src/
Result: ✅ Identified as Mule API project (has pom.xml)

Folder: gradle-mule-project/
  ├── build.gradle ✅
  └── src/
Result: ✅ Identified (if build.gradle is in markerFiles)
```

### **Customization Examples:**

**Strict Mule detection (recommended):**
```yaml
muleApiProject:
  matchMode: "ALL"
  markerFiles:
    - "pom.xml"
    - "mule-artifact.json"
```

**Support multiple build systems:**
```yaml
muleApiProject:
  matchMode: "ANY"
  markerFiles:
    - "pom.xml"
    - "build.gradle"
    - "mule-artifact.json"
```

**Maven + Gradle with strict validation:**
```yaml
muleApiProject:
  matchMode: "ALL"
  markerFiles:
    - "pom.xml"
    - "build.gradle"
    - "mule-artifact.json"
# Note: This requires ALL three files - very strict!
```

---

## 3. Ignored Folders

### **Purpose:** Skip specific folders during validation

### **3.1 Exact Names** (`ignoredFolders.exactNames`)
**Type:** List of Strings  
**Purpose:** Ignore folders with exact name match (case-sensitive)

**Default:**
```yaml
exactNames:
  - "muleguard-reports"
  - "target"
  - "bin"
  - "build"
  - ".git"
  - ".idea"
  - ".vscode"
  - "node_modules"
```

**Customization:**
```yaml
exactNames:
  - "muleguard-reports"
  - "target"
  - "dist"
  - "out"
  - "temp"
  - "backup"
```

### **3.2 Prefixes** (`ignoredFolders.prefixes`)
**Type:** List of Strings  
**Purpose:** Ignore folders starting with specific prefixes

**Default:**
```yaml
prefixes:
  - "."  # Ignore all hidden folders
```

**Customization:**
```yaml
prefixes:
  - "."      # Hidden folders (.git, .idea, etc.)
  - "temp_"  # Temporary folders
  - "old_"   # Old/archived folders
  - "bak_"   # Backup folders
```

---

## Complete Example

### **Scenario:** Custom project structure

```yaml
config:
  projectIdentification:
    # Config folders end with "-config" or "_config"
    configFolder:
      namePattern: ".*(-config|_config)$"
    
    # Support Maven, Gradle, and custom marker
    muleApiProject:
      markerFiles:
        - "pom.xml"
        - "build.gradle"
        - "mule-artifact.json"
        - "project.properties"
    
    # Ignore additional folders
    ignoredFolders:
      exactNames:
        - "muleguard-reports"
        - "target"
        - "build"
        - "dist"
        - "node_modules"
        - "archive"
        - "backup"
      
      prefixes:
        - "."       # Hidden folders
        - "temp_"   # Temporary folders
        - "old_"    # Old versions
        - "test_"   # Test folders
```

---

## Benefits

✅ **No Code Changes Required** - Modify behavior via YAML only  
✅ **Easy Customization** - Adapt to different project structures  
✅ **Flexible Patterns** - Use regex for complex matching  
✅ **Multiple Markers** - Support various project types  
✅ **Granular Control** - Ignore folders by name or prefix  

---

## Migration from Old Configuration

### **Old Format (Deprecated):**
```yaml
config:
  folderPattern: ".*_config.*"
```

### **New Format:**
```yaml
config:
  projectIdentification:
    configFolder:
      namePattern: ".*_config.*"
    muleApiProject:
      markerFiles:
        - "pom.xml"
        - "mule-artifact.json"
    ignoredFolders:
      exactNames: [...]
      prefixes: [...]
```

**Note:** The old `folderPattern` is still supported for backward compatibility but is deprecated.

---

## Testing Your Configuration

1. **Update `rules.yaml`** with your custom patterns
2. **Run validation:**
   ```bash
   java -jar target/muleguard-1.0.0-jar-with-dependencies.jar -p ./testData
   ```
3. **Verify output** shows correct project detection

---

## Build Status

✅ **BUILD SUCCESS**  
✅ All project identification logic externalized  
✅ Fully configurable via `rules.yaml`  
✅ Backward compatible with old configuration  
