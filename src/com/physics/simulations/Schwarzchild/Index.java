package com.physics.simulations.Schwarzchild;

public class Index {
    
    char index;
    boolean covariant;

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
