#!/bin/bash

# Script to download JOGL (Java OpenGL) libraries
# JOGL provides OpenGL bindings for Java without requiring GLFW

echo "Setting up JOGL libraries..."

# Create libs directory if it doesn't exist
mkdir -p libs/jogl

# JOGL version
JOGL_VERSION="2.4.0"

# Base URL for JOGL downloads
BASE_URL="https://jogamp.org/deployment/v${JOGL_VERSION}/jar"

# JOGL JARs needed
JARS=(
    "gluegen-rt.jar"
    "jogl-all.jar"
)

# Native libraries (platform-specific)
if [[ "$OSTYPE" == "darwin"* ]]; then
    # macOS
    NATIVES=(
        "gluegen-rt-natives-macosx-universal.jar"
        "jogl-all-natives-macosx-universal.jar"
    )
elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
    # Linux
    NATIVES=(
        "gluegen-rt-natives-linux-amd64.jar"
        "jogl-all-natives-linux-amd64.jar"
    )
else
    echo "Unsupported OS: $OSTYPE"
    exit 1
fi

echo "Downloading JOGL JARs..."
for jar in "${JARS[@]}"; do
    if [ ! -f "libs/jogl/$jar" ]; then
        echo "Downloading $jar..."
        curl -L -o "libs/jogl/$jar" "${BASE_URL}/${jar}"
        if [ $? -ne 0 ]; then
            echo "Failed to download $jar"
            exit 1
        fi
    else
        echo "$jar already exists, skipping..."
    fi
done

echo "Downloading JOGL native libraries..."
for jar in "${NATIVES[@]}"; do
    if [ ! -f "libs/jogl/$jar" ]; then
        echo "Downloading $jar..."
        curl -L -o "libs/jogl/$jar" "${BASE_URL}/${jar}"
        if [ $? -ne 0 ]; then
            echo "Failed to download $jar"
            exit 1
        fi
    else
        echo "$jar already exists, skipping..."
    fi
done

echo ""
echo "JOGL setup complete!"
echo "All JARs are in libs/jogl/"
echo ""
echo "Note: JOGL JARs contain native libraries, so no separate native extraction is needed."

