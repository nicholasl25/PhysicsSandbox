package simulations.tensor;

import java.util.ArrayList;

/**
 * Dense tensor stored as a flat {@code data} list: slot {@code indices[0]} is slowest-varying
 * (see {@link #contract(char)} / {@link #mulTensor(Tensor)}). {@code dim} is the size per index;
 * rank 0 uses {@code dim == 0} (see {@link Scalar}).
 */
public class Tensor {

    String name;
    Index[] indices;
    ArrayList<Double> data;
    int dim;
    double size;

    /** Validates {@code data.size() == dim^rank} and no duplicate same-type index letters. */
    public Tensor(String name, ArrayList<Double> data,  Index[] indices, int dim) {
        this.data = data;
        this.indices = indices;
        this.dim = dim;
        this.name = name;
        this.size = Math.pow(dim, indices.length);

        checkConsistent();
    }

    public Index[] getIndices() {
        return this.indices;
    }

    public int getDim() {
        return this.dim;
    }

    public ArrayList<Double> getdata() {
        return this.data;
    }

    /**
     * Tensor product: outer product on components, then Einstein sum over every covariant/contravariant
     * index pair that shares the same letter.
     */
    public Tensor mulTensor(Tensor other){
        if (this.dim != other.dim && this.dim != 0 && other.dim != 0) {
            throw new TensorDimensionMismatchException("Dimensions of Tensors must match to multiply");
        }
        int baseDim = (this.dim != 0) ? this.dim : other.dim;
        int total_idx = this.indices.length + other.indices.length;

        Index[] new_indices = new Index[total_idx];

        /* Created list of combined indices : duplicates removed later with contract()  */
        System.arraycopy(this.indices, 0, new_indices, 0, this.indices.length);
        System.arraycopy(other.indices, 0, new_indices,  this.indices.length, other.indices.length);

        /* Outer product: combined storage uses the same "big-endian" layout as contract():
         * flat = leftDigit0·dim^(na+nb-1) + … + rightDigit(nb-1)·dim^0,
         * i.e. left tensor's index list is the slower (more significant) block, then the right
         * tensor's components vary fastest. That is flatCombined = flatLeft·dim^nb + flatRight. */
        int numLeftSlots = this.indices.length;
        int numRightSlots = other.indices.length;
        int strideRightBlock = (int) Math.pow(baseDim, numRightSlots);
        int outerLinearSize = (int) Math.pow(baseDim, numLeftSlots + numRightSlots);
        ArrayList<Double> outer_prod = new ArrayList<>(outerLinearSize);
        for (int flatCombined = 0; flatCombined < outerLinearSize; flatCombined++) {
            int flatLeft = flatCombined / strideRightBlock;
            int flatRight = flatCombined % strideRightBlock;
            double leftValue = this.data.get(flatLeft);
            double rightValue = other.data.get(flatRight);
            outer_prod.add(leftValue * rightValue);
        }

        Tensor outer_prod_tensor = new Tensor("X", outer_prod, new_indices, baseDim);

        /* Check for duplicate co/contravariant pairs of indices */
        ArrayList<Character> contracted_indices = new ArrayList<>();
        for (int i = 0; i < new_indices.length; i++){
            for (int j = 0; j < i; j++) {
                if (new_indices[i].index() == new_indices[j].index()) {
                    if (new_indices[i].covariant() ^ new_indices[j].covariant()) {
                        contracted_indices.add(new_indices[i].index());
                    }
                    else if (new_indices[i].covariant()) {
                        String idx = Character.toString(new_indices[i].index());
                        String s = String.format("Error concatenating index %s in tensor multiplication: duplicated covariant index", idx);
                        throw new TensorConsistencyException(s);
                    }
                    else {
                        String idx = Character.toString(new_indices[i].index());
                        String s = String.format("Error concatenating index %s in tensor multiplication: duplicated contravariant index", idx);
                        throw new TensorConsistencyException(s);
                    }
                }

            }
        }

        /* Contract over all duplicated indices */
        Tensor intermediate = outer_prod_tensor;
        for (int k = 0; k < contracted_indices.size(); k++) {
            char idx = contracted_indices.get(k);
            intermediate = intermediate.contract(idx);
        }

        return intermediate;

    }

    /** Sums over one repeated index letter (exactly two slots with that letter); removes both slots from the result. */
    public Tensor contract(char idx) {
        /* Find position of two summation indices */
        int idxPos1 = -1;
        int idxPos2 = -1;
        boolean second = false;
        for (int i = 0; i < this.indices.length; i++) {
            if (idx == this.indices[i].index()) {
                if (second) {
                    idxPos2 = i;
                }
                else {
                    idxPos1 = i;
                    second = true;
                }
            }
        }

        if (idxPos1 == -1 || idxPos2 == -1) {
            throw new TensorContractionException(String.format("Less than two indices found with id : %s", idx));
        }
        if (idxPos1 == idxPos2) {
            throw new TensorContractionException("Contracted indices can not be in the same position");
        }
        int length = this.data.size() / (this.dim * this.dim);
        double[] new_data = new double[length];

        /* Sum over repeated indices : Einstein Summation Convention.
         * Linear index i encodes multi-indices with indices[0] the slowest-varying slot:
         * digitAtSlot = (i / dim^(n-1-pos)) % dim. */
        int numSlots = this.indices.length;
        for (int i = 0; i < this.data.size(); i++) {
            int summationDigitAtPos1 = (i / (int) Math.pow(this.dim, numSlots - 1 - idxPos1)) % this.dim;
            int summationDigitAtPos2 = (i / (int) Math.pow(this.dim, numSlots - 1 - idxPos2)) % this.dim;
            if (summationDigitAtPos1 == summationDigitAtPos2) {
                int new_idx = newIdx(i, idxPos1, idxPos2);
                new_data[new_idx] += this.data.get(i);
            }
        }

        /* New data after contraction */
        ArrayList<Double> new_data_list = new ArrayList<>();
        for (double x: new_data) {
            new_data_list.add(x);
        }

        /* New indices after contraction */
        Index[] new_indices = new Index[this.indices.length - 2];
        int newIdx = 0;
        for (int i = 0; i < this.indices.length; i++) {
            if (i != idxPos1 && i != idxPos2) {
                new_indices[newIdx++] = this.indices[i];
            }
        }

        Tensor Contracted = new Tensor(this.name, new_data_list, new_indices, this.dim);
        return Contracted;
    }


    /** Per-slot digits for linear index {@code idx}, matching contract(): slot 0 = indices[0] is slowest. */
    public int[] getIndicesVal(int idx) {
        int numSlots = this.indices.length;
        int[] digitAtSlot = new int[numSlots];
        for (int slot = 0; slot < numSlots; slot++) {
            int powerFromRight = numSlots - 1 - slot;
            digitAtSlot[slot] = (idx / (int) Math.pow(this.dim, powerFromRight)) % this.dim;
        }
        return digitAtSlot;
    }

    /** Linear index in the contracted tensor (same big-endian rule on the surviving slots, in order). */
    public int newIdx(int idx, int idx1, int idx2) {
        int[] digitAtSlot = getIndicesVal(idx);
        int numSlots = this.indices.length;
        int outRank = numSlots - 2;
        int contractedLinearIndex = 0;
        int outSlot = 0;
        for (int slot = 0; slot < numSlots; slot++) {
            if (slot == idx1 || slot == idx2) {
                continue;
            }
            int weightPower = outRank - 1 - outSlot;
            contractedLinearIndex += digitAtSlot[slot] * (int) Math.pow(this.dim, weightPower);
            outSlot++;
        }
        return contractedLinearIndex;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Tensor: ");
        sb.append(this.name);
        for (int i = 0; i < this.indices.length; i++) {
            
            if (this.indices[i].covariant()) {
                sb.append("_");
            }
            else {
                sb.append("^");
            }
            
            sb.append(this.indices[i].index());
        }

        sb.append("\n[");
        for (int i = 0; i < this.data.size(); i++) {
            sb.append(this.data.get(i));
            if (i < this.data.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append("]");

        return sb.toString();
    }

    // Checks equality of data only
    @Override 
    public boolean equals(Object obj) {
        if (!(obj instanceof Tensor)) {
            return false;
        }

        Tensor T = (Tensor) obj;
        ArrayList<Double> other_data = T.getdata();
        ArrayList<Double> this_data = this.data;

        if (this.getDim() != T.getDim()) {
            return false;
        }

        if (this_data.size() != other_data.size()) {
            return false;
        }

        for (int i = 0; i < other_data.size(); i++) {
            if (this_data.get(i).doubleValue() != other_data.get(i).doubleValue()) {
                return false;
            }
        }

        return true;
    }

    // Checks equality of data ArrayList up to some tolerance
    public boolean equalswithtolerance(Tensor T, double tolerance) {
        ArrayList<Double> other_data = T.getdata();
        ArrayList<Double> this_data = this.data;

        if (this_data.size() != other_data.size()) {
            return false;
        }

        for (int i = 0; i < other_data.size(); i++) {
            if (Math.abs(this_data.get(i) - other_data.get(i)) > tolerance) {
                return false;
            }
        }

        return true;
    }

    // Checks equality of indicies and data
    public boolean strictequals(Tensor T) {
        if (!this.equals(T)) {
            return false;
        }
        Index[] idx1 = this.getIndices();
        Index[] idx2 = T.getIndices();
        if (idx1.length != idx2.length) {
            return false;
        }

        for (int i = 0; i < idx1.length; i++) {
            if (!idx1[i].equals(idx2[i])) {
                return false;
            }
        }
        return true;
    }

    public void checkConsistent() {
        if (this.data.size() != Math.pow(this.dim, this.indices.length)) {
            throw new TensorConsistencyException("Length of Tensor does not align with dimension and number of indices");
        }

        for (int i = 0; i < this.indices.length; i++) {
            for (int j = 0; j < i; j++) {
                Index idx1 = this.indices[i];
                Index idx2 = this.indices[j];
                boolean invalid = (idx1.index() == idx2.index()) && (idx1.covariant() == idx2.covariant());
                if (invalid) {
                    throw new TensorConsistencyException("Duplicate co/contravariant index");
                }
            }
        }
    }

    public void setName(String name) {
        this.name = name;
    }
}
