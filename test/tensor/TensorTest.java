package tensor;

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

    // Scalars now intentionally use dim=0 (dimensionless scalar object).
    static Scalar S0 = new Scalar("S", 10.0);
    static Scalar S1 = new Scalar("S", -0.5);

    // Legacy scalar representation retained for constructor/migration behavior checks.
    static Tensor Slegacy = new Tensor("S", scalar0, scalar_indices, 2);

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

    static Tensor M2d_a$a = new Tensor("T", data0, matrix_indices0, 2);
    static Tensor M2d_b$c = new Tensor("T", data1, matrix_indices1, 2);
    static Tensor M2d_c$a = new Tensor("N", data2, matrix_indices2, 2);

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
            assertTrue(M2d_a$a instanceof Tensor, "M2d_a$a not of type Tensor");
            assertEquals(2, M2d_a$a.getDim());
            assertEquals(1.0, M2d_a$a.getdata().get(0));
            assertEquals(4.0, M2d_a$a.getdata().get(3));
            assertTrue(M2d_a$a.getIndices()[0] == a1);
            assertTrue(M2d_a$a.getIndices()[1] == a2);
        }

    }

    @Nested
    @DisplayName("Tensor Contraction")
    class TensorContractionCategory {
        @Test
        void testTensorContraction1() {
            Tensor C1 = M2d_a$a.contract('a');
            assertEquals(1, C1.getdata().size(), "Trace of matrix should be scalar");
            assertEquals(5.0, C1.getdata().get(0), "Trace of matrix should be computed correctly");
            assertEquals(0, C1.getIndices().length, "Scalars do not have any indices");
        }
    }

    @Nested
    @DisplayName("Tensor Multiplication")
    class TensorMultiplicationCategory {
        @Test
        void testScalarLegacyTensorMultAllowed() {
            Tensor result = S0.mulTensor(Slegacy);
            assertEquals(10.0 * 10.0, result.getdata().get(0));
            assertEquals(2, result.getDim(), "dim=0 scalar should adopt non-zero partner dim");
            assertEquals(0, result.getIndices().length);
        }

        @Test
        void testInnerProduct() {
            Tensor result1 = V4d_a.mulTensor(V4d$a);
            Tensor result2 = V4d$a.mulTensor(V4d_a);
            Tensor expected = new Tensor("A", new ArrayList<>(Arrays.asList(30.0)), new Index[]{}, 4);

            assertTrue(expected.equals(result1), "Inner product between vectors");
            assertTrue(expected.equals(result2), "Inner product between vectors");
            assertTrue(result1.strictequals(result2), "Inner product should be strictly symmetric");
        }

        @Test
        void testMatrixVecMult() {
            Tensor V2d$b = new Tensor("V",
                    new ArrayList<>(Arrays.asList(1.0, 2.0)),
                    new Index[] { b2 },
                    2);

            Tensor u1 = I2d$a_b.mulTensor(V2d$b);
            Tensor expect1 = new Tensor("V",
                    new ArrayList<>(Arrays.asList(1.0, 2.0)),
                    new Index[] { a2 },
                    2);
            assertTrue(u1.strictequals(expect1), "δ^a_b v^b should yield v^a");

            Tensor u2 = M2d_b$c.mulTensor(V2d$b);
            Tensor expect2 = new Tensor("V",
                    new ArrayList<>(Arrays.asList(-1.0, -2.0)),
                    new Index[] { c2 },
                    2);
            assertTrue(u2.strictequals(expect2), "(-I)_b^c v^b = -v^c");
        }

        @Test
        void testMatrixMatMult() {
            Tensor MN = M2d_b$c.mulTensor(M2d_c$a);
            Tensor expectGeneralCase = new Tensor("P",
                    new ArrayList<>(Arrays.asList(-4.0, 0.0, 0.0, -4.0)),
                    new Index[] { b1, a2 },
                    2);
            assertTrue(MN.strictequals(expectGeneralCase),
                    "(-I)_b^c (4I)_c^a should be (-4I)_b^a");

            Tensor MCb = M2d_c$b.mulTensor(M2d_b$a);
            Tensor expectCbBa = new Tensor("S",
                    new ArrayList<>(Arrays.asList(-4.2, -2.88, 55.56, -17.52)),
                    new Index[] { c1, a2 },
                    2);
            assertTrue(MCb.strictequals(expectCbBa),
                    "M_c^b M_b^a should contract b to yield P[c,a]");
        }
    }

    @Nested
    @DisplayName("Error Cases")
    class ErrorCasesCategory {
        @Test
        void contractWithNoSuchIndex_throws() {
            TensorContractionException ex = assertThrows(TensorContractionException.class,
                    () -> M2d_a$a.contract('z'));
            assertEquals("Less than two indices found with id : z", ex.getMessage());
        }

        @Test
        void contractWithOnlyOneOccurrence_throws() {
            TensorContractionException ex = assertThrows(TensorContractionException.class,
                    () -> V2d_a.contract('a'));
            assertEquals("Less than two indices found with id : a", ex.getMessage());
        }

        @Test
        void mulTensorMismatchedDimensionBothNonZero_throws() {
            TensorDimensionMismatchException ex = assertThrows(TensorDimensionMismatchException.class,
                    () -> V4d_a.mulTensor(M2d_a$a));
            assertEquals("Dimensions of Tensors must match to multiply", ex.getMessage());
        }

        @Test
        void constructorDataLengthMismatch_throws() {
            ArrayList<Double> tooShort = new ArrayList<>(Arrays.asList(1.0, 2.0));
            TensorConsistencyException ex = assertThrows(TensorConsistencyException.class,
                    () -> new Tensor("Bad", tooShort, matrix_indices0, 2));
            assertEquals("Length of Tensor does not align with dimension and number of indices",
                    ex.getMessage());
        }

        @Test
        void constructorDuplicateSameVarianceIndex_throws() {
            Index[] bad = { a1, new Index('a', true) };
            ArrayList<Double> okSize = new ArrayList<>(Arrays.asList(1.0, 2.0, 3.0, 4.0));
            TensorConsistencyException ex = assertThrows(TensorConsistencyException.class,
                    () -> new Tensor("Bad", okSize, bad, 2));
            assertEquals("Duplicate co/contravariant index", ex.getMessage());
        }
    }
}
