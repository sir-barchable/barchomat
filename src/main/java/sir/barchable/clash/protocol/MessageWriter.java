package sir.barchable.clash.protocol;

/**
 * @author Sir Barchable
 *         Date: 1/05/15
 */
public class MessageWriter {
    private TypeFactory typeFactory;

    public MessageWriter(TypeFactory typeFactory) {
        this.typeFactory = typeFactory;
    }

    public void write(TypeFactory.Type type, Object o, MessageOutputStream out) {

    }
}
