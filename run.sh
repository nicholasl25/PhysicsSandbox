#!/bin/bash

# Simple script to compile and run the Physics Simulations project
# Usage: ./run.sh

echo "Compiling Physics Simulations..."

# Create output directory
mkdir -p out

# Build classpath with JOML library if it exists
CLASSPATH="out"
if [ -d "libs/joml" ]; then
    echo "Including JOML libraries..."
    for jar in libs/joml/*.jar; do
        if [ -f "$jar" ]; then
            CLASSPATH="$CLASSPATH:$jar"
        fi
    done
fi

# Compile Java files (including subdirectories)
if [ -d "libs/joml" ]; then
    javac -d out -sourcepath src -cp "$CLASSPATH" $(find src -name "*.java")
else
    javac -d out -sourcepath src $(find src -name "*.java")
fi

if [ $? -eq 0 ]; then
    echo "Compilation successful!"
    echo "Running application..."
    echo ""
    
    # Run application (no native dependencies needed!)
    if [ -d "libs/joml" ]; then
        java -cp "$CLASSPATH" simulations.Main
    else
        java -cp out simulations.Main
    fi
else
    echo "Compilation failed. Please check for errors above."
fi
