package sir.barchable.clash.protocol;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.util.Arrays;

import static sir.barchable.clash.protocol.Pdu.Type.EnemyHomeData;

/**
 * Read/write some test messages.
 *
 * @author Sir Barchable
 *         Date: 4/05/15
 */
public class TestRoundTrip {
    @Test
    public void rw() throws IOException {
        MessageFactory factory = new MessageFactory(new TypeFactory(ProtocolTool.read(new File("src/main/messages"))));

        byte[] pduBytesIn;
        try (FileInputStream in = new FileInputStream("EnemyHomeData.pdu")) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            IOUtils.copy(in, out);
            pduBytesIn = out.toByteArray();
        }

        Message enemyHome;
        try (FileInputStream in = new FileInputStream("EnemyHomeData.pdu")) {
            enemyHome = factory.fromStream(EnemyHomeData, in);
        }

        Pdu enemyHomePdu = factory.toPdu(enemyHome);
        byte[] pduBytesOut = enemyHomePdu.getPayload();

        Assert.assertEquals(pduBytesIn.length, pduBytesOut.length);

        for (int i = 0; i < pduBytesOut.length; i++) {
            byte a = pduBytesIn[i];
            byte b = pduBytesOut[i];
            Assert.assertEquals("byte " + i, a, b);
        }
    }
}
