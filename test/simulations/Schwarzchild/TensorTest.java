package simulations.Schwarzchild;

import java.util.ArrayList;
import java.util.Arrays;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

    // Tensors: {kind}{dim}d + _i (covariant i) / $i (contravariant i) per index.
    // Scalars have no indices → S2d0/S2d1, S4d0/S4d1 by slot.

    static Tensor S2d0 = new Tensor("S", scalar0, scalar_indices, 2);
    static Tensor S2d1 = new Tensor("S", scalar1, scalar_indices, 2);
    static Tensor S4d0 = new Tensor("S", scalar0, scalar_indices, 4);
    static Tensor S4d1 = new Tensor("S", scalar1, scalar_indices, 4);

    static Tensor V4d_a = new Tensor("V", data0, covar_vector_indices1, 4);
    static Tensor V4d_b = new Tensor("V", data1, covar_vector_indices2, 4);
    static Tensor V4d$a = new Tensor("V", data0, contra_vector_indices1, 4);
    static Tensor V4d$b = new Tensor("V", data1, contra_vector_indices2, 4);

    static Tensor V2d_a = new Tensor("V",
            new ArrayList<>(Arrays.asList(3.14, -0.618)),
            new Index[] { a1 },
            2);
    static Tensor V2d$a = new Tensor("V",
            new ArrayList<>(Arrays.asList(1.41, 2.71)),
            new Index[] { a2 },
            2);
    static Tensor V2d_b = new Tensor("V",
            new ArrayList<>(Arrays.asList(-4.2, 7.89)),
            new Index[] { b1 },
            2);

    static Tensor M2d_a$a = new Tensor("T", data0, matrix_indices0, 2);
    static Tensor M2d_a$a_dup = new Tensor("T", data0, matrix_indices0, 2);
    static Tensor M2d_b$c = new Tensor("T", data1, matrix_indices1, 2);
    static Tensor M2d_c$a = new Tensor("N", data2, matrix_indices2, 2);

    static Tensor I2d_a_b = new Tensor("I",
            new ArrayList<>(Arrays.asList(1.0, 0.0, 0.0, 1.0)),
            new Index[] { a1, b1 },
            2);

    static Tensor I2d_a_b_x8 = new Tensor("I",
            new ArrayList<>(Arrays.asList(8.0, 0.0, 0.0, 8.0)),
            new Index[] { a1, b1 },
            2);

    static Tensor I2d$a$b = new Tensor("I",
            new ArrayList<>(Arrays.asList(1.0, 0.0, 0.0, 1.0)),
            new Index[] { a2, b2 },
            2);

    static Tensor I2d$a_b = new Tensor("I",
            new ArrayList<>(Arrays.asList(1.0, 0.0, 0.0, 1.0)),
            new Index[] { a2, b1 },
            2);

    static Tensor M2d_c$b = new Tensor("R",
            new ArrayList<>(Arrays.asList(0.37, -2.14, 9.01, 0.5)),
            new Index[] { c1, b2 },
            2);

    static Tensor M2d_b$a = new Tensor("R",
            new ArrayList<>(Arrays.asList(6.0, -2.0, 3.0, 1.0)),
            new Index[] { b1, a2 },
            2);

    @Nested
    @DisplayName("Create Tensor")
    class CreateTensorCategory {
        @Test
        void testCreateTensor1() {
            System.out.println("Test 1: Creating a tensor");

            // Ensure basic methods working properly
            assertTrue(M2d_a$a instanceof Tensor, "M2d_a$a not of type Tensor in testCreateTensor()");
            assertEquals(1.0, M2d_a$a.getdata().get(0));
            assertEquals(2.0, M2d_a$a.getdata().get(1));
            assertEquals(3.0, M2d_a$a.getdata().get(2));
            assertEquals(4.0, M2d_a$a.getdata().get(3));

            assertEquals(2, M2d_a$a.getDim());

            assertTrue(M2d_a$a.getIndices()[0] == a1);
            assertTrue(M2d_a$a.getIndices()[1] == a2);

            System.out.println(M2d_a$a.toString());
        }
    }

    @Nested
    @DisplayName("Tensor Contraction")
    class TensorContractionCategory {
        @Test
        void testTensorContraction1() {
            System.out.println("Test: tensor contraction (trace of M2d_a$a)");
            assertTrue(M2d_b$c instanceof Tensor, "M2d_b$c not of type Tensor in testTensorContraction()");

            assertEquals(4, M2d_b$c.getdata().size());
            // Contract over the "a" index which appears twice in the index list
            Tensor C1 = M2d_a$a.contract('a');
            // Contraction here computes the trace of M2d_a$a, which is a scalar
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
            System.out.println("Test: scalar × scalar");
            Tensor scalarProd = S2d0.mulTensor(S2d1);
            assertEquals(-5.0, scalarProd.getdata().get(0), "Standard Scalar Multiplication");
            assertEquals(1, scalarProd.getdata().size(), "Product of two scalars is a scalar");
            assertEquals(0, scalarProd.getIndices().length, "Scalars do not have any indices");
        }

        @Test
        void testVectorScalarMult() {
            System.out.println("Test: vector × scalar (both orders)");
            Tensor result1 = V4d_a.mulTensor(S4d0);
            ArrayList<Double> expected_data = new ArrayList<>(Arrays.asList(10.0, 20.0, 30.0, 40.0));
            Tensor expected = new Tensor("T", expected_data, V4d_a.getIndices(), V4d_a.getDim());
            assertTrue(expected.equals(result1), "Vector Scalar Multiplication");
            assertEquals(4, result1.getdata().size(), "Product of a scalar and vector is a vector");
            assertEquals(1, result1.getIndices().length, "Vectors should only have 1 index");

            Tensor result2 = S4d0.mulTensor(V4d_a);
            assertTrue(expected.equals(result2), "Vector Scalar Multiplication");
            assertEquals(4, result2.getdata().size(), "Product of a scalar and vector is a vector");
            assertEquals(1, result2.getIndices().length, "Vectors should only have 1 index");

            assertTrue(result2.equals(result1), "Vector Scalar Should be symmetric");
            assertTrue(result1.equalswithtolerance(result2, 0.0001), "Vector Scalar Should be symmetric (with tolerence)");
            assertTrue(result1.strictequals(result2), "Vector Scalar Should be strictly symmetric");
        }
    

        @Test
        void testInnerProduct() {
            System.out.println("Test: inner product");
            Tensor result1 = V4d_a.mulTensor(V4d$a);
            Tensor result2 = V4d$a.mulTensor(V4d_a);

            ArrayList<Double> data = new ArrayList<>(Arrays.asList(30.0));
            Tensor expected = new Tensor("A", data, new Index[]{}, 4);

            assertTrue(expected.equals(result1), "Inner product between vectors");
            assertEquals(1, result1.getdata().size(), "Inner product of two vectors is a scalar");
            assertEquals(0, result1.getIndices().length, "Scalars should have no indicies");

            assertTrue(expected.equals(result2), "Inner product between vectors");
            assertEquals(1, result2.getdata().size(), "Inner product of two vectors is a scalar");
            assertEquals(0, result2.getIndices().length, "Scalars should have no indicies");

            assertTrue(result2.equals(result1), "Inner product should be symmetric");
            assertTrue(result1.equals(result2), "Inner product should be symmetric");
            assertTrue(result1.equalswithtolerance(result2, 0.0001), "Inner product should be symmetric(with tolerence)");
            assertTrue(result1.strictequals(result2), "Inner product should be strictly symmetric");
        }

        @Test
        void testMatrixVecMult() {
            System.out.println("Test: matrix × vector");
            // Contravariant v^b so it pairs with _b on M2d_b$c / I2d$a_b (no duplicate same-variance b).
            Tensor V2d$b = new Tensor("V",
                    new ArrayList<>(Arrays.asList(1.0, 2.0)),
                    new Index[] { b2 },
                    2);

            // δ^a_b v^b = v^a (identity on remaining contravariant a)
            Tensor u1 = I2d$a_b.mulTensor(V2d$b);
            Tensor expect1 = new Tensor("V",
                    new ArrayList<>(Arrays.asList(1.0, 2.0)),
                    new Index[] { a2 },
                    2);
            assertEquals(2, u1.getDim());
            assertEquals(1, u1.getIndices().length);
            assertTrue(u1.getIndices()[0] == a2);
            assertTrue(u1.strictequals(expect1), "δ^a_b v^b should yield v^a");

            // M2d_b$c is [-1,0,0,-1] on (_b, ^c); same v^b → u^c = (-1, -2) (remaining index is ^c)
            Tensor u2 = M2d_b$c.mulTensor(V2d$b);
            Tensor expect2 = new Tensor("V",
                    new ArrayList<>(Arrays.asList(-1.0, -2.0)),
                    new Index[] { c2 },
                    2);
            assertEquals(2, u2.getDim());
            assertEquals(1, u2.getIndices().length);
            assertTrue(u2.getIndices()[0] == c2);
            assertTrue(u2.strictequals(expect2), "(-I) along b contracts v^b to -v on ^c");

            // Non-identity mixed matrix R_{c}{}^{b}: contract with covariant v_b (not δ or a multiple of I).
            Tensor V2d_b = new Tensor("V",
                    new ArrayList<>(Arrays.asList(1.0, 2.0)),
                    new Index[] { b1 },
                    2);
            Tensor u3 = M2d_c$b.mulTensor(V2d_b);
            Tensor expect3 = new Tensor("V",
                    new ArrayList<>(Arrays.asList(-3.91, 10.01)),
                    new Index[] { c1 },
                    2);
            assertEquals(2, u3.getDim());
            assertEquals(1, u3.getIndices().length);
            assertTrue(u3.getIndices()[0] == c1);
            assertTrue(u3.equalswithtolerance(expect3, 0.0001),
                    "R_c^b v_b should give u_c = (0.37·1-2.14·2, 9.01·1+0.5·2)");
        }

        @Test
        void testMatrixMatMult() {
            System.out.println("Test: matrix × matrix");
            // δ_{a}^{b} M_{b}^{c} = M_{a}^{c}: need I with _a and ^b so outer indices are not two _b's.
            Tensor I2d_a$b = new Tensor("I",
                    new ArrayList<>(Arrays.asList(1.0, 0.0, 0.0, 1.0)),
                    new Index[] { a1, b2 },
                    2);
            Tensor IM = I2d_a$b.mulTensor(M2d_b$c);
            Tensor expectIdentityCase = new Tensor("T",
                    new ArrayList<>(Arrays.asList(-1.0, 0.0, 0.0, -1.0)),
                    new Index[] { a1, c2 },
                    2);
            assertEquals(2, IM.getDim());
            assertEquals(2, IM.getIndices().length);
            assertTrue(IM.strictequals(expectIdentityCase),
                    "δ_a^b M_b^c should equal M with b summed and a replacing b on the left");

            // M_{b}^{c} N_{c}^{a} with M = -I, N = 4·I → product = -4·I on (_b, ^a)
            Tensor MN = M2d_b$c.mulTensor(M2d_c$a);
            Tensor expectGeneralCase = new Tensor("P",
                    new ArrayList<>(Arrays.asList(-4.0, 0.0, 0.0, -4.0)),
                    new Index[] { b1, a2 },
                    2);
            assertEquals(2, MN.getDim());
            assertEquals(2, MN.getIndices().length);
            assertTrue(MN.strictequals(expectGeneralCase),
                    "(-I)_b^c (4I)_c^a should be (-4I)_b^a");

            // M_{c}^{b} N_{b}^{a} on static M2d_c$b and M2d_b$a → (MN)_{c}^{a}
            Tensor MCb = M2d_c$b.mulTensor(M2d_b$a);
            Tensor expectCbBa = new Tensor("S",
                    new ArrayList<>(Arrays.asList(-4.2, -2.88, 55.56, -17.52)),
                    new Index[] { c1, a2 },
                    2);
            assertEquals(2, MCb.getDim());
            assertEquals(2, MCb.getIndices().length);
            assertTrue(MCb.strictequals(expectCbBa),
                    "M_c^b M_b^a should contract b to yield P[c,a] = Σ_b R[c,b] N[b,a]");
        }

    }

    @Nested
    @DisplayName("Error Cases")
    class ErrorCasesCategory {

        @Test
        void contractWithNoSuchIndex_throws() {
            // No 'z' on M2d_a$a → contract() finds zero matches (implementation reports as same-position error).
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> M2d_a$a.contract('z'));
            assertEquals("Contracted indices can not be in the same position", ex.getMessage());
        }

        @Test
        void contractWithOnlyOneOccurrence_throws() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> V2d_a.contract('a'));
            assertEquals("Less than two indices found with id : a", ex.getMessage());
        }

        @Test
        void mulTensorMismatchedDimension_throws() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> S4d0.mulTensor(M2d_a$a));
            assertEquals("Dimensions of Tensors must match to multiply", ex.getMessage());
        }

        @Test
        void constructorDataLengthMismatch_throws() {
            ArrayList<Double> tooShort = new ArrayList<>(Arrays.asList(1.0, 2.0));
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> new Tensor("Bad", tooShort, matrix_indices0, 2));
            assertEquals("Length of Tensor does not align with dimension and number of indices",
                    ex.getMessage());
        }

        @Test
        void constructorDuplicateSameVarianceIndex_throws() {
            Index[] bad = { a1, new Index('a', true) };
            ArrayList<Double> okSize = new ArrayList<>(Arrays.asList(1.0, 2.0, 3.0, 4.0));
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> new Tensor("Bad", okSize, bad, 2));
            assertEquals("Duplicate co/contravariant index", ex.getMessage());
        }
    }
}

