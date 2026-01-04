#!/bin/bash

# Simple script to compile and run the Physics Simulations project
# Usage: ./run.sh

echo "Compiling Physics Simulations..."

# Create output directory
mkdir -p out

# Build classpath with LWJGL and JOML libraries if they exist
CLASSPATH="out"
if [ -d "libs/lwjgl" ]; then
    echo "Including LWJGL libraries..."
    for jar in libs/lwjgl/*.jar; do
        if [ -f "$jar" ]; then
            CLASSPATH="$CLASSPATH:$jar"
        fi
    done
fi
if [ -d "libs/joml" ]; then
    echo "Including JOML libraries..."
    for jar in libs/joml/*.jar; do
        if [ -f "$jar" ]; then
            CLASSPATH="$CLASSPATH:$jar"
        fi
    done
fi

# Compile Java files (including subdirectories)
if [ -d "libs/lwjgl" ]; then
    javac -d out -sourcepath src -cp "$CLASSPATH" $(find src -name "*.java")
else
    javac -d out -sourcepath src $(find src -name "*.java")
fi

if [ $? -eq 0 ]; then
    echo "Compilation successful!"
    echo "Running application..."
    echo ""
    
    # Set up native library path for LWJGL
    if [ -d "libs/lwjgl" ] || [ -d "libs/joml" ]; then
        # Extract natives if needed (LWJGL natives are in JARs, Java will find them)
        java -cp "$CLASSPATH" -Djava.library.path="libs/lwjgl" simulations.Main
    else
        java -cp out simulations.Main
    fi
else
    echo "Compilation failed. Please check for errors above."
    echo ""
    echo "Note: If you see errors about missing LWJGL classes, run:"
    echo "  ./setup-lwjgl.sh"
    echo "to download LWJGL libraries."
fi
