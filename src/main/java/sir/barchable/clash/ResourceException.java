package sir.barchable.clash;

/**
 * @author Sir Barchable
 *         Date: 22/04/15
 */
public class ResourceException extends RuntimeException {
    public ResourceException() {
    }

    public ResourceException(String message) {
        super(message);
    }

    public ResourceException(String message, Throwable cause) {
        super(message, cause);
    }

    public ResourceException(Throwable cause) {
        super(cause);
    }
}
