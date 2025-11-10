package com.physics.simulations.gravity;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.BasicStroke;

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
 */


public class Planet {
    
    double mass;
    double radius;
    double x, y;
    double vx, vy;
    Color color;
    boolean clicked = false;

    public Planet(double mass, double radius, double x, double y, double vx, double vy, Color color) {
        this.mass = mass;
        this.radius = radius;
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.color = color;
        this.clicked = false;
    }
    
    /**
     * Updates the planet's position based on its velocity.
     * Called during each simulation step.
     * 
     * @param deltaTime Time elapsed since last update
     */
    public void updatePosition(double deltaTime) {
        this.x += this.vx * deltaTime;
        this.y += this.vy * deltaTime;
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
     * Draws the planet on the screen.
     * 
     * @param g2d Graphics2D object for drawing
     */
    public void draw(Graphics2D g2d) {
        g2d.setColor(color);
        int drawX = (int)(x - radius);
        int drawY = (int)(y - radius);
        int size = (int)(radius * 2);
        g2d.fillOval(drawX, drawY, size, size);

        if (clicked) {
            g2d.setStroke(new BasicStroke(3.0f));
            g2d.setColor(Color.YELLOW);
            g2d.drawOval(drawX-5, drawY-5, size+10, size+10);
        }
        // Optional: Draw velocity vector
        // g2d.setColor(Color.YELLOW);
        // g2d.drawLine((int)x, (int)y, (int)(x + vx), (int)(y + vy));
    }

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

        double newRadius = this.radius > other.radius ? this.radius : other.radius;

        Color c1 = this.color;
        Color c2 = other.color;
        int r = (c1.getRed() + c2.getRed()) / 2;
        int g = (c1.getGreen() + c2.getGreen()) / 2;
        int b = (c1.getBlue() + c2.getBlue()) / 2;

        Color newColor = new Color(r, g, b);
        return new Planet(combinedMass, newRadius, newX, newY, newVx, newVy, newColor);
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
        return String.format("Planet at (%.2f, %.2f) vel=(%.2f, %.2f)", x, y, vx, vy);
    }


}

