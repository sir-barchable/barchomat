package sir.barchable.clash.protocol;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;

/**
 * Message construction and serialization.
 *
 * @author Sir Barchable
 *         Date: 2/05/15
 */
public class MessageFactory {
    private TypeFactory typeFactory;
    private MessageReader reader;
    private MessageWriter writer;

    public MessageFactory(TypeFactory typeFactory) {
        this.typeFactory = typeFactory;
        this.reader = new MessageReader(typeFactory);
        this.writer = new MessageWriter(typeFactory);
    }

    /**
     * New empty message.
     *
     * @param type the message type
     * @return a new message with an empty field set
     */
    public Message newMessage(Pdu.Type type) {
        Protocol.StructDefinition definition = typeFactory.getStructDefinitionForId(type.id());
        if (definition == null) {
            throw new IllegalArgumentException("No definition for " + type);
        }
        return new Message(definition);
    }

    /**
     * Read a message.
     *
     * @param pdu the PDU containing the message
     * @return a map of field names -> field values, or null if the message ID isn't recognized
     */
    public Message fromPdu(Pdu pdu) {
        Optional<String> structName = typeFactory.getStructNameForId(pdu.getId());
        if (structName.isPresent()) {
            try {
                MessageInputStream in = new MessageInputStream(new ByteArrayInputStream(pdu.getPayload()));
                TypeFactory.Type type = typeFactory.parse(structName.get());
                Map<String, Object> fields = (Map<String, Object>) reader.readValue(type, in);
                return new Message(type.getStructDefinition(), fields);
            } catch (IOException e) {
                throw new PduException(e);
            }
        } else {
            return null;
        }
    }

    public Pdu toPdu(Message message) {
        ByteArrayOutputStream sink = new ByteArrayOutputStream();
        MessageOutputStream out = new MessageOutputStream(sink);
        try {
            TypeFactory.Type type = typeFactory.parse(message.getDefinition().getName());
            writer.write(type, message.getFields(), out);
            return new Pdu(type.getStructDefinition().getId(), sink.toByteArray());
        } catch (IOException e) {
            throw new PduException(e);
        }
    }
}
