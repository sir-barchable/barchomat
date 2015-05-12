package sir.barchable.clash;

import com.beust.jcommander.ParameterException;
import sir.barchable.clash.model.Logic;
import sir.barchable.clash.model.LogicParser;
import sir.barchable.clash.protocol.MessageFactory;
import sir.barchable.clash.protocol.ProtocolTool;
import sir.barchable.clash.protocol.TypeFactory;

import java.io.File;
import java.io.IOException;

/**
 * Shared environment for the proxy and server.
 *
 * @author SirBarchable
 *         Date: 12/05/15
 */
public class ClashServices {
    private Logic logic;
    private MessageFactory messageFactory;
    private File workingDir;

    public ClashServices(Env env) throws IOException {
        workingDir = env.getWorkingDir();
        if (!workingDir.exists() || !workingDir.isDirectory()) {
            throw new ParameterException(workingDir.toString());
        }

        //
        // Read the protocol definition
        //

        TypeFactory typeFactory;
        if (env.getResourceDir() != null) {
            typeFactory = new TypeFactory(new ProtocolTool(env.getResourceDir()).read());
        } else {
            typeFactory = new TypeFactory();
        }
        messageFactory = new MessageFactory(typeFactory);

        //
        // Load the logic files
        //

        File logicFile;
        if (env.getLogicFile() != null) {
            logicFile = env.getLogicFile();
        } else {
            File[] apks = workingDir.listFiles((dir, name) -> name.endsWith(".apk"));
            if (apks.length != 1) {
                throw new ParameterException("Logic file not specified");
            } else {
                logicFile = apks[0];
            }
        }
        logic = LogicParser.loadLogic(logicFile);
    }

    public Logic getLogic() {
        return logic;
    }

    public MessageFactory getMessageFactory() {
        return messageFactory;
    }

    public File getWorkingDir() {
        return workingDir;
    }
}
