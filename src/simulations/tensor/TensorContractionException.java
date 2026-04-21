package simulations.tensor;

/** Thrown when {@link Tensor#contract(char)} cannot find a valid repeated-index pair or arguments are invalid. */
public class TensorContractionException extends TensorException {
    public TensorContractionException(String message) {
        super(message);
    }
}
