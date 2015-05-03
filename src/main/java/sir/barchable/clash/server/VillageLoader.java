package sir.barchable.clash.server;

import sir.barchable.clash.protocol.Message;
import sir.barchable.clash.protocol.MessageFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.regex.Pattern;

import static sir.barchable.clash.protocol.Pdu.Type.OwnHomeData;

/**
 * @author Sir Barchable
 */
public class VillageLoader {
    private static final Pattern HOME_PATTERN = Pattern.compile("OwnHomeData.*\\.pdu");

    private MessageFactory messageFactory;
    private File home;

    public VillageLoader(MessageFactory messageFactory, File dir) throws IOException {
        this.messageFactory = messageFactory;

        Optional<Path> homePath = Files.walk(dir.toPath())
            .filter(path -> HOME_PATTERN.matcher(path.toFile().getName()).matches())
            .findFirst();

        if (homePath.isPresent()) {
            home = homePath.get().toFile();
        } else {
            throw new FileNotFoundException("No home village file found in " + dir);
        }
    }

    public File getHome() {
        return home;
    }

    public Message loadHomeVillage() throws IOException {
        try (FileInputStream in = new FileInputStream(home)) {
            Message ownHomeData = messageFactory.fromStream(OwnHomeData, in);
            return ownHomeData;
        }
    }
}


