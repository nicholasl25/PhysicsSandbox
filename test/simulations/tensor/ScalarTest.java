package simulations.tensor;

import java.util.ArrayList;
import java.util.Arrays;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ScalarTest {

    static Index a1 = new Index('a', true);
    static Scalar S0 = new Scalar("S", 10.0);
    static Scalar S1 = new Scalar("S", -0.5);
    static Tensor V4d_a = new Tensor("V", new ArrayList<>(Arrays.asList(1.0, 2.0, 3.0, 4.0)), new Index[] { a1 }, 4);

    @Nested
    @DisplayName("Scalar Construction")
    class ScalarConstructionCategory {
        @Test
        void testCreateScalarSubclass() {
            assertTrue(S0 instanceof Tensor);
            assertEquals(0, S0.getDim());
            assertEquals(0, S0.getIndices().length);
            assertEquals(10.0, S0.getValue());
        }
    }

    @Nested
    @DisplayName("Scalar Multiplication")
    class ScalarMultiplicationCategory {
        @Test
        void testScalarScalarMult() {
            Tensor scalarProd = S0.mulTensor(S1);
            assertEquals(-5.0, scalarProd.getdata().get(0), "Standard scalar multiplication");
            assertEquals(1, scalarProd.getdata().size());
            assertEquals(0, scalarProd.getIndices().length);
            assertEquals(0, scalarProd.getDim(), "dim=0 should remain scalar-dimensionless");
        }

        @Test
        void testVectorScalarMult() {
            Tensor result1 = V4d_a.mulTensor(S0);
            ArrayList<Double> expected_data = new ArrayList<>(Arrays.asList(10.0, 20.0, 30.0, 40.0));
            Tensor expected = new Tensor("T", expected_data, V4d_a.getIndices(), V4d_a.getDim());
            assertTrue(expected.equals(result1), "Vector scalar multiplication");
            assertEquals(4, result1.getDim(), "Multiplying by dim=0 scalar should preserve tensor dimension");

            Tensor result2 = S0.mulTensor(V4d_a);
            assertTrue(expected.equals(result2), "Vector scalar multiplication");
            assertEquals(4, result2.getDim(), "Multiplying by dim=0 scalar should preserve tensor dimension");
            assertTrue(result1.strictequals(result2), "Vector scalar multiplication should be symmetric");
        }
    }
}
