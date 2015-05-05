package sir.barchable.clash.server;

/**
 * Incomplete layout or similar fault with village data.
 *
 * @author Sir Barchable
 */
public class LogicException extends RuntimeException {
    public LogicException() {
    }

    public LogicException(String message) {
        super(message);
    }

    public LogicException(String message, Throwable cause) {
        super(message, cause);
    }

    public LogicException(Throwable cause) {
        super(cause);
    }
}
