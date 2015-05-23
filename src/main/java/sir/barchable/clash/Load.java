package sir.barchable.clash;

import sir.barchable.clash.model.json.Village;
import sir.barchable.clash.model.json.WarVillage;
import sir.barchable.clash.protocol.Message;
import sir.barchable.util.Json;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

/**
 * Load messages from files.
 *
 * @author Sir Barchable
 *         Date: 18/05/15
 */
public class Load {
    private ClashServices services;
    private Main.LoadCommand command;

    public Load(ClashServices services, Main.LoadCommand command) {
        this.services = services;
        this.command = command;
    }

    public void run() throws IOException {
        File inFile = command.getInFile();

        String name = inFile.getName();

        if (name.endsWith(".pdu")) {
            try (FileInputStream in = new FileInputStream(inFile)) {
                Message message = services.getMessageFactory().fromStream(in);
                Json.writePretty(getFields(message), System.out);
            }
        } else {
            throw new IllegalArgumentException("Unknown file type (required .pdu)");
        }
    }

    /**
     * Unpack nested JSON encoded objects. This will extract know JSON formatted object descriptions from the PDU,
     * deserialize them, and insert the raw objects back into the source map in place of the strings.
     */
    private Map<String, Object> getFields(Message message) throws IOException {
        Map<String, Object> fields = message.getFields();

        switch (message.getType()) {
            case EnemyHomeData:
            case OwnHomeData:
            case VisitedHomeData:
                Village village = Json.valueOf((String) fields.get("homeVillage"), Village.class);
                fields.put("homeVillage", village);
                break;

            case WarHomeData:
                WarVillage warVillage = Json.valueOf((String) fields.get("homeVillage"), WarVillage.class);
                fields.put("homeVillage", warVillage);
                break;
        }

        return fields;
    }
}
