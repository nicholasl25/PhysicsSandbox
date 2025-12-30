package simulations.NewtonianGravity;

import java.awt.Color;

/**
 * PointMass - A stationary planet that doesn't move but exerts gravitational force.
 * Extends Planet but overrides updateVelocity and updatePosition to remain stationary.
 */
public class PointMass extends Planet {
    
    /**
     * Constructor for PointMass with all parameters.
     * texturePath and name can be null.
     * angularVelocity is in the same position as Planet constructor (after radius, before color).
     */
    public PointMass(double mass, double radius, double x, double y, double angularVelocity, double temperature, Color color, String texturePath, String name) {
        // Call Planet constructor with zero velocity (stationary), but allow angular velocity
        super(mass, radius, x, y, 0.0, 0.0, angularVelocity, temperature, color, texturePath, name);
    }

    /**
     * Override updateVelocity - PointMass doesn't move, so velocity never changes
     */
    @Override
    public void updateVelocity(Vector accel, double deltaTime) {
        // Do nothing - PointMass is stationary
    }

    /**
     * Override updatePosition - PointMass doesn't move, so position never changes
     * But still allow rotation if angularVelocity is set
     */
    @Override
    public void updatePosition(double deltaTime, double timeFactor) {
        // Don't update position (x, y) - PointMass is stationary
        // But still update rotation angle if angularVelocity is set
        this.rotationAngle += angularVelocity * timeFactor * deltaTime;
        this.rotationAngle %= (Math.PI * 2);
        if (this.rotationAngle < 0) {
            this.rotationAngle += Math.PI * 2;
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

        // Texture path is not used for PointMass, so set to null
        String newTexturePath = null;
        if (this.radius > other.radius && this.texturePath != null) {

            newTexturePath = this.texturePath;
        } else {
            newTexturePath = other.texturePath;
        }
        
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
        
        double angularMomentum = 0.4 * (this.radius * this.radius * this.mass * this.angularVelocity 
            + other.radius * other.radius * other.mass * other.angularVelocity);
        double newAngularVelocity = 2.5 * angularMomentum / (newRadius * newRadius * combinedMass);
        
        // Average the temperatures
        double newTemperature = (this.temperature * this.mass + other.temperature * other.mass) / combinedMass;

        return new PointMass(combinedMass, newRadius, this.pos.get(0), this.pos.get(1), newAngularVelocity, newTemperature, newColor, newTexturePath, mergedName);
    }
}
