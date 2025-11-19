#!/bin/bash

# Build script for Texas Hold'em Poker Server
echo "========================================="
echo "Building Texas Hold'em Poker Server"
echo "========================================="

# Check Java version
echo -e "\nChecking Java version..."
JAVA_VERSION=$(java -version 2>&1 | head -1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ -z "$JAVA_VERSION" ]; then
    echo "❌ Java not found. Please install Java 17 or higher."
    exit 1
fi

# Extract major version (handles both 1.8 and 11+ formats)
if [ "$JAVA_VERSION" = "1" ]; then
    JAVA_VERSION=$(java -version 2>&1 | head -1 | cut -d'"' -f2 | cut -d'.' -f2)
fi

echo "Found Java version: $JAVA_VERSION"

if [ "$JAVA_VERSION" -lt 17 ]; then
    echo "❌ Java 17 or higher is required. Found Java $JAVA_VERSION"
    echo "   This project uses Java 17+ features (records, switch expressions)"
    echo ""
    echo "   Please install Java 17 or higher:"
    echo "   • Ubuntu/Debian: sudo apt install openjdk-17-jdk"
    echo "   • macOS: brew install openjdk@17"
    echo "   • Or download from: https://adoptium.net/"
    exit 1
fi

# Clean previous builds
echo -e "\nCleaning previous builds..."
rm -rf bin/
rm -rf target/
mkdir -p bin

# Compile source code
echo -e "\nCompiling source code..."
find src/main/java -name "*.java" > sources.txt

javac -source 17 -target 17 \
      -d bin \
      -sourcepath src/main/java \
      -cp "lib/*" \
      @sources.txt

if [ $? -ne 0 ]; then
    echo "❌ Compilation failed!"
    rm sources.txt
    exit 1
fi

rm sources.txt

# Copy resources
echo -e "\nCopying resources..."
if [ -d "src/main/resources" ]; then
    cp -r src/main/resources/* bin/
fi

# Create manifest
echo -e "\nCreating manifest..."
cat > manifest.txt << EOF
Manifest-Version: 1.0
Main-Class: com.poker.PokerApplication
Class-Path: lib/sqlite-jdbc-3.42.0.0.jar
EOF

# Create JAR
echo -e "\nCreating JAR file..."
mkdir -p target
jar cfm target/poker-server.jar manifest.txt -C bin .

rm manifest.txt

if [ $? -eq 0 ]; then
    echo -e "\n✅ Build successful!"
    echo -e "\nJAR file created: target/poker-server.jar"
    echo -e "\nTo run demo mode:"
    echo "  java -jar target/poker-server.jar --demo"
    echo -e "\nTo start server:"
    echo "  java -jar target/poker-server.jar --server"
else
    echo "❌ JAR creation failed!"
    exit 1
fi
