package tensor;

/** Thrown when index letters or data length violate tensor invariants (e.g. duplicate same-type indices, wrong flat size). */
public class TensorConsistencyException extends TensorException {
    public TensorConsistencyException(String message) {
        super(message);
    }
}
