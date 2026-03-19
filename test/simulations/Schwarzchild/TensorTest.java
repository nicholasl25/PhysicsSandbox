package simulations.Schwarzchild;

import java.util.ArrayList;
import java.util.Arrays;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TensorTest {

    // Create list of indices to be used several times
    static Index a1 = new Index('a', true);
    static Index a2 = new Index('a', false);
    static Index b1 = new Index('b', true);
    static Index b2 = new Index('b', false);
    static Index c1 = new Index('c', true);
    static Index c2 = new Index('c', false);


    // Create list of data to be added several times
    static ArrayList<Double> scalar0 = new ArrayList<>(Arrays.asList(10.0));
    static ArrayList<Double> scalar1 = new ArrayList<>(Arrays.asList(-0.5));

    static ArrayList<Double> data0 = new ArrayList<>(Arrays.asList(1.0, 2.0, 3.0, 4.0));
    static ArrayList<Double> data1 = new ArrayList<>(Arrays.asList(-1.0, 0.0, 0.0, -1.0));
    static ArrayList<Double> data2 = new ArrayList<>(Arrays.asList(4.0, 0.0, 0.0, 4.0));

    // Create array of indices to be used several times
    static Index[] scalar_indices = {};

    static Index[] covar_vector_indices1 = {a1};
    static Index[] covar_vector_indices2 = {b1};
    static Index[] contra_vector_indices1 = {a2};
    static Index[] contra_vector_indices2 = {b2};


    static Index[] matrix_indices0 = {a1, a2};
    static Index[] matrix_indices1 = {b1, c2};
    static Index[] matrix_indices2 = {c1, a2};

    // Create Tensors for testing

    // Scalars
    static Tensor S0 = new Tensor("S", scalar0, scalar_indices, 2);
    static Tensor S1 = new Tensor("S", scalar1, scalar_indices, 2);
    static Tensor S2 = new Tensor("S", scalar0, scalar_indices, 4);
    static Tensor S3 = new Tensor("S", scalar1, scalar_indices, 4);

    // Vectors 
    static Tensor V1 = new Tensor("V", data0, covar_vector_indices1, 4);
    static Tensor V2 = new Tensor("V", data1, covar_vector_indices2, 4);
    static Tensor V3 = new Tensor("V", data0, contra_vector_indices1, 4);
    static Tensor V4 = new Tensor("V", data1, contra_vector_indices2, 4);

    // 2x2 simple matrices
    static Tensor T0 = new Tensor("T", data0, matrix_indices0, 2);
    static Tensor T0_copy = new Tensor("T", data0, matrix_indices0, 2);
    static Tensor T1 = new Tensor("T", data1, matrix_indices1, 2);
    static Tensor T2 = new Tensor("N", data2, matrix_indices2, 2);

    @Nested
    @DisplayName("Create Tensor")
    class CreateTensorCategory {
        @Test
        void testCreateTensor1() {
            System.out.println("Test 1: Creating a tensor");

            // Ensure basic methods working properly
            assertTrue(T0 instanceof Tensor, "T0 not of type Tensor in testCreateTensor()");
            assertEquals(1.0, T0.getdata().get(0));
            assertEquals(2.0, T0.getdata().get(1));
            assertEquals(3.0, T0.getdata().get(2));
            assertEquals(4.0, T0.getdata().get(3));

            assertEquals(2, T0.getDim());

            assertTrue(T0.getIndices()[0] == a1);
            assertTrue(T0.getIndices()[1] == a2);

            System.out.println(T0.toString());
        }
    }

    @Nested
    @DisplayName("Tensor Contraction")
    class TensorContractionCategory {
        @Test
        void testTensorContraction1() {
            assertTrue(T1 instanceof Tensor, "T1 not of type Tensor in testTensorContraction()");

            assertEquals(4, T1.getdata().size());
            // Contract over the "a" index which appears twice in the index list
            Tensor C1 = T0.contract('a');
            // Contraction here computes the Trace of T1, which is a scalar
            assertEquals(1, C1.getdata().size(), "Trace of matrix should be scalar");
            assertEquals(5.0, C1.getdata().get(0), "Trace of matrix should be computed correctly");
            assertEquals(0, C1.getIndices().length, "Scalars do not have any indices");
        }
    }

    @Nested
    @DisplayName("Tensor Multiplication")
    class TensorMultiplicationCategory {
        @Test
        void testScalarScalarMult() {
            Tensor S2 = S0.mulTensor(S1);
            assertEquals(-5.0, S2.getdata().get(0), "Standard Scalar Multiplication");
            assertEquals(1, S2.getdata().size(), "Product of two scalars is a scalar");
            assertEquals(0, S2.getIndices().length, "Scalars do not have any indices");
        }

        @Test
        void testVectorScalarMult() {
            Tensor result1 = V1.mulTensor(S2);
            ArrayList<Double> expected_data = new ArrayList<>(Arrays.asList(10.0, 20.0, 30.0, 40.0));
            Tensor expected = new Tensor("T", expected_data, V1.getIndices(), V1.getDim());
            assertTrue(expected.equals(result1), "Vector Scalar Multiplication");
            assertEquals(4, result1.getdata().size(), "Product of a scalar and vector is a vector");
            assertEquals(1, result1.getIndices().length, "Vectors should only have 1 index");

            Tensor result2 = S2.mulTensor(V1);
            assertTrue(expected.equals(result2), "Vector Scalar Multiplication");
            assertEquals(4, result2.getdata().size(), "Product of a scalar and vector is a vector");
            assertEquals(1, result2.getIndices().length, "Vectors should only have 1 index");

            assertTrue(result2.equals(result1), "Vector Scalar Should be symmetric");
            assertTrue(result1.equalswithtolerance(result2, 0.0001), "Vector Scalar Should be symmetric (with tolerence)");
            assertTrue(result1.strictequals(result2), "Vector Scalar Should be strictly symmetric");
        }
    }

    @Nested
    @DisplayName("Error Cases")
    class ErrorCasesCategory {
        // Intentionally empty for now
    }
}

