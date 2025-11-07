#!/bin/bash

# Simple script to compile and run the Physics Simulations project
# Usage: ./run.sh

echo "Compiling Physics Simulations..."

# Create output directory
mkdir -p out

# Compile Java files
javac -d out -sourcepath src src/com/physics/simulations/*.java

if [ $? -eq 0 ]; then
    echo "Compilation successful!"
    echo "Running application..."
    echo ""
    java -cp out com.physics.simulations.Main
else
    echo "Compilation failed. Please check for errors above."
fi
