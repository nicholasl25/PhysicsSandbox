package tensor;

import java.util.ArrayList;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class VectorTest {

    static Index a1 = new Index('a', true);

    static Vector u = new Vector("u", new double[] { 1.0, 2.0, 3.0, 4.0 }, a1, 4);

    @Nested
    @DisplayName("Vector construction")
    class Construction {
        @Test
        void isTensorWithOneIndex() {
            assertTrue(u instanceof Tensor);
            assertEquals(4, u.getDim());
            assertEquals(1, u.getIndices().length);
            assertTrue(a1.equals(u.getIndex()));
        }

        @Test
        void rejectsNonPositiveDim() {
            assertThrows(TensorConsistencyException.class,
                    () -> new Vector("w", new ArrayList<Double>(), a1, 0));
        }
    }

    @Nested
    @DisplayName("Norms")
    class Norms {
        @Test
        void norm2() {
            assertEquals(Math.sqrt(30.0), u.norm2(), 1e-12);
        }

        @Test
        void norm1() {
            assertEquals(10.0, u.norm1(), 1e-12);
        }

        @Test
        void normInf() {
            assertEquals(4.0, u.normInf(), 1e-12);
        }
    }

    @Nested
    @DisplayName("String label")
    class StringLabel {
        @Test
        void toStringUsesVectorPrefix() {
            assertTrue(u.toString().startsWith("Vector:"));
        }
    }
}
