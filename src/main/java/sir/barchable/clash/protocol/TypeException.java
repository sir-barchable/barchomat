package sir.barchable.clash.protocol;

/**
 * A problem in the protocol definition files.
 *
 * @author Sir Barchable
 *         Date: 25/04/15
 */
public class TypeException extends RuntimeException {
    public TypeException() {
    }

    public TypeException(String message) {
        super(message);
    }

    public TypeException(String message, Throwable cause) {
        super(message, cause);
    }

    public TypeException(Throwable cause) {
        super(cause);
    }
}
