package sir.barchable.clash.protocol;

import sir.barchable.util.NoopCipher;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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

    public MessageReader getMessageReader() {
        return reader;
    }

    public MessageWriter getMessageWriter() {
        return writer;
    }

    public TypeFactory getTypeFactory() {
        return typeFactory;
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
            throw new TypeException("No type definition for " + type);
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
        try (ByteArrayInputStream in = new ByteArrayInputStream(pdu.getPayload())) {
            return fromStream(pdu.getType(), in);
        } catch (IOException e) {
            throw new PduException(e);
        }
    }

    /**
     * Deserialize a message.
     */
    public Message fromStream(Pdu.Type pduType, InputStream in) {
        Optional<String> structName = typeFactory.getStructNameForId(pduType.id());
        if (structName.isPresent()) {
            try {
                TypeFactory.Type type = typeFactory.newType(structName.get());
                Map<String, Object> fields = (Map<String, Object>) reader.readValue(type, MessageInputStream.toMessageInputStream(in));
                return new Message(type.getStructDefinition(), fields);
            } catch (IOException e) {
                throw new PduException(e);
            }
        } else {
            throw new TypeException("No type definition for " + pduType);
        }
    }

    /**
     * Read an unencrypted PDU from a stream and deserialize the contents.
     */
    public Message fromStream(InputStream in) throws IOException {
        return fromPdu(new PduInputStream(in, NoopCipher.NOOP_CIPHER).read());
    }

    public Pdu toPdu(Message message) {
        ByteArrayOutputStream sink = new ByteArrayOutputStream();
        MessageOutputStream out = new MessageOutputStream(sink);
        try {
            TypeFactory.Type type = typeFactory.newType(message.getDefinition().getName());
            writer.write(type, message.getFields(), out);
            return new Pdu(type.getStructDefinition().getId(), sink.toByteArray());
        } catch (IOException e) {
            throw new PduException(e);
        }
    }
}
