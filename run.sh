#!/bin/bash

# Simple script to compile and run the Physics Simulations project
# Usage: ./run.sh

echo "Compiling Physics Simulations..."

# Create output directory
mkdir -p out

# Build classpath with JOGL and JOML libraries if they exist
CLASSPATH="out"
if [ -d "libs/jogl" ]; then
    echo "Including JOGL libraries..."
    for jar in libs/jogl/*.jar; do
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
if [ -d "libs/jogl" ]; then
    javac -d out -sourcepath src -cp "$CLASSPATH" $(find src -name "*.java")
else
    javac -d out -sourcepath src $(find src -name "*.java")
fi

if [ $? -eq 0 ]; then
    echo "Compilation successful!"
    echo "Running application..."
    echo ""
    
    # JOGL natives are in JARs, Java will find them automatically
    # Add JVM arguments for Java 9+ module system compatibility with JOGL
    if [ -d "libs/jogl" ] || [ -d "libs/joml" ]; then
        java --add-opens java.desktop/sun.awt=ALL-UNNAMED \
             --add-opens java.desktop/java.awt=ALL-UNNAMED \
             --add-opens java.desktop/sun.java2d=ALL-UNNAMED \
             -cp "$CLASSPATH" simulations.Main
    else
        java -cp out simulations.Main
    fi
else
    echo "Compilation failed. Please check for errors above."
    echo ""
    echo "Note: If you see errors about missing JOGL classes, run:"
    echo "  ./setup-jogl.sh"
    echo "to download JOGL libraries."
fi
