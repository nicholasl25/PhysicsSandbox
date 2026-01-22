package simulations.NewtonianGravity;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.BasicStroke;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.awt.geom.AffineTransform;
import java.awt.RenderingHints;

/**
 * Planet class - represents a celestial body in the gravity simulation.
 * 
 * This class encapsulates all the properties and behaviors of a planet.
 * Uses Vector for position and velocity to support both 2D and 3D simulations.
 */
public class Planet {
    
    protected int dimension;
    protected double mass;
    protected double radius;
    protected Vector pos;
    protected Vector vel;
    protected Color color;
    protected boolean clicked = false;
    protected double angularVelocity;
    protected double temperature;
    protected String name;
    
    // Texture and rotation fields
    private BufferedImage texture;
    protected double rotationAngle = 0.0;
    protected String texturePath;
    

    /**
     * Constructor with Vector position and velocity (supports 2D and 3D).
     */
    public Planet(int dimension, double mass, double radius, Vector pos, Vector vel,
    double angularVelocity, double temperature, Color color, String texturePath, String name) {
        if (dimension != 2 && dimension != 3) {
            throw new IllegalArgumentException("Dimension must be 2 or 3");
        }
        if (pos.dimensions() != dimension || vel.dimensions() != dimension) {
            throw new IllegalArgumentException("Position and velocity vectors must match dimension: " + dimension);
        }
        
        this.dimension = dimension;
        this.mass = mass;
        this.radius = radius;
        this.pos = pos.clone();
        this.vel = vel.clone();
        this.color = color;
        this.clicked = false;
        this.texturePath = texturePath;
        this.angularVelocity = angularVelocity;
        this.temperature = temperature;
        this.name = name;
        
        // Load texture if provided
        if (texturePath != null && !texturePath.isEmpty()) {
            loadTexture(texturePath);
        }
    }
    
    /**
     * Constructor for 2D compatibility (creates 2D vectors from x, y, vx, vy).
     */
    public Planet(double mass, double radius, double x, double y, double vx, double vy,
    double angularVelocity, double temperature, Color color, String texturePath, String name) {
        this(2, mass, radius, new Vector(new double[]{x, y}), new Vector(new double[]{vx, vy}), 
             angularVelocity, temperature, color, texturePath, name);
    }
    
    /**
     * Loads and scales a texture image from file
     */
    private void loadTexture(String path) {
        try {
            BufferedImage original = ImageIO.read(new File(path));
            
            // Pre-scale to planet size for better performance
            int texSize = (int)(radius * 2.5);
            texture = new BufferedImage(texSize, texSize, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = texture.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(original, 0, 0, texSize, texSize, null);
            g.dispose();
            
            System.out.println("Loaded texture: " + path);
        } catch (IOException e) {
            System.err.println("Failed to load texture: " + path + " - Using solid color");
            texture = null;
        }
    }
    
    /**
     * Updates the planet's position based on its velocity.
     * Called during each simulation step.
     * 
     * @param deltaTime Time elapsed since last update
     */
    public void updatePosition(double deltaTime, double timeFactor) {
        Vector displacement = vel.multiply(deltaTime * timeFactor);
        pos = pos.add(displacement);
        
        // Update rotation angle
        this.rotationAngle += angularVelocity * timeFactor * deltaTime;
        this.rotationAngle %= (Math.PI * 2);  // reduce it into [ -2π, 2π )

        // make sure it's positive if you want it in [0, 2π)
        if (this.rotationAngle < 0) {
            this.rotationAngle += Math.PI * 2;
        }
    }
    
    /**
     * Updates the planet's velocity based on acceleration vector.
     * 
     * @param accel Acceleration vector
     * @param deltaTime Time elapsed since last update
     */
    public void updateVelocity(Vector accel, double deltaTime) {
        Vector deltaVel = accel.multiply(deltaTime);
        vel = vel.add(deltaVel);
    }
    
    /**
     * Calculates the distance to another planet.
     * Useful for gravitational force calculations.
     * 
     * @param other The other planet
     * @return Distance between the two planets
     */

    public double distanceTo(Planet other) {
        Vector delta = this.pos.subtract(other.pos);
        return delta.magnitude();
    }
    
    /**
     * Calculates gravitational force exerted on this planet by another planet.
     * Formula: F = G * m1 * m2 / r²
     * 
     * @param other The other planet exerting force
     * @return Array [fx, fy] representing force components
     */
    /**
     * Calculates gravitational force exerted on this planet by another planet.
     * Returns a Vector representing the force.
     */
    public Vector gravitationalForceFrom(Planet other, double gravitationalConstant) {
        double distance = distanceTo(other);
        if (distance == 0.0) {
            return new Vector(new double[pos.dimensions()]); // Zero vector
        }
        
        Vector direction = other.pos.subtract(this.pos).normalize();
        double forceMagnitude = gravitationalConstant * this.mass * other.mass / (distance * distance);
        
        return direction.multiply(forceMagnitude);
    }
    
    /**
     * Calculates gravitational force (2D compatibility - returns array).
     */
    public double[] gravitationalForceFromArray(Planet other, double gravitationalConstant) {
        Vector force = gravitationalForceFrom(other, gravitationalConstant);
        return force.getData();
    }
    
    
    /**
     * Draws the planet on the screen with texture rotation or solid color.
     * 
     * @param g2d Graphics2D object for drawing
     */
    public void draw(Graphics2D g2d) {
        // For 2D drawing, use first two components
        double x = pos.get(0);
        double y = pos.dimensions() > 1 ? pos.get(1) : 0.0;
        int drawX = (int)(x - radius);
        int drawY = (int)(y - radius);
        int size = (int)(radius * 2);
        
        if (texture != null) {
            // Save original transform
            AffineTransform oldTransform = g2d.getTransform();
            
            // Create clipping circle for the planet
            g2d.setClip(new java.awt.geom.Ellipse2D.Double(drawX, drawY, size, size));
            
            // Translate to planet center
            g2d.translate(x, y);
            
            // Apply rotation
            g2d.rotate(rotationAngle);
            
            // Draw texture centered and scaled
            int texSize = texture.getWidth();
            g2d.drawImage(texture, 
                         -texSize/2, -texSize/2,
                         texSize, texSize,
                         null);
            
            // Restore transform and clip
            g2d.setTransform(oldTransform);
            g2d.setClip(null);
            
        } else {
            // Fallback: draw solid color if no texture
            g2d.setColor(color);
            g2d.fillOval(drawX, drawY, size, size);
        }

        if (clicked) {
            g2d.setStroke(new BasicStroke(3.0f));
            g2d.setColor(Color.YELLOW);
            g2d.drawOval(drawX-5, drawY-5, size+10, size+10);
        }
    }
    
    /**
     * Adds 3D shading effect over the texture for realism
     */

    public void clicked() {
        this.clicked = !this.clicked;
    }

    /**
     * Checks if this planet collides with another planet.
     * Simple collision detection: if distance < sum of radii
     * 
     * @param other The other planet
     * @return true if colliding
     */
    public boolean collidesWith(Planet other) {
        return distanceTo(other) < (this.radius + other.radius);
    }
    
    public Planet merge(Planet other) {
        double combinedMass = this.mass + other.mass;
        
        // Weighted average of velocities
        Vector newVel = this.vel.multiply(this.mass).add(other.vel.multiply(other.mass));
        newVel = newVel.multiply(1.0 / combinedMass);
        
        // Weighted average of positions
        Vector newPos = this.pos.multiply(this.mass).add(other.pos.multiply(other.mass));
        newPos = newPos.multiply(1.0 / combinedMass);

        double newTemperature = (this.temperature * this.mass + other.temperature * other.mass) / combinedMass;

        double newRadius = this.radius > other.radius ? this.radius : other.radius;

        Color c1 = this.color;
        Color c2 = other.color;
        int r = (c1.getRed() + c2.getRed()) / 2;
        int g = (c1.getGreen() + c2.getGreen()) / 2;
        int b = (c1.getBlue() + c2.getBlue()) / 2;

        Color newColor = new Color(r, g, b);

        double momentOfInertiaCoeff = 0.4;

        double angularMomentum = momentOfInertiaCoeff * (this.radius * this.radius * this.mass * this.angularVelocity 
        + other.radius * other.radius * other.mass * other.angularVelocity);
        double newAngularVelocity = angularMomentum / (momentOfInertiaCoeff * newRadius * newRadius * combinedMass);

        String newTexturePath = null;
        String newName = null;
        if (this.radius > other.radius) {
            if (this.texturePath != null) {
                newTexturePath = this.texturePath;
            }
            else {
                newTexturePath = other.texturePath;
            }
            newName = this.name;
        } else {
            if (other.texturePath != null) {
                newTexturePath = other.texturePath;
            }
            else {
                newTexturePath = this.texturePath;
            }
            newName = other.name;
        }
        
        return new Planet(this.dimension, combinedMass, newRadius, newPos, newVel, 
        newAngularVelocity, newTemperature, newColor, newTexturePath, newName);
    }

    public void bouncePlanet(double coefficientOfRestitution, Planet other) {
        Vector deltaPos = other.pos.subtract(this.pos);
        double deltaPosMag = deltaPos.magnitude();
        if (deltaPosMag == 0.0) return; // Same position, skip
        
        Vector n = deltaPos.multiply(1.0 / deltaPosMag); // Normalized direction

        // Project velocities onto the collision normal
        double u1 = this.vel.dot(n);
        double u2 = other.vel.dot(n);

        double relVel = u1 - u2;
        if (relVel <= 0) return; // they are separating, no bounce

        double m1 = this.mass;
        double m2 = other.getMass();
        double e = coefficientOfRestitution;

        double u1p = ( (m1 - e*m2)*u1 + (1 + e)*m2*u2 ) / (m1 + m2);
        double u2p = ( (m2 - e*m1)*u2 + (1 + e)*m1*u1 ) / (m1 + m2);

        double deltaU1 = u1p - u1;
        double deltaU2 = u2p - u2;

        // Update velocities along collision normal
        Vector deltaVel1 = n.multiply(deltaU1);
        Vector deltaVel2 = n.multiply(deltaU2);
        
        this.vel = this.vel.add(deltaVel1);
        other.vel = other.vel.add(deltaVel2);
    }

    public double getPeriodOfRotation() {
        if (angularVelocity != 0.0) {
            return 2 * Math.PI / angularVelocity;}
        else {
            return 0.0;
        }
    }

    public void bouncePointMass(double coefficientOfRestitution) {
        this.vel = this.vel.multiply(-coefficientOfRestitution);
    }

    public Vector getVelocity() {
        return vel.clone();
    }
    
    public double[] getVelocityArray() {
        return vel.getData();
    }

    public Vector getPosition() {
        return pos.clone();
    }
    
    public double[] getPositionArray() {
        return pos.getData();
    }

    public double getMass() {
        return this.mass;
    }

    public double getRadius() {
        return this.radius;
    }


    public Color getColor() {
        return this.color;
    }

    public boolean isClicked() {
        return this.clicked;
    }

    public double getAngularVelocity() {
        return this.angularVelocity;
    }

    public double getTemperature() {
        return this.temperature;
    }

    public String getName() {
        return this.name;
    }

    public void setVelocity(Vector newVel) {
        this.vel = newVel.clone();
    }
    
    public void setPosition(Vector newPos) {
        this.pos = newPos.clone();
    }

    public boolean containsPoint(double x, double y) {
        double dx = x - pos.get(0);
        double dy = y - (pos.dimensions() > 1 ? pos.get(1) : 0.0);
        return Math.sqrt(dx*dx + dy*dy) <= this.radius;
    }
    
    /**
     * Gets the texture image.
     */
    public java.awt.image.BufferedImage getTexture() {
        return texture;
    }
    
    /**
     * Gets the rotation angle.
     */
    public double getRotationAngle() {
        return rotationAngle;
    }
    
    @Override
    public String toString() {
        return String.format("Planet with mass = %.2f pos=%s vel=%s", mass, pos, vel);
    }


}

