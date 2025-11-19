# Java 17 Installation Guide

## Problem
The build script is failing because this project requires **Java 17 or higher**, but your system has **Java 11**.

This project uses modern Java features:
- **Records** (Java 14+)
- **Switch expressions** with arrows (Java 14+)
- **Pattern matching** (Java 16+)

## Solution: Install Java 17

### For Ubuntu/Debian

```bash
# Install Java 17
sudo apt update
sudo apt install openjdk-17-jdk

# Verify installation
java -version
# Should show: openjdk version "17.x.x"

# If you have multiple Java versions, set Java 17 as default
sudo update-alternatives --config java
# Select the Java 17 option

# Verify again
java -version
```

### For macOS

```bash
# Using Homebrew
brew install openjdk@17

# Add to PATH
echo 'export PATH="/usr/local/opt/openjdk@17/bin:$PATH"' >> ~/.zshrc
source ~/.zshrc

# Verify
java -version
```

### For Windows

1. Download Java 17 from [Adoptium (Eclipse Temurin)](https://adoptium.net/)
2. Run the installer
3. Add Java to PATH:
   - System Properties → Environment Variables
   - Edit PATH variable
   - Add: `C:\Program Files\Eclipse Adoptium\jdk-17.x.x-hotspot\bin`
4. Open new terminal and verify: `java -version`

### Alternative: Use SDKMAN (Linux/macOS)

SDKMAN is a tool for managing multiple Java versions:

```bash
# Install SDKMAN
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"

# Install Java 17
sdk install java 17.0.9-tem

# Use Java 17 for this project
sdk use java 17.0.9-tem

# Or set as default
sdk default java 17.0.9-tem

# Verify
java -version
```

## After Installing Java 17

Once Java 17 is installed, you can build the project:

```bash
# Make sure you're in the project directory
cd /path/to/aipoker

# Run the build script
./build.sh

# Or if not executable
bash build.sh
```

## Quick Commands

### Check Current Java Version
```bash
java -version
javac -version
```

### List All Installed Java Versions (Ubuntu)
```bash
update-java-alternatives --list
```

### Switch Between Java Versions (Ubuntu)
```bash
sudo update-alternatives --config java
sudo update-alternatives --config javac
```

## What the Build Script Does

The updated `build.sh` script now:
1. ✅ Checks Java version (must be 17+)
2. ✅ Shows clear error if wrong version
3. ✅ Compiles with `-source 17 -target 17` flags
4. ✅ Creates JAR file in `target/` directory

## Troubleshooting

### "java: invalid source release: 17"
- Your `javac` (compiler) is not Java 17
- Run: `javac -version`
- Make sure both `java` and `javac` point to Java 17

### Multiple Java Versions Installed
```bash
# Check where java points to
which java
ls -l $(which java)

# Check where javac points to
which javac
ls -l $(which javac)

# Set JAVA_HOME explicitly
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export PATH=$JAVA_HOME/bin:$PATH
```

### Still Having Issues?

1. **Uninstall old Java versions** (optional):
   ```bash
   sudo apt remove openjdk-11-jdk
   ```

2. **Use Docker** (alternative):
   ```bash
   # Build in Docker container with Java 17
   docker run --rm -v "$PWD":/app -w /app openjdk:17 bash build.sh
   ```

3. **Use Maven/Gradle instead** (if preferred):
   The project can be built with Maven or Gradle which handle Java versions better.

## Need Help?

If you continue to have issues:
1. Check Java version: `java -version` and `javac -version`
2. Check where Java is installed: `which java`
3. Check if multiple versions exist: `ls /usr/lib/jvm/`
4. Open an issue with the output of the above commands

---

**Note**: Java 17 is an LTS (Long Term Support) version and is recommended for production use. The project uses modern Java features to demonstrate best practices in enterprise software development.
