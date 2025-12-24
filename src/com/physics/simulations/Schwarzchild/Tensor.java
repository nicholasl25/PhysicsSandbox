package com.physics.simulations.Schwarzchild;

import java.util.ArrayList;

public class Tensor {

    Index[] indices;
    ArrayList<Double> data;
    int dim;
    double size;
    
    public Tensor(ArrayList<Double> data,  Index[] indices, int dim) {
        this.data = data;
        this.indices = indices;
        this.dim = dim;
        this.size = Math.pow(dim, indices.length);
    }

    public Index[] getIndices() {
        return this.indices;
    }

    public int getDim() {
        return this.dim;
    }

    public Tensor mulTensor(Tensor other) throws Exception {
        assert this.dim == other.dim: "Dimensions of Tensors must match to multiply";
        int total_idx = this.indices.length + other.indices.length;
        int total_data_size = this.data.size() * other.data.size();

        Index[] new_indices = new Index[total_idx];

        /* Created list of combined indices : duplicates removed later with contract()  */
        System.arraycopy(this.indices, 0, new_indices, 0, this.indices.length);
        System.arraycopy(other.indices, 0, new_indices,  this.indices.length, other.indices.length);

        /* Compute simple outer product between tensors */
        ArrayList<Double> outer_prod = new ArrayList<>();
        for (double y : this.data) {
            ArrayList<Double> result = new ArrayList<>(other.data.stream().map(x -> x * y).toList());
            outer_prod.addAll(result);
        }

        Tensor outer_prod_tensor = new Tensor(outer_prod, new_indices, total_data_size);

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
                        String s = String.format("Error Concatinating Index %s in Tensor Multiplication: Duplicated Covariant Index", idx);
                        throw new Exception(s);
                    }
                    else {
                        String idx = Character.toString(new_indices[i].index());
                        String s = String.format("Error Concatinating Index %s in Tensor Multiplication: Duplicated Contravariant Index", idx);
                        throw new Exception(s);
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

    public Tensor contract(char idx) {
        /* Find position of two summation indices */
        int idxPos1 = -1;
        int idxPos2 = -1;
        boolean second = false;
        for (int i = 0; i < this.indices.length; i++) {
            if (idx == this.indices[i].index()) {
                if (second) {
                    idxPos2 = i;
                    second = true;
                }
                else {
                    idxPos1 = i;
                }
            }
        }

        assert idxPos1 != idxPos2: "Contracted indices can not be in the same position";
        double[] new_data = new double[this.data.size() / (this.dim * this.dim)];

        /* Sum over repeated indices : Einstein Summation Convention */
        for (int i = 0; i < this.data.size(); i++) {
            int idx1 = (i / (int)Math.pow(this.dim, idxPos1)) % this.dim;
            int idx2 = (i / (int)Math.pow(this.dim, idxPos2)) % this.dim;
            if (idx1 == idx2) {
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

        Tensor Contracted = new Tensor(new_data_list, new_indices, this.dim);
        return Contracted;
    }


    public int[] getIndicesVal(int idx) {
        int[] index_lst = new int[this.indices.length];
        for (int i = 0; i < this.indices.length; i++) {
            int remainder = idx % (int)Math.pow(this.dim, i);
            index_lst[this.indices.length - (i+1)] = remainder;
        }
        return index_lst;
    }

    public int newIdx(int idx, int idx1, int idx2) {
        int count = 0;
        int[] index_lst = getIndicesVal(idx);
        int i = 0;
        int j = 0;

        while (i < index_lst.length) {
            if (i == idx1 || i == idx2) {
                i++;
                continue;
            }
            else {
                count += index_lst[this.indices.length - (i+1)] * Math.pow(this.dim, j);
                i++;
                j++;
            }
        }
        return count;
    }


    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Tensor: ");
        for (int i = 0; i < this.indices.length; i++) {
            
            if (this.indices[i].covariant()) {
                sb.append("_");
            }
            else {
                sb.append("^");
            }
            
            sb.append(this.indices[i].index());
        }
        sb.append("\n");
        for (int i = 0; i < this.data.size(); i++) {
            sb.append(this.data.get(i));
        }

        return sb.toString();
    }
}
