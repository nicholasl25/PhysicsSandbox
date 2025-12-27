package com.physics.simulations.Schwarzchild;

import java.util.ArrayList;
import java.util.Arrays;

public class TensorTest {

    // Create list of indices to be used several times
    static Index a1 = new Index('a', true);
    static Index a2 = new Index('a', false);
    static Index b1 = new Index('b', true);
    static Index b2 = new Index('b', false);
    static Index c1 = new Index('c', true);
    static Index c2 = new Index('c', false);

    
    public static void main(String[] args) {
        System.out.println("Testing Tensor class...\n");
        
        testCreateTensor();
        testTensorContraction();
        testTensorMultiplication();
        testErrorCases();
        
        System.out.println("\nAll tests completed!");
    }

    
    
    public static void testCreateTensor() {
        System.out.println("Test 1: Creating a tensor");

        // Create a 2D tensor (2x2 matrix)
        ArrayList<Double> data = new ArrayList<>();
        data.add(1.0);
        data.add(2.0);
        data.add(3.0);
        data.add(4.0);

        Index[] indices = {a1, a2};

        Tensor T1 = new Tensor("T", data, indices, 2);
        System.out.println(T1.toString());

    }

    public static void testTensorContraction() {
        ArrayList<Double> data1 = new ArrayList<>();
        data1.addAll(Arrays.asList(-1.0, 0.0, 0.0, -1.0));
        Index[] indices1 = {a1, a2};

        Tensor T1 = new Tensor("T", data1, indices1, 2);
        Tensor T = T1.contract('a');
        System.out.println(T.toString());
        
    }
    
    public static void testTensorMultiplication() {
        // Test diagonal 2x2 matrix multiplication
        ArrayList<Double> data1 = new ArrayList<>();
        data1.addAll(Arrays.asList(-1.0, 0.0, 0.0, -1.0));
        Index[] indices1 = {a1, b2};
        
        ArrayList<Double> data2 = new ArrayList<>();
        data2.addAll(Arrays.asList(4.0, 0.0, 0.0, 4.0));
        Index[] indices2 = {b1, c2};

        Tensor T1 = new Tensor("T", data1, indices1, 2);
        Tensor T2 = new Tensor("N", data2, indices2, 2);


        Tensor T = T1.mulTensor(T2);
        System.out.println(T.toString());


    }
    

    
    public static void testErrorCases() {
        
    }
}

