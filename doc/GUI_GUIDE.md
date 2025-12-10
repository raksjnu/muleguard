# MuleGuard GUI

## Overview
MuleGuard now includes a web-based graphical user interface (GUI) that makes it easy to run validations without using the command line.

## Features
- 🎨 **Beautiful Interface** - Truist purple-themed UI matching the report dashboard
- 🚀 **Easy to Use** - Simple form with pre-populated default path
- 📊 **Instant Results** - View validation results and open reports directly
- 🌐 **Cross-Platform** - Works on Windows, Mac, and Linux
- 🔄 **Non-Breaking** - CLI functionality remains fully intact

## How to Launch

### Windows
```batch
start-gui.bat
```

### Mac/Linux
```bash
./start-gui.sh
```

The GUI will automatically:
1. Start a local web server on port 8080
2. Open your default browser to `http://localhost:8080`
3. Display the MuleGuard validation interface

## Using the GUI

1. **Enter Project Path**
   - The input field is pre-populated with `./testData`
   - You can use:
     - **Absolute paths**: `C:\projects\muleapps`
     - **Relative paths**: `./testData` or `../myproject`

2. **Run Validation**
   - Click the "🚀 Run Validation - MuleGuard" button
   - The validation will run in the background
   - Progress is shown with a loading spinner

3. **View Results**
   - Once complete, you'll see:
     - Total APIs scanned
     - Total rules checked
     - Pass/Fail statistics
   - Click "📊 Open Dashboard Report" to view the full HTML report

## Stopping the Server
Press `Ctrl+C` in the terminal window to stop the GUI server.

## Technical Details
- **Port**: 8080 (default)
- **Technology**: Embedded Java HTTP server
- **No External Dependencies**: Uses only Java standard library
- **Thread-Safe**: Handles concurrent requests

## CLI Still Works!
The traditional command-line interface is still fully functional:
```bash
java -jar target/muleguard-1.0.0-jar-with-dependencies.jar -p ./testData
```

## Troubleshooting

**Port Already in Use**
If port 8080 is already in use, you'll see an error. Stop any other applications using that port.

**Browser Doesn't Open Automatically**
Manually navigate to `http://localhost:8080` in your browser.

**Path Not Found**
Ensure the path you enter exists and contains MuleSoft projects.
