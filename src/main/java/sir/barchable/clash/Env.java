package sir.barchable.clash;

import com.beust.jcommander.Parameter;

import java.io.File;

/**
 * Shared environment config.
 *
 * @author Sir Barchable
 *         Date: 12/05/15
 */
public class Env {
    @Parameter(names = {"-d", "--definition-dir"}, description = "Directory to load the protocol definition from")
    private File resourceDir;

    @Parameter(names = {"-l", "--logic"}, description = "Directory/file to load the game logic from")
    private File logicFile;

    @Parameter(names = {"-w", "--working-dir"}, description = "Directory to read streams from")
    private File workingDir = new File(".");

    public File getResourceDir() {
        return resourceDir;
    }

    public File getLogicFile() {
        return logicFile;
    }

    public File getWorkingDir() {
        return workingDir;
    }
}
