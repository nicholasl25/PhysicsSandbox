# Web App Conversion Guide

This document outlines the changes needed to convert the Physics Sandbox from a desktop Java application to a web application with:
- **Java Backend**: Handles physics calculations and simulation state
- **Web Frontend**: Handles 3D rendering using WebGL (Three.js)

## Architecture Overview

```
┌─────────────────┐         HTTP/WebSocket         ┌──────────────────┐
│  Web Browser    │ ◄─────────────────────────────► │  Java Backend    │
│  (Frontend)     │                                 │  (Physics Engine)│
│                 │                                 │                  │
│  - Three.js     │                                 │  - Planet Logic  │
│  - WebGL        │                                 │  - Force Calc    │
│  - UI Controls  │                                 │  - State Mgmt    │
└─────────────────┘                                 └──────────────────┘
```

## Required Changes

### 1. Backend Changes (Java)

#### A. Create REST API Endpoints

**New File: `src/simulations/api/SimulationAPI.java`**
- `GET /api/simulations/{id}/state` - Get current simulation state (planets, settings)
- `POST /api/simulations/{id}/planets` - Add a new planet
- `DELETE /api/simulations/{id}/planets/{planetId}` - Remove a planet
- `PUT /api/simulations/{id}/settings` - Update simulation settings (gravity constant, etc.)
- `POST /api/simulations/{id}/pause` - Pause/resume simulation
- `POST /api/simulations/{id}/reset` - Reset simulation

#### B. Create WebSocket Server

**New File: `src/simulations/api/SimulationWebSocket.java`**
- Broadcast simulation state updates every frame (60 FPS)
- Send planet positions, velocities, rotations
- Handle client connections/disconnections

#### C. Extract Physics Engine

**New File: `src/simulations/physics/PhysicsEngine.java`**
- Move all physics calculation logic from `Gravity3DSimulation` here
- Run physics loop in a separate thread (independent of rendering)
- Maintain simulation state (planets list, settings)
- Calculate forces, update positions, handle collisions

**Changes to existing files:**
- `Gravity3DSimulation.java`: Remove all OpenGL/JOGL code, keep only physics logic
- `Planet.java`: Keep as-is (already pure physics)
- `PointMass.java`: Keep as-is

#### D. Update WebServer.java

**Modify `src/simulations/WebServer.java`:**
- Add REST API endpoints
- Add WebSocket support (use Java-WebSocket library or Jetty WebSocket)
- Serve static files (HTML, JS, CSS) from `resources/web/`
- Remove Swing-based launch handlers

### 2. Frontend Changes (HTML/JavaScript)

#### A. Create 3D WebGL Renderer

**New File: `resources/web/gravity3d.html`**
```html
<!DOCTYPE html>
<html>
<head>
    <title>3D Gravity Simulation</title>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/three.js/r128/three.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/three@0.128.0/examples/js/controls/OrbitControls.js"></script>
</head>
<body>
    <canvas id="canvas"></canvas>
    <div id="controls">...</div>
    <script src="gravity3d.js"></script>
</body>
</html>
```

**New File: `resources/web/gravity3d.js`**
- Initialize Three.js scene (Scene, Camera, Renderer)
- Create WebSocket connection to backend
- Receive planet data from WebSocket
- Render spheres for each planet using Three.js
- Handle camera controls (OrbitControls)
- Update planet positions/rotations each frame
- Handle user interactions (add planet, adjust settings)

#### B. Create API Client

**New File: `resources/web/api-client.js`**
- Functions to call REST API endpoints
- WebSocket connection management
- State synchronization

#### C. Create UI Controls

**Modify `resources/web/index.html`:**
- Update launch buttons to open web pages instead of Java windows
- Add links to `gravity3d.html`, `gravity2d.html`

**New File: `resources/web/controls.js`**
- Form for adding planets (mass, radius, position, velocity, color)
- Settings panel (gravity constant, time factor, etc.)
- Pause/resume button
- Planet selection and property display

### 3. Data Format

#### Planet JSON Format
```json
{
  "id": "planet-1",
  "mass": 1000.0,
  "radius": 10.0,
  "position": {"x": 0.0, "y": 0.0, "z": 0.0},
  "velocity": {"x": 0.0, "y": 0.0, "z": 0.0},
  "color": {"r": 255, "g": 0, "b": 0},
  "texture": "earth",
  "rotationAngle": 0.5,
  "name": "Planet 1"
}
```

#### Simulation State JSON
```json
{
  "id": "sim-1",
  "planets": [...],
  "settings": {
    "gravitationalConstant": 6000.0,
    "timeFactor": 1.0,
    "bounce": false,
    "coefficientOfRestitution": 1.0
  },
  "paused": false
}
```

### 4. Dependencies to Add

#### Backend
- **Java-WebSocket** (for WebSocket support): Add to `libs/`
  ```xml
  <dependency>
    <groupId>org.java-websocket</groupId>
    <artifactId>Java-WebSocket</artifactId>
    <version>1.5.3</version>
  </dependency>
  ```
- **Jackson** (for JSON serialization): Already likely available, but ensure it's in classpath
  ```xml
  <dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>2.15.2</version>
  </dependency>
  ```

#### Frontend
- **Three.js** (via CDN): `https://cdnjs.cloudflare.com/ajax/libs/three.js/r128/three.min.js`
- **OrbitControls** (for camera): Included with Three.js examples

### 5. File Structure After Conversion

```
Physics-/
├── src/simulations/
│   ├── Main.java                    # Start web server only
│   ├── WebServer.java               # HTTP server + REST API
│   ├── api/
│   │   ├── SimulationAPI.java      # REST endpoints
│   │   └── SimulationWebSocket.java # WebSocket server
│   ├── physics/
│   │   └── PhysicsEngine.java       # Physics calculations
│   └── NewtonianGravity/
│       ├── Planet.java              # (unchanged)
│       ├── PointMass.java           # (unchanged)
│       └── Vector.java              # (unchanged)
├── resources/
│   ├── web/
│   │   ├── index.html               # Homepage
│   │   ├── gravity3d.html          # 3D simulation page
│   │   ├── gravity3d.js            # 3D rendering logic
│   │   ├── api-client.js           # API communication
│   │   └── controls.js              # UI controls
│   └── textures/                    # (unchanged)
└── libs/
    ├── java-websocket.jar           # (new)
    └── jackson-*.jar                # (new, if needed)
```

### 6. Implementation Steps

1. **Phase 1: Extract Physics Engine**
   - Create `PhysicsEngine.java` with physics loop
   - Move planet management and force calculations
   - Test physics independently

2. **Phase 2: Create REST API**
   - Add endpoints to `WebServer.java`
   - Serialize Planet objects to JSON
   - Test with curl/Postman

3. **Phase 3: Add WebSocket**
   - Implement WebSocket server
   - Broadcast simulation state at 60 FPS
   - Test connection from browser console

4. **Phase 4: Frontend 3D Rendering**
   - Create `gravity3d.html` with Three.js
   - Connect to WebSocket
   - Render planets as spheres
   - Add camera controls

5. **Phase 5: UI Controls**
   - Add planet creation form
   - Add settings panel
   - Add pause/resume controls

6. **Phase 6: Polish**
   - Add planet textures (load images in Three.js)
   - Add lighting and shadows
   - Improve UI/UX

### 7. Benefits of This Architecture

✅ **No Native Dependencies**: No JOGL, no GLFW, no threading issues
✅ **Cross-Platform**: Works on any device with a web browser
✅ **Easy Deployment**: Single Java server, no client installation
✅ **Modern UI**: Can use any web framework (React, Vue, etc.)
✅ **Scalable**: Multiple clients can view the same simulation
✅ **Easy Updates**: Update frontend without recompiling Java

### 8. Example Code Snippets

#### Backend: PhysicsEngine.java (simplified)
```java
public class PhysicsEngine {
    private List<Planet> planets = new ArrayList<>();
    private double gravitationalConstant = 6000.0;
    private boolean running = false;
    private Thread physicsThread;
    
    public void start() {
        running = true;
        physicsThread = new Thread(() -> {
            while (running) {
                update(DELTA_TIME);
                try { Thread.sleep(16); } catch (InterruptedException e) {}
            }
        });
        physicsThread.start();
    }
    
    private void update(double deltaTime) {
        // Calculate forces
        for (Planet p : planets) {
            Vector totalForce = new Vector(new double[3]);
            for (Planet other : planets) {
                if (p != other) {
                    totalForce = totalForce.add(
                        p.gravitationalForceFrom(other, gravitationalConstant)
                    );
                }
            }
            Vector accel = totalForce.multiply(1.0 / p.getMass());
            p.updateVelocity(accel, deltaTime);
        }
        
        // Update positions
        for (Planet p : planets) {
            p.updatePosition(deltaTime, 1.0);
        }
    }
    
    public List<Planet> getPlanets() { return planets; }
    public void addPlanet(Planet p) { planets.add(p); }
}
```

#### Frontend: gravity3d.js (simplified)
```javascript
// Initialize Three.js
const scene = new THREE.Scene();
const camera = new THREE.PerspectiveCamera(75, window.innerWidth/window.innerHeight, 0.1, 1000);
const renderer = new THREE.WebGLRenderer({ canvas: document.getElementById('canvas') });
const controls = new THREE.OrbitControls(camera, renderer.domElement);

// WebSocket connection
const ws = new WebSocket('ws://localhost:8080/api/simulations/1/stream');
const planetMeshes = new Map();

ws.onmessage = (event) => {
    const state = JSON.parse(event.data);
    
    // Update or create planet meshes
    state.planets.forEach(planet => {
        let mesh = planetMeshes.get(planet.id);
        if (!mesh) {
            const geometry = new THREE.SphereGeometry(planet.radius, 32, 32);
            const material = new THREE.MeshBasicMaterial({ 
                color: new THREE.Color(planet.color.r/255, planet.color.g/255, planet.color.b/255)
            });
            mesh = new THREE.Mesh(geometry, material);
            scene.add(mesh);
            planetMeshes.set(planet.id, mesh);
        }
        
        // Update position
        mesh.position.set(planet.position.x, planet.position.y, planet.position.z);
        mesh.rotation.y = planet.rotationAngle;
    });
};

// Animation loop
function animate() {
    requestAnimationFrame(animate);
    controls.update();
    renderer.render(scene, camera);
}
animate();
```

## Summary

The main changes are:
1. **Remove all OpenGL/JOGL code** from Java
2. **Extract physics into a service** that runs independently
3. **Add REST API + WebSocket** for communication
4. **Create web frontend** with Three.js for 3D rendering
5. **Move UI controls** to HTML/JavaScript

This eliminates all the native library and threading issues you've been experiencing while making the application more accessible and easier to maintain.
