#!/bin/bash

# Simple script to compile and run the Physics Simulations project
# Usage: ./run.sh

echo "Compiling Physics Simulations..."

# Create output directory
mkdir -p out

# Compile Java files (including subdirectories)
javac -d out -sourcepath src $(find src -name "*.java")

if [ $? -eq 0 ]; then
    echo "Compilation successful!"
    echo "Running application..."
    echo ""
    java -cp out simulations.Main
else
    echo "Compilation failed. Please check for errors above."
fi
