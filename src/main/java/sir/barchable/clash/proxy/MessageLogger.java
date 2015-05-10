package sir.barchable.clash.proxy;

import sir.barchable.clash.protocol.PduException;
import sir.barchable.clash.protocol.Pdu.Type;
import sir.barchable.util.Json;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import static sir.barchable.clash.protocol.Pdu.Type.EndClientTurn;

/**
 * @author Sir Barchable
 */
public class MessageLogger {
    private Writer out;

    public MessageLogger() {
        out = new OutputStreamWriter(System.out, StandardCharsets.UTF_8);
    }

    public MessageLogger(Writer out) {
        this.out = out;
    }

    public MessageTap tapFor(Type messageType) {
        return tapFor(messageType, null);
    }

    public MessageTap tapFor(Type messageType, String field) {
        return (id, message) -> {
            if (id == messageType) {

                // Hack to ignore empty EndClientTurns...
                if (messageType == EndClientTurn) {
                    Object[] commands = (Object[]) message.get("commands");
                    if (commands == null || commands.length == 0) {
                        return;
                    }
                }

                Object value = message;
                if (field != null) {
                    value = message.get(field);
                }
                if (value != null) {
                    try {
                        out.write(String.valueOf(messageType));
                        out.write(":");
                        if (field != null) {
                            out.write(field);
                        }
                        out.write(" ");
                        Json.writePretty(value, out);
                        out.write('\n');
                        out.flush();
                    } catch (IOException e) {
                        throw new PduException(e);
                    }
                }
            }
        };
    }
}
