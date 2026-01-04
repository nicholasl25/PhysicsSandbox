#!/bin/bash

# Script to download and set up JOML (Java OpenGL Math Library) for the Physics Simulations project
# JOML provides Vector3f, Matrix4f, and other math utilities for 3D graphics

JOML_VERSION="1.10.7"
JOML_DIR="libs/joml"
BASE_URL="https://repo1.maven.org/maven2/org/joml/joml/${JOML_VERSION}"

echo "Setting up JOML ${JOML_VERSION}..."

# Create libs directory
mkdir -p "$JOML_DIR"

echo "Downloading JOML JAR..."

# Download JOML JAR
JAR_URL="${BASE_URL}/joml-${JOML_VERSION}.jar"
curl -L -o "${JOML_DIR}/joml-${JOML_VERSION}.jar" "$JAR_URL" 2>/dev/null

if [ $? -eq 0 ]; then
    echo "JOML download successful!"
else
    echo "Warning: Failed to download JOML. You may need to download it manually."
    echo "Download from: https://mvnrepository.com/artifact/org.joml/joml"
    exit 1
fi

echo ""
echo "JOML setup complete!"
echo ""
echo "JOML provides:"
echo "  - Vector3f, Vector4f (3D/4D vectors)"
echo "  - Matrix4f (4x4 matrices for view/projection)"
echo "  - Quaternionf (rotations)"
echo "  - And more math utilities"

