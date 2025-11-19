#!/bin/bash

# Build script for Texas Hold'em Poker Server
echo "========================================="
echo "Building Texas Hold'em Poker Server"
echo "========================================="

# Check Java version
echo -e "\nChecking Java version..."
java -version 2>&1 | grep -q "version"
if [ $? -ne 0 ]; then
    echo "❌ Java not found. Please install Java 17 or higher."
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

javac -d bin \
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
