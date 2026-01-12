#!/bin/bash

echo "Compiling and running Gravity3D tests..."

mkdir -p out

CLASSPATH="out"
if [ -d "libs/joml" ]; then
    for jar in libs/joml/*.jar; do
        if [ -f "$jar" ]; then
            CLASSPATH="$CLASSPATH:$jar"
        fi
    done
fi

javac -d out -sourcepath src:test -cp "$CLASSPATH" test/simulations/NewtonianGravity/Gravity3D/Gravity3DTest.java

if [ $? -eq 0 ]; then
    echo "Compilation successful!"
    echo "Running tests..."
    echo ""
    java -cp "$CLASSPATH" simulations.NewtonianGravity.Gravity3D.Gravity3DTest
else
    echo "Compilation failed."
fi

