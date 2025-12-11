# New Feature: Optional --config Parameter

## Overview
Added support for an optional `--config` parameter that allows you to specify an external `rules.yaml` file without rebuilding the JAR.

---

## Usage

### **Command Line with Custom Config**
```bash
java -jar muleguard-1.0.0-jar-with-dependencies.jar -p <folder> --config <path-to-rules.yaml>
```

**Example**:
```bash
java -jar .\target\muleguard-1.0.0-jar-with-dependencies.jar -p .\testData --config src\main\resources\rules\rules.yaml
```

### **Command Line with Default Config**
```bash
java -jar muleguard-1.0.0-jar-with-dependencies.jar -p <folder>
```

Uses the embedded `rules.yaml` from the JAR (packaged during `mvn package`).

### **GUI Mode (Double-Click)**
```bash
java -jar muleguard-1.0.0-jar-with-dependencies.jar
```

Opens folder selection dialog and uses the embedded `rules.yaml`.

---

## Behavior

### **With --config Parameter**
1. Attempts to load the specified external `rules.yaml` file
2. If file is found → Uses the external config
3. If file is NOT found → Falls back to embedded `rules.yaml` with warning message

### **Without --config Parameter**
- Uses the embedded `rules.yaml` from the JAR (default behavior)

### **Error Handling**
- If external config file cannot be read → Falls back to embedded rules.yaml
- If embedded rules.yaml is missing → Exits with error

---

## Benefits

✅ **No rebuild required**: Test rule changes without running `mvn package`  
✅ **Flexible testing**: Quickly iterate on rule configurations  
✅ **Backward compatible**: Existing usage without `--config` works exactly as before  
✅ **GUI compatible**: Double-click behavior unchanged  
✅ **Safe fallback**: Automatically uses embedded rules if external file fails  

---

## Examples

### **Test New Rules Without Rebuilding**
```bash
# Edit rules.yaml
notepad src\main\resources\rules\rules.yaml

# Test immediately without mvn package
java -jar .\target\muleguard-1.0.0-jar-with-dependencies.jar -p .\testData --config src\main\resources\rules\rules.yaml
```

### **Use Different Rule Sets**
```bash
# Production rules
java -jar muleguard.jar -p .\projects --config rules-prod.yaml

# Development rules
java -jar muleguard.jar -p .\projects --config rules-dev.yaml
```

### **Default Behavior (No Changes)**
```bash
# Uses embedded rules.yaml
java -jar muleguard.jar -p .\projects
```

---

## File Modified

**File**: `c:\muleguard-fixed\tmp\muleguard\src\main\java\com\raks\muleguard\MuleGuardMain.java`

**Changes**:
1. Added `configFilePath` variable to store optional config path
2. Updated argument parsing to detect `--config` parameter
3. Modified `loadConfig()` method to accept optional file path
4. Added logic to load external file or fall back to embedded rules.yaml
5. Updated usage message to show new parameter

---

## Build Status

✅ **BUILD SUCCESS** (`mvn clean compile -DskipTests`)

The feature is ready to use!
