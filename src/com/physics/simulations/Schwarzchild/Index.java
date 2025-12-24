package com.physics.simulations.Schwarzchild;

public class Index {
    
    private char index;
    private boolean covariant;

    public Index(char index, boolean covariant) {
        this.index = index;
        this.covariant = covariant;
    }

    public boolean covariant() {
        return this.covariant;
    }

    public char index() {
        return this.index;
    }

}
