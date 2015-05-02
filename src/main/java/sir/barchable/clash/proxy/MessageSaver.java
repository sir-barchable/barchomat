package sir.barchable.clash.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.barchable.clash.protocol.Pdu;
import sir.barchable.clash.protocol.Pdu.Type;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

import static sir.barchable.clash.protocol.Pdu.Type.*;

/**
 * Save PDU message payloads to a directory.
 *
 * @author Sir Barchable
 *         Date: 25/04/15
 */
public class MessageSaver implements PduFilter {
    private static final Logger log = LoggerFactory.getLogger(MessageSaver.class);

    private File saveDir;
    private Set<Type> types;

    /**
     * Construct a MessageSaver for village messages.
     *
     * @param saveDir where to save the messages
     */
    public MessageSaver(File saveDir) throws FileNotFoundException {
        this(saveDir, OwnHomeData, VisitedHomeData, EnemyHomeData);
    }

    /**
     * Construct a MessageSaver for specified message types.
     *
     * @param saveDir where to save the messages to
     * @param types the IDs of the PDUs to save
     */
    public MessageSaver(File saveDir, Type... types) throws FileNotFoundException {
        if (!saveDir.exists()) {
            throw new FileNotFoundException(saveDir.getName());
        }
        this.saveDir = saveDir;
        this.types = new HashSet<>(Arrays.asList(types));
    }

    @Override
    public Pdu filter(Pdu pdu) throws IOException {
        Type type = Type.valueOf(pdu.getId());
        try {
            if (types.contains(type)) {
                String name = String.format("%s-%2$tF-%2$tH-%2$tM-%2$tS.pdu", type, new Date());
                try (FileOutputStream out = new FileOutputStream(new File(saveDir, name))) {
                    out.write(pdu.getPayload());
                }
            }
        } catch (IOException e) {
            log.error("Couldn't save village", e);
        }
        return pdu;
    }
}
