# PhysicsSandbox

A physics simulation platform featuring both web-based and desktop applications for exploring gravitational interactions and black hole physics.

## Overview

PhysicsSandbox includes:
- **Web Applications**: Browser-based 3D gravity simulation (JavaScript/Three.js)
- **Desktop Applications**: Java-based 2D gravity and black hole simulations (Java Swing)

## Features

### 3D Gravity Simulation (Web)
- **Realistic Physics**: N-body gravitational interactions in 3D space
- **3D Rendering**: Textured rotating planets with NASA imagery using WebGL
- **Interactive Controls**: 
  - Mouse drag to orbit camera around selected planet
  - Mouse wheel to zoom in/out
  - Click planets to select and follow
  - Spacebar to pause/resume
- **Real-time Physics**: Collision detection with merging or bouncing
- **Adjustable Parameters**: Gravity constant, time factor, bounce mode
- **No Installation Required**: Runs entirely in your browser

### 2D Gravity Simulation (Desktop)
- **Realistic Physics**: N-body gravitational simulation with collision detection
- **Textured Rotating Planets**: Add planets with real NASA textures that rotate as they move
- **Interactive Controls**: 
  - Click to set spawn position (red X marker)
  - Drag to pan the view
  - Spacebar to pause/resume
  - Select planets to view their properties
- **Stationary Masses**: Add immovable massive objects for interesting orbital dynamics
- **Adjustable Gravity**: Real-time gravity constant slider (0-10000)

### Black Hole Simulator (Desktop)
- **Light Ray Geodesics**: Trajectories following curved spacetime in Schwarzschild metric
- **Tensor Algebra Engine**: Einstein summation notation for general relativity calculations
- **Visualization**: Gravitational lensing effects and event horizon visualization
- **Interactive Controls**: Adjustable physical constants (c, G, M)

## How to Run

### Web Application
1. Start the web server:
   ```bash
   ./run.sh
   ```
2. Open your browser to `http://localhost:8080`
3. Click "Launch 3D Simulation" under Web Applications

### Desktop Applications
1. Start the web server:
   ```bash
   ./run.sh
   ```
2. Open your browser to `http://localhost:8080`
3. Click the desired simulation under Desktop Applications
4. Java Swing windows will launch (requires Java runtime)

### Deploying Web Application
The web application can be deployed to any static hosting service:
- **GitHub Pages**: Copy `resources/web/` contents to `docs/` or `gh-pages` branch
- **Netlify/Vercel**: Point to `resources/web/` directory
- **Any Web Server**: Serve the `resources/web/` directory

The web application is self-contained and requires no backend server when deployed statically.

## Technologies

### Web Application
- **JavaScript/ES6**: Modern JavaScript for physics engine
- **Three.js**: 3D graphics rendering with WebGL
- **HTML5/CSS3**: Modern web interface

### Desktop Applications
- **Java Swing**: Desktop GUI framework
- **Custom Physics Engine**: N-body simulation and tensor algebra
- **Graphics2D**: 2D rendering with texture support

## Project Structure

```
Physics-/
├── src/                              # Java source code (Desktop apps)
│   └── simulations/
│       ├── Main.java                 # Entry point - starts web server
│       ├── WebServer.java            # HTTP server for launch page
│       ├── BaseSimulation.java       # Base class for simulations
│       ├── NewtonianGravity/
│       │   ├── Gravity2D/            # 2D gravity simulation (Java)
│       │   ├── Planet.java          # Planet physics class
│       │   └── Vector.java           # Vector math library
│       └── Schwarzchild/            # Black hole simulation (Java)
│
├── resources/
│   ├── web/                          # Web application files
│   │   ├── index.html                # Launch page
│   │   ├── app.js                    # Launch page logic
│   │   ├── style.css                 # Launch page styling
│   │   └── gravity3d/                # 3D Gravity web application
│   │       ├── gravity3d.html        # Main HTML file
│   │       ├── gravity3d.js          # Main simulation engine
│   │       ├── gravity3d.css        # Application styling
│   │       ├── gravity3d-ui.js       # UI controller
│   │       └── physics/              # Physics engine (JavaScript)
│   │           ├── Vector.js
│   │           └── Planet.js
│   └── textures/                     # Shared planet textures
│
├── out/                              # Compiled Java classes
├── libs/                             # Java dependencies (JOML - unused)
└── run.sh                            # Build and run script
```

## Controls

### 3D Gravity (Web)
- **Mouse Drag**: Rotate camera around selected planet
- **Mouse Wheel**: Zoom in/out
- **Click Planet**: Select and follow planet
- **Spacebar**: Pause/Resume simulation

### 2D Gravity (Desktop)
- **Click**: Set position for next object (shows red X)
- **Drag**: Pan the simulation view
- **Spacebar**: Pause/Resume simulation
- **Click on Planet**: Select and view planet properties

## Planet Textures

Planets can use realistic NASA textures that rotate as they move through space. Textures are located in `resources/textures/` and include Earth, Mars, Jupiter, Moon, Sun, and Venus.

For more information about textures, see `resources/textures/README.md`.

## Development

### Building
```bash
./run.sh
```

This compiles Java files and starts the web server on port 8080.

### Web Application Development
The web application files are in `resources/web/`. To develop:
1. Make changes to HTML/CSS/JS files
2. Refresh browser (if server is running)
3. For static deployment, test by serving `resources/web/` directory

### Java Application Development
Java source files are in `src/simulations/`. Compile with:
```bash
javac -d out -cp "out:libs/joml/*.jar" src/simulations/**/*.java
```

## License

[Add your license here]
