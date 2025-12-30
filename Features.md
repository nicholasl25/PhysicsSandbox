# Physics Simulations - Features

## Newtonian Gravity

### Gravity 2D

#### Implemented Features

- Multiple planets interact through gravitational forces with support for both regular planets and stationary point masses.
- Interactive planet creation by clicking on the canvas with customizable mass, radius, velocity, position, texture, and temperature.
- Planet textures with images for Sun, Earth, Mars, Venus, Jupiter, and Moon that rotate based on angular velocity.
- Collision handling with planet merging and optional bounce mode with configurable coefficient of restitution.
- Two integration methods: Euler (default) and RK4 (Runge-Kutta 4th order) for improved numerical accuracy.
- Camera controls including zoom in/out with +/- keys and pan by clicking and dragging.
- Simulation controls: pause/resume with spacebar, adjustable gravitational constant and time factor via sliders, and clear simulation button.
- Planet selection and information display showing position, velocity, mass, radius, temperature, and name when clicked.
- Collapsible control panel and stars background image for enhanced user experience.

#### Coming Soon

- Temperature visualization and thermal effects on planet appearance.
- Temperature-based physics interactions.

### Gravity 3D

#### Coming Soon

- Full 3D physics simulation with planets moving in three-dimensional space.
- 3D graphics rendering with perspective projection and camera controls.
- 3D planet textures and rotation visualization.
- 3D collision detection and merging.
- 3D camera controls for viewing the simulation from different angles.
- All features from the 2D simulation adapted for 3D space.

---

## Black Hole

### Schwarzschild

#### Implemented Features

- Light ray trajectories (null geodesics) following curved spacetime around a black hole with automatic removal when crossing the event horizon.
- Adjustable physical constants via sliders: speed of light (c), gravitational constant (G), and black hole mass (M).
- Interactive light ray selection by clicking to view detailed information including polar position, 3-velocity components, and null geodesic norm.
- Trajectory visualization showing complete paths and event horizon visualization as a white circle representing the Schwarzschild radius.
- Simulation controls: pause/resume with spacebar and collapsible control panel.
- Null geodesic constraint enforcement through renormalization to maintain g_μν u^μ u^ν = 0.

#### Coming Soon

- Interactive light ray spawning by clicking on the canvas.
- Multiple black hole configurations.
- Accretion disk visualization.
- Gravitational lensing effects visualization.

### More Coming Soon

---

## General Features

### Implemented Features

- Web-based homepage with Java web server for launching simulations from a browser.
- Base simulation and control panel frameworks providing common structure and collapsible sidebar functionality.
- Vector math library supporting n-dimensional vector operations for 2D and 3D simulations.

