package com.physics.simulations.gravity;

import java.awt.Color;
import java.awt.Graphics2D;

/**
 * PointMass - A stationary planet that doesn't move but exerts gravitational force.
 * Extends Planet but overrides updateVelocity and updatePosition to remain stationary.
 */
public class PointMass extends Planet {
    
    /**
     * Constructor for PointMass with default radius and color
     */
    public PointMass(double mass, double x, double y) {
        this(mass, x, y, 10, Color.WHITE, null);
    }

    /**
     * Constructor for PointMass with default name
     */
    public PointMass(double mass, double x, double y, double radius, Color color) {
        this(mass, x, y, radius, color, null);
    }

    /**
     * Constructor for PointMass with all parameters
     */
    public PointMass(double mass, double x, double y, double radius, Color color, String name) {
        // Call Planet constructor with zero velocity and angular velocity (stationary)
        super(mass, radius, x, y, 0.0, 0.0, 0.0, color, null, name);
    }

    /**
     * Override updateVelocity - PointMass doesn't move, so velocity never changes
     */
    @Override
    public void updateVelocity(double ax, double ay, double deltaTime) {
        // Do nothing - PointMass is stationary
    }

    /**
     * Override updatePosition - PointMass doesn't move, so position never changes
     */
    @Override
    public void updatePosition(double deltaTime, double timeFactor) {
        // Do nothing - PointMass is stationary
        // Rotation also doesn't change (angularVelocity is 0)
    }

    /**
     * Override draw to use simpler rendering (no texture/rotation needed for stationary mass)
     */
    @Override
    public void draw(Graphics2D g2d) {
        g2d.setColor(color);
        int drawX = (int)(x - radius);
        int drawY = (int)(y - radius);
        int size = (int)(radius * 2);
        g2d.fillOval(drawX, drawY, size, size);
        
        // Draw yellow circle when selected (same as Planet)
        if (clicked) {
            g2d.setStroke(new java.awt.BasicStroke(3.0f));
            g2d.setColor(Color.YELLOW);
            g2d.drawOval(drawX-5, drawY-5, size+10, size+10);
        }
    }

    /**
     * Merges with another planet, creating a new PointMass with combined mass.
     * PointMass always wins in a merge (remains stationary).
     * 
     * @param other The planet to merge with
     * @return A new PointMass with combined properties
     */
    public PointMass merge(Planet other) {
        double combinedMass = this.mass + other.mass;
        
        // Use the larger radius
        double newRadius = this.radius > other.radius ? this.radius : other.radius;
        
        // Average the colors
        Color c1 = this.color;
        Color c2 = other.color;
        int r = (c1.getRed() + c2.getRed()) / 2;
        int g = (c1.getGreen() + c2.getGreen()) / 2;
        int b = (c1.getBlue() + c2.getBlue()) / 2;
        Color newColor = new Color(r, g, b);
        
        // Keep PointMass name if it has one, otherwise use other's name, or null
        String mergedName = (this.name != null && !this.name.trim().isEmpty()) 
            ? this.name 
            : (other.name != null && !other.name.trim().isEmpty() ? other.name : null);
        
        // Position stays at PointMass location (stationary)
        return new PointMass(combinedMass, this.x, this.y, newRadius, newColor, mergedName);
    }
}
