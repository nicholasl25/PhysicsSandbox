package com.physics.simulations.NewtonianGravity;

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
 * Think about what a planet needs:
 * - Position (x, y coordinates)
 * - Velocity (vx, vy components)
 * - Mass (affects gravitational pull)
 * - Size/radius (for drawing)
 * - Color (visual representation)
 * - Angular velocity (for rotation)
 * - Texture (for drawing)
 * - Name (for identification)
 * - Temperature (to update texture WIP)
 */


public class Planet {
    
    double mass;
    double radius;
    double x, y;
    double vx, vy;
    Color color;
    boolean clicked = false;
    double angularVelocity;
    double temperature;
    String name;
    
    // Texture and rotation fields
    private BufferedImage texture;
    protected double rotationAngle = 0.0;
    protected String texturePath;
    
    // RK4 integration fields (optional, only used when RK4 methods are called)
    private double k1vx, k1vy, k1x, k1y;
    private double k2vx, k2vy, k2x, k2y;
    private double k3vx, k3vy, k3x, k3y;
    private double k4vx, k4vy, k4x, k4y;
    private double lastAx = 0.0;
    private double lastAy = 0.0;

    // Constructor with texture
    public Planet(double mass, double radius, double x, double y, double vx, double vy,
    double angularVelocity, double temperature, Color color, String texturePath, String name) {
        this.mass = mass;
        this.radius = radius;
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
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
        this.x += this.vx * deltaTime * timeFactor;
        this.y += this.vy * deltaTime * timeFactor;
        
        // Update rotation angle
        this.rotationAngle += angularVelocity * timeFactor * deltaTime;
        this.rotationAngle %= (Math.PI * 2);  // reduce it into [ -2π, 2π )

        // make sure it's positive if you want it in [0, 2π)
        if (this.rotationAngle < 0) {
            this.rotationAngle += Math.PI * 2;
        }
    }
    
    /**
     * Updates the planet's velocity based on acceleration.
     * 
     * @param ax Horizontal acceleration component
     * @param ay Vertical acceleration component
     * @param deltaTime Time elapsed since last update
     */
    public void updateVelocity(double ax, double ay, double deltaTime) {
        vx += ax * deltaTime;
        vy += ay * deltaTime;
    }
    
    /**
     * Updates velocity using RK4 integration.
     * Uses the provided acceleration and estimates intermediate accelerations
     * by assuming velocity changes linearly.
     * 
     * @param ax Acceleration in x direction
     * @param ay Acceleration in y direction
     * @param deltaTime Time step
     */
    public void updateVelocityRK4(double ax, double ay, double deltaTime) {
        // Store acceleration for position RK4
        lastAx = ax;
        lastAy = ay;
        
        // k1: acceleration at current state
        k1vx = ax * deltaTime;
        k1vy = ay * deltaTime;
        
        // k2: acceleration at midpoint (estimate using k1)
        // Assume acceleration changes linearly: a(t+dt/2) ≈ a(t)
        k2vx = ax * deltaTime;
        k2vy = ay * deltaTime;
        
        // k3: acceleration at midpoint using k2 (same estimate)
        k3vx = ax * deltaTime;
        k3vy = ay * deltaTime;
        
        // k4: acceleration at end using k3
        k4vx = ax * deltaTime;
        k4vy = ay * deltaTime;
        
        // Weighted average: v += (k1 + 2*k2 + 2*k3 + k4) / 6
        // Since all k values are the same, this simplifies to Euler, but we keep
        // the structure for when intermediate accelerations can be computed
        vx += (k1vx + 2.0 * k2vx + 2.0 * k3vx + k4vx) / 6.0;
        vy += (k1vy + 2.0 * k2vy + 2.0 * k3vy + k4vy) / 6.0;
    }
    
    /**
     * Updates position using RK4 integration.
     * Uses intermediate velocities computed from the current velocity and acceleration.
     * 
     * @param deltaTime Time step
     * @param timeFactor Time scaling factor
     */
    public void updatePositionRK4(double deltaTime, double timeFactor) {
        double dt = deltaTime * timeFactor;
        
        // k1: velocity at current state
        k1x = vx * dt;
        k1y = vy * dt;
        
        // k2: velocity at midpoint using k1
        // v(t + dt/2) ≈ v(t) + a(t) * dt/2
        double vxMid1 = vx + lastAx * dt * 0.5;
        double vyMid1 = vy + lastAy * dt * 0.5;
        k2x = vxMid1 * dt;
        k2y = vyMid1 * dt;
        
        // k3: velocity at midpoint using k2
        double vxMid2 = vx + lastAx * dt * 0.5;
        double vyMid2 = vy + lastAy * dt * 0.5;
        k3x = vxMid2 * dt;
        k3y = vyMid2 * dt;
        
        // k4: velocity at end using k3
        // v(t + dt) ≈ v(t) + a(t) * dt
        double vxEnd = vx + lastAx * dt;
        double vyEnd = vy + lastAy * dt;
        k4x = vxEnd * dt;
        k4y = vyEnd * dt;
        
        // Weighted average: x += (k1 + 2*k2 + 2*k3 + k4) / 6
        x += (k1x + 2.0 * k2x + 2.0 * k3x + k4x) / 6.0;
        y += (k1y + 2.0 * k2y + 2.0 * k3y + k4y) / 6.0;
        
        // Update rotation angle (same as parent)
        rotationAngle += angularVelocity * timeFactor;
        rotationAngle %= (Math.PI * 2);
        if (rotationAngle < 0) {
            rotationAngle += Math.PI * 2;
        }
    }
    
    /**
     * Calculates the distance to another planet.
     * Useful for gravitational force calculations.
     * 
     * @param other The other planet
     * @return Distance between the two planets
     */

    public double distanceTo(Planet other) {
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        return Math.sqrt(dx*dx + dy*dy);
    }
    
    /**
     * Calculates gravitational force exerted on this planet by another planet.
     * Formula: F = G * m1 * m2 / r²
     * 
     * @param other The other planet exerting force
     * @return Array [fx, fy] representing force components
     */
    public double[] gravitationalForceFrom(Planet other, double gravitationalConstant) {
        double distance = distanceTo(other);
        double directionX = (other.x - this.x) / distance;
        double directionY = (other.y - this.y) / distance;

        double forceMagnitude = gravitationalConstant * this.mass * other.mass / (distance * distance);

        double forceX = directionX * forceMagnitude;
        double forceY = directionY * forceMagnitude;


        double[] forceVect = {forceX, forceY};
        return forceVect; 
    }
    
    
    /**
     * Draws the planet on the screen with texture rotation or solid color.
     * 
     * @param g2d Graphics2D object for drawing
     */
    public void draw(Graphics2D g2d) {
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
        double newVx = (this.vx * this.mass + other.vx * other.mass) / combinedMass;
        double newVy = (this.vy * this.mass + other.vy * other.mass) / combinedMass;
        
        double newX = (this.x * this.mass + other.x * other.mass) / combinedMass;
        double newY = (this.y * this.mass + other.y * other.mass) / combinedMass;

        double newTemperature = (this.temperature * this.mass + other.temperature * other.mass) / combinedMass;

        double newRadius = this.radius > other.radius ? this.radius : other.radius;

        Color c1 = this.color;
        Color c2 = other.color;
        int r = (c1.getRed() + c2.getRed()) / 2;
        int g = (c1.getGreen() + c2.getGreen()) / 2;
        int b = (c1.getBlue() + c2.getBlue()) / 2;

        Color newColor = new Color(r, g, b);

        double angularMomentum = 0.4 * (this.radius * this.radius * this.mass * this.angularVelocity 
        + other.radius * other.radius * other.mass * other.angularVelocity);
        double newAngularVelocity = 2.5 * angularMomentum / (newRadius * newRadius * combinedMass);

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
        
        return new Planet(combinedMass, newRadius, newX, newY, newVx, newVy, 
        newAngularVelocity, newTemperature, newColor, newTexturePath, newName);
    }

    public void bouncePlanet(double coefficientOfRestitution, Planet other) {
        double[] otherPos = other.getPosition();
        double otherMass = other.getMass();
        double otherX = otherPos[0];
        double otherY = otherPos[1];
        double[] deltaPos = {otherX - this.x, otherY - this.y};
        double deltaPosMag = Math.sqrt((deltaPos[0] * deltaPos[0]) + (deltaPos[1] * deltaPos[1]));

        double[] n = {deltaPos[0] / deltaPosMag, deltaPos[1] / deltaPosMag};

        double[] v1 = { this.vx, this.vy };
        double[] v2 = other.getVelocity();

        // Project velocities onto the collision normal
        double u1 = v1[0]*n[0] + v1[1]*n[1];
        double u2 = v2[0]*n[0] + v2[1]*n[1];

        double relVel = u1 - u2;
        if (relVel <= 0) return; // they are separating, no bounce

        double m1 = this.mass;
        double m2 = otherMass;
        double e = coefficientOfRestitution;

        double u1p = ( (m1 - e*m2)*u1 + (1 + e)*m2*u2 ) / (m1 + m2);
        double u2p = ( (m2 - e*m1)*u2 + (1 + e)*m1*u1 ) / (m1 + m2);

        double deltaU1 = u1p - u1;
        double deltaU2 = u2p - u2;

        this.vx += deltaU1 * n[0];
        this.vy += deltaU1 * n[1];

        other.setVelocity(
            v2[0] + deltaU2 * n[0],
            v2[1] + deltaU2 * n[1]
        );
    }

    public double getPeriodOfRotation() {
        if (angularVelocity != 0.0) {
            return 2 * Math.PI / angularVelocity;}
        else {
            return 0.0;
        }
    }

    public void bouncePointMass(double coefficientOfRestitution) {
        this.vx *= -coefficientOfRestitution;
        this.vy *= -coefficientOfRestitution;
    }

    public double[] getVelocity() {
        return new double[] {vx, vy};
    }

    public double[] getPosition() {
        return new double[] {x, y};
    }

    public double getMass() {
        return this.mass;
    }

    public void setVelocity(double new_vx, double new_vy){
        this.vx = new_vx;
        this.vy = new_vy;
    }

    public boolean containsPoint(double x, double y) {
        double dx = x - this.x;
        double dy = y - this.y;
        return Math.sqrt(dx*dx + dy*dy) <= this.radius;
    }
    
    @Override
    public String toString() {
        return String.format("Planet with mass = %.2f (%.2f, %.2f) vel=(%.2f, %.2f)", mass, x, y, vx, vy);
    }


}

