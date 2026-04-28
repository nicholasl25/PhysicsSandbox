package tensor;

/** Base unchecked exception for tensor construction and algebra errors. */
public class TensorException extends RuntimeException {
    public TensorException(String message) {
        super(message);
    }
}
