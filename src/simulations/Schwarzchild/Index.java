package simulations.Schwarzchild;

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

    @Override 
    public boolean equals(Object obj) {
        if (!(obj instanceof Index)) {
            return false;
        }

        Index idx = (Index) obj;

        boolean same_letter = (this.index() == idx.index());
        boolean same_class = (this.covariant() == idx.covariant());

        return (same_letter & same_class);

    }

}
