package sir.barchable.clash.protocol;

/**
 * A Pdu could not be parsed/serialized.
 *
 * @author Sir Barchable
 */
public class PduException extends RuntimeException {
    public PduException() {
    }

    public PduException(String message) {
        super(message);
    }

    public PduException(String message, Throwable cause) {
        super(message, cause);
    }

    public PduException(Throwable cause) {
        super(cause);
    }
}
