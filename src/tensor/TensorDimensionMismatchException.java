package tensor;

/** Thrown when combining tensors whose per-index basis sizes ({@code dim}) disagree. */
public class TensorDimensionMismatchException extends TensorException {
    public TensorDimensionMismatchException(String message) {
        super(message);
    }
}
