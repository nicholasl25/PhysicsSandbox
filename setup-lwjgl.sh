#!/bin/bash

# Script to download and set up LWJGL 3 for the Physics Simulations project
# This script downloads LWJGL 3 and sets up the necessary JARs and native libraries

LWJGL_VERSION="3.3.3"
LWJGL_DIR="libs/lwjgl"
BASE_URL="https://github.com/LWJGL/lwjgl3/releases/download/${LWJGL_VERSION}"

echo "Setting up LWJGL ${LWJGL_VERSION}..."

# Detect OS
OS="unknown"
if [[ "$OSTYPE" == "linux-gnu"* ]]; then
    OS="linux"
    ARCH="x64"
elif [[ "$OSTYPE" == "darwin"* ]]; then
    OS="macos"
    ARCH="arm64"  # Change to x64 if on Intel Mac
    # Check if Intel Mac
    if [[ $(uname -m) == "x86_64" ]]; then
        ARCH="x64"
    fi
elif [[ "$OSTYPE" == "msys" || "$OSTYPE" == "win32" ]]; then
    OS="windows"
    ARCH="x64"
fi

if [ "$OS" == "unknown" ]; then
    echo "Error: Unsupported operating system: $OSTYPE"
    exit 1
fi

echo "Detected OS: $OS ($ARCH)"

# Create libs directory
mkdir -p "$LWJGL_DIR"

# Required LWJGL modules
MODULES=(
    "lwjgl"
    "lwjgl-opengl"
    "lwjgl-glfw"
    "lwjgl-stb"
)

# Download JARs and natives
for module in "${MODULES[@]}"; do
    echo "Downloading $module..."
    
    # Download JAR
    JAR_URL="${BASE_URL}/${module}-${LWJGL_VERSION}.jar"
    curl -L -o "${LWJGL_DIR}/${module}-${LWJGL_VERSION}.jar" "$JAR_URL" 2>/dev/null
    
    if [ $? -ne 0 ]; then
        echo "Warning: Failed to download ${module} JAR. You may need to download it manually."
    fi
    
    # Download native library
    NATIVE_URL="${BASE_URL}/${module}-${LWJGL_VERSION}-natives-${OS}-${ARCH}.jar"
    curl -L -o "${LWJGL_DIR}/${module}-${LWJGL_VERSION}-natives-${OS}-${ARCH}.jar" "$NATIVE_URL" 2>/dev/null
    
    if [ $? -ne 0 ]; then
        echo "Warning: Failed to download ${module} natives. You may need to download it manually."
    fi
done

echo ""
echo "LWJGL setup complete!"
echo ""
echo "Note: If automatic download failed, please download LWJGL manually from:"
echo "https://www.lwjgl.org/download"
echo ""
echo "Place the JARs in: $LWJGL_DIR"
echo "The run.sh script will automatically include them in the classpath."

