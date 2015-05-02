package sir.barchable.clash.protocol;

import sir.barchable.clash.protocol.PduInputStream;
import sir.barchable.clash.protocol.PduOutputStream;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Sir Barchable
 *         Date: 19/04/15
 */
public class Connection implements Closeable {
    private AtomicBoolean closed = new AtomicBoolean();

    private String name;
    private PduInputStream in;
    private PduOutputStream out;

    public Connection(Socket socket) throws IOException {
        this.name = socket.toString();
        this.in = new PduInputStream(socket.getInputStream());
        this.out = new PduOutputStream(socket.getOutputStream());
    }

    public Connection(String name, InputStream in, OutputStream out) {
        this.name = name;
        this.in = in instanceof PduInputStream ? (PduInputStream) in : new PduInputStream(in);
        this.out = out instanceof PduOutputStream ? (PduOutputStream) out : new PduOutputStream(out);
    }

    public String getName() {
        return name;
    }

    public PduInputStream getIn() {
        return in;
    }

    public PduOutputStream getOut() {
        return out;
    }

    public void close() throws IOException {
        if (closed.compareAndSet(false, true)) {
            try {
                in.close();
            } catch (IOException e) {
                // ignore
            }
            out.close();
        }
    }

    public void setKey(byte[] key) {
        in.setKey(key);
        out.setKey(key);
    }
}
